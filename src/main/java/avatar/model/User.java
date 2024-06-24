package avatar.model;

import avatar.common.BossShopItem;
import avatar.db.DbManager;
import avatar.item.Item;
import avatar.item.Part;
import avatar.lucky.DialLucky;
import avatar.network.Session;

import java.sql.*;
import java.util.Date;

import avatar.service.*;
import org.json.simple.JSONValue;
import org.json.simple.JSONObject;

import java.io.IOException;

import avatar.network.Message;
import avatar.play.MapService;

import java.util.ArrayList;

import avatar.play.Zone;
import avatar.server.GameString;
import avatar.server.ServerManager;
import avatar.server.UserManager;
import avatar.server.Utils;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;

@Getter
@Setter
public class User {

    private static final Logger logger = Logger.getLogger(User.class);
    public Session session;
    private int id;
    private String username;
    private String password;
    private byte gender;
    public long xu;
    public int datCuoc = 0;
    public long tienCuoc;
    public boolean SicboNhanTien = false;
    public int luong;
    public int luongKhoa;
    public int xeng;
    private short clanID;
    private byte role;
    private byte star;
    private int leverMain;
    private int expMain;
    private int leverFarm;
    private byte leverPercen;
    private int expFarm;
    private byte friendly;
    private byte crazy;
    private byte stylish;
    private byte happy;
    private byte hunger;
    private byte chestSlot;
    private byte chestHomeSlot;
    private List<Item> wearing;
    private List<Item> chests;
    private Zone zone;
    private short x, y;
    private byte direct;
    private List<Menu> menus;
    private DialLucky dialLucky;
    private short idImg = -1;
    private List<Command> listCmd;
    private List<Command> listCmdRotate;
    @Getter
    @Setter
    private boolean loadDataFinish;

    @Getter
    @Setter
    private List<BossShopItem> bossShopItems;

    public User() {
        this.role = -1;
        this.chests = new ArrayList<>();
        this.wearing = new ArrayList<>();
        this.listCmd = new ArrayList<>();
        this.listCmdRotate = new ArrayList<>();
    }

    public AvatarService getAvatarService() {
        return session.getAvatarService();
    }

    public FarmService getFarmService() {
        return session.getFarmService();
    }

    public HomeService getHomeService() {
        return session.getHomeService();
    }

    public ParkService getParkService() {
        return session.getParkService();
    }

    public MapService getMapService() {
        if (zone == null) {
            return NoService.getInstance();
        }
        return zone.getService();
    }


    public Service getService() {
        return session.getService();
    }

    public void sortWearing() {
        this.wearing.sort((o1, o2) -> o1.getPart().getZOrder() - o2.getPart().getZOrder());
    }

    public void setGender(byte gender) {
        this.gender = gender;
    }

    public synchronized void updateXu(long xuUp) {
        this.xu += xuUp;
    }

    public synchronized void updateLuong(int luongUp) {
        this.luong += luongUp;
    }

    public synchronized void updateLuongKhoa(int luongUp) {
        this.luong += luongUp;
    }

    public synchronized void updateXeng(int xengUp) {
        this.xeng += xengUp;
    }

    public synchronized void updateHunger(int hunger) {
        this.hunger += (byte) hunger;
    }

    public void sendMessage(Message ms) {
        this.session.sendMessage(ms);
    }

    protected void saveData() {
        DbManager.getInstance().executeUpdate("UPDATE `players` SET `gender` = ?, `friendly` = ?, `crazy` = ?, `stylish` = ?, `happy` = ?, `hunger` = ? WHERE `user_id` = ? LIMIT 1;",
                this.gender, this.friendly, this.crazy, this.stylish, this.happy, this.hunger, this.id);
        DbManager.getInstance().executeUpdate("UPDATE `players` SET `xu` = ?, `luong` = ?, `luong_khoa` = ?, `xeng` = ?, `level_main` = ?, `exp_main` = ? WHERE `user_id` = ? LIMIT 1;",
                this.xu, this.luong, this.luongKhoa, this.xeng, this.leverMain, this.expMain, this.id);
        JSONArray jChests = new JSONArray();
        for (Item item : this.chests) {
            JSONObject obj = new JSONObject();
            obj.put("id", item.getId());
            obj.put("expired", item.getExpired());
            obj.put("quantity", item.getQuantity());
            jChests.add(obj);
        }
        JSONArray jWearing = new JSONArray();
        for (Item item : this.wearing) {
            JSONObject obj = new JSONObject();
            obj.put("id", item.getId());
            obj.put("expired", item.getExpired());
            obj.put("quantity", item.getQuantity());
            jWearing.add(obj);
        }
        DbManager.getInstance().executeUpdate("UPDATE `players` SET `chests` = ?, `wearing` = ? WHERE `user_id` = ? LIMIT 1;",
                jChests.toJSONString(), jWearing.toJSONString(), this.id);
        System.out.println("Save data user " + this.getUsername());
    }

    public synchronized boolean login() {
        if (!ServerManager.active) {
            getService().serverMessage("Máy chủ đang bảo trì. Vui lòng quay lại sau!");
            return false;
        }
        String ACCOUNT_LOGIN = "SELECT * FROM `users` WHERE `username` = ? AND `password` = ? LIMIT 1;";
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(ACCOUNT_LOGIN);) {
            ps.setString(1, this.username);
            ps.setString(2, Utils.md5(password));
            try (ResultSet red = ps.executeQuery()) {
                if (red.next()) {
                    this.id = red.getInt("id");
                    this.role = (byte) red.getInt("role");
                    boolean active = red.getBoolean("active");
                    if (!active) {
                        getService().serverMessage(GameString.userLoginActive());
                        return false;
                    }
                    JSONObject banData = (JSONObject) ((red.getString("ban") != null)
                            ? JSONValue.parse(red.getString("ban"))
                            : new JSONObject());
                    if (!banData.isEmpty()) {
                        int banType = ((Long) banData.get("type")).intValue();
                        if (banType == 2) {
                            if (banData.get("forever") != null) {
                                getService().serverMessage(GameString.userLoginLockForever());
                                return false;
                            }
                            int minutes = ((Long) banData.get("minutes")).intValue();
                            Date timeNowwww = new Date();
                            Date banStart = Utils.getDate((String) banData.get("start"));
                            Date banEnd = new Date(banStart.getTime() + 60000 * minutes);
                            if (banEnd.after(timeNowwww)) {
                                minutes = (int) ((banEnd.getTime() - timeNowwww.getTime()) / 60000L);
                                getService().serverMessage(GameString.userLoginLock(minutes));
                                return false;
                            }
                        }
                    }
                    User us = UserManager.getInstance().find(this.id);
                    if (us != null) {
                        getService().serverMessage(GameString.userLoginMany());
                        us.getService().serverMessage(GameString.userLoginMany());
                        Utils.setTimeout(() -> {
                            us.session.close();
                        }, 1000);
                        return false;
                    }
                    return true;
                } else {
                    getService().serverMessage(GameString.loginPassFail());
                }
            }
        } catch (SQLException ex) {
            getService().serverMessage(ex.getMessage());
        }
        return false;
    }

    public boolean loadData() {
        String GET_PLAYER_DATA = "SELECT * FROM `players` WHERE `user_id` = ? LIMIT 1;";
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement(GET_PLAYER_DATA);) {
            ps.setInt(1, this.id);
            try (ResultSet res = ps.executeQuery()) {
                if (res.next()) {
                    if (res.getInt("user_id") == 7) {
                        //this.id+=(Npc.ID_ADD+1000);
                    }
                    this.leverMain = res.getInt("level_main");
                    this.expMain = res.getInt("exp_main");
                    this.gender = res.getByte("gender");
                    this.gender = res.getByte("gender");
                    this.xu = res.getLong("xu");
                    this.luong = res.getShort("luong");
                    this.luongKhoa = res.getShort("luong_khoa");
                    this.xeng = res.getInt("xeng");
                    this.gender = res.getByte("gender");
                    this.clanID = res.getShort("clan_id");
                    this.friendly = res.getByte("friendly");
                    this.crazy = res.getByte("crazy");
                    this.stylish = res.getByte("stylish");
                    this.happy = res.getByte("happy");
                    this.hunger = res.getByte("hunger");
                    this.star = res.getByte("star");
                    this.chests = new ArrayList<>();
                    JSONArray chests = (JSONArray) JSONValue.parse(res.getString("chests"));
                    for (Object chest : chests) {
                        JSONObject obj = (JSONObject) chest;
                        int id = ((Long) obj.get("id")).intValue();
                        long expired = ((Long) obj.get("expired"));
                        int quantity = 1;
                        if (obj.containsKey("quantity")) {
                            quantity = ((Long) obj.get("quantity")).intValue();
                        }
                        Item item = Item.builder().id(id)
                                .quantity(quantity)
                                .expired(expired)
                                .build();
                        if (item.reliability() > 0) {
                            this.chests.add(item);
                        }
                    }
                    this.wearing = new ArrayList<>();
                    JSONArray wearing = (JSONArray) JSONValue.parse(res.getString("wearing"));
                    for (Object o : wearing) {
                        JSONObject obj = (JSONObject) o;
                        int id = ((Long) obj.get("id")).intValue();
                        long expired = ((Long) obj.get("expired"));
                        int quantity = 1;
                        if (obj.containsKey("quantity")) {
                            quantity = ((Long) obj.get("quantity")).intValue();
                        }
                        Item item = Item.builder().id(id)
                                .quantity(quantity)
                                .expired(expired)
                                .build();
                        if (item.reliability() > 0) {
                            this.wearing.add(item);
                        }
                    }
                    setLoadDataFinish(true);
                    return true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            getService().serverMessage(ex.getMessage());
        }
        return false;
    }

    public void initAvatar() {
        sortWearing();
        listCmd.add(new Command("Chức năng", 2));
//        listCmd.add(new Command("Quản trị", 0));
        listCmdRotate.add(new Command((short) 0, "Hội nhóm", 41, (byte) 1));
        listCmdRotate.add(new Command((short) 4, "Oan Tu Xi", 44, (byte) 1));
        listCmdRotate.add(new Command((short) 33, "Hô phong hoán vũ", 1053, (byte) 1));
        listCmdRotate.add(new Command((short) 34, "Triệu hồi bia mộ", 1053, (byte) 1));
        listCmdRotate.add(new Command((short) 35, "Cánh thần hiển linh", 1055, (byte) 0));
        listCmdRotate.add(new Command((short) 48, "pháo sinh nhật(5 lượng)", 1115, (byte) 0));
        listCmdRotate.add(new Command((short) 47, "Pháo hạnh phúc (5 lượng)", 242, (byte) 0));
        listCmdRotate.add(new Command((short) 8, "Pháo thịnh vượng (5 lượng)", 241, (byte) 0));
        listCmdRotate.add(new Command((short) 23, "Cuốc", 869, (byte) 0));
        listCmdRotate.add(new Command((short) 36, "Hẹn hò", 1096, (byte) 1));
    }

    public void doAction(Message ms) {
        try {
            int idTo = ms.reader().readInt();
            short action = ms.reader().readShort();
            getMapService().doAction(id, idTo, action);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getExpMax() {
        return (this.leverMain * (this.leverMain + 1) / 2) * 1000;
    }

    public byte getLeverMainPercen() {
        return (byte) (this.expMain * 100 / getExpMax());
    }

    public void viewChest(Message ms) {
//        int type = ms.reader();
        List<Item> _chests = chests.stream().filter(item -> {
            return item.getPart().getZOrder() != 30 && item.getPart().getZOrder() != 40;
        }).collect(Collectors.toList());
        getAvatarService().viewChest(_chests);
    }

    public void requestYourInfo(Message ms) {
        try {
            int userId = ms.reader().readInt();
            User us = UserManager.getInstance().find(userId);
            if (us != null) {
                getAvatarService().requestYourInfo(us);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doAvatarFeel(Message ms) {
        try {
            if (ms.reader().available() <= 0) {
                return;
            }
            byte idFeel = ms.reader().readByte();
            getMapService().doAvatarFeel(id, idFeel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        if (zone != null) {
            zone.leave(this);
        }
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        DbManager.getInstance().executeUpdate("UPDATE `players` SET `is_online` = ?, `client_id` = ?, `last_online` = ? WHERE `user_id` = ? LIMIT 1;", 0, session.id, timestamp, this.id);
        if (isLoadDataFinish()) {
            saveData();
        }
    }

    @Override
    public String toString() {
        return "User " + this.username;
    }

    public void move(Message ms) {
        try {
            if (ms.reader().available() < 5) {
                return;
            }
            short x = ms.reader().readShort();
            short y = ms.reader().readShort();
            byte direct = ms.reader().readByte();
            if (ms.reader().available() >= 2) {
                ms.reader().readShort();
            }
            this.x = x;
            this.y = y;
            this.direct = direct;
            getMapService().move(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addExp(int exp) {
        this.expMain += exp;
        int expMax = getExpMax();
        if (this.expMain >= expMax) {
            this.leverMain++;
            this.expMain -= expMax;
        }
    }

    public void addItemToChests(Item item) {
        synchronized (chests) {
            Item itm = findItemInChests(item.getId());
            if (itm != null) {
                if (itm.getPart().getType() == -2) {
                    itm.increase(item.getQuantity());
                } else {
                    setReliabilityForItem(itm, item);
                }
                return;
            } else {
                itm = findItemInWearing(item.getId());
                if (itm != null) {
                    setReliabilityForItem(itm, item);
                    return;
                }
            }
            this.chests.add(item);
        }
    }

    public void setReliabilityForItem(Item old, Item newI) {
        if (!old.isForever()) {
            if (newI.isForever() || newI.reliability() > old.reliability()) {
                old.setExpired(newI.getExpired());
            }
        }
    }

    public void removeItemFromChests(Item item) {
        synchronized (chests) {
            this.chests.remove(item);
        }
    }

    public void addItemToWearing(Item item) {
        synchronized (wearing) {
            this.wearing.add(item);
        }
    }

    public void removeItemFromWearing(Item item) {
        synchronized (wearing) {
            this.wearing.remove(item);
        }
    }

    public Item findItemInChests(int id) {
        synchronized (chests) {
            for (Item item : chests) {
                if (item.getId() == id) {
                    return item;
                }
            }
            return null;
        }
    }

    public Item findItemInWearing(int id) {
        synchronized (wearing) {
            for (Item item : wearing) {
                if (item.getId() == id) {
                    return item;
                }
            }
            return null;
        }
    }

    public Item findItemWearingByZOrder(int zOrder) {
        synchronized (wearing) {
            for (Item item : wearing) {
                if (item.getPart().getZOrder() == zOrder) {
                    return item;
                }
            }
            return null;
        }
    }

    public boolean removeItem(int id, int quantity) {
        Item item = findItemInChests(id);
        if (item != null) {
            int q = item.reduce(quantity);
            if (q <= 0) {
                removeItemFromChests(item);
            }
            return true;
        }
        return false;
    }

    public void usingItem(short itemID, byte type) {
        try {
            logger.debug("itemID: " + itemID + " type: " + type);
            if (type == 1) {
                logger.debug("Find Item");
                Item item = findItemInChests(itemID);
                logger.debug("Find Done");
                if (item == null) {
                    getService().serverDialog("Không tìm thấy vật phẩm");
                    return;
                }
                logger.debug("Find Detail Item");
                Part part = item.getPart();
                int gender = part.getGender();
                logger.debug("Item Gender=" + gender);
                if ((gender == 1 || gender == 2) && (this.gender != gender)) {
                    getService().serverDialog("Giới tính không phù hợp");
                    return;
                }
                logger.debug("Get Type");
                short pType = part.getType();
                logger.debug("Type =" + pType);
                if (pType == -1) {
                    int zOrder = part.getZOrder();
                    Item w = findItemWearingByZOrder(zOrder);
                    if (w != null) {
                        removeItemFromWearing(w);
                        addItemToChests(w);
                    }
                    addItemToWearing(item);
                    removeItemFromChests(item);
                    sortWearing();
                    getMapService().usingPart(id, itemID);
                } else if (pType == -2) {
                    getService().serverMessage(String.format("Số lượng: %,d", item.getQuantity()));
                } else {
                    getService().serverDialog("Vật phẩm lỗi, không thể sử dụng");
                }
            } else {
                Item item = findItemInWearing(itemID);
                if (item == null) {
                    return;
                }
                int zOrder = item.getPart().getZOrder();
                if (zOrder == 10 || zOrder == 20 || zOrder == 50) {
                    getService().serverMessage("Không thể cất vật phẩm này.");
                    return;
                }
                removeItemFromWearing(item);
                addItemToChests(item);
                getMapService().usingPart(id, itemID);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void usingItem(Message ms) {
        try {
            short itemID = ms.reader().readShort();
            byte type = ms.reader().readByte();
            usingItem(itemID, type);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void chat(Message ms) {
        try {
            if (ms.reader().available() < 4) {
                return;
            }
            String message = ms.reader().readUTF();
            getMapService().chat(this, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doRemoveItem(Message ms) {
        try {
            short itemID = ms.reader().readShort();
            byte type = ms.reader().readByte();
            if (type == 0) {
                Item item = findItemInWearing(itemID);
                if (item != null) {
                    int zOrder = item.getPart().getZOrder();
                    if (zOrder == 10 || zOrder == 20 || zOrder == 50) {
                        return;
                    }
                    removeItemFromWearing(item);
                    getMapService().removeItem(id, itemID);
                    if (getStylish() > 0) {
                        setStylish((byte) (getStylish() - 1));
                        getAvatarService().requestYourInfo(this);
                    }
                }
            } else {
                Item item = findItemInChests(itemID);
                if (item != null) {
                    removeItemFromChests(item);
                    getAvatarService().removeItem(id, itemID);
                    if (getStylish() > 0) {
                        setStylish((byte) (getStylish() - 1));
                        getAvatarService().requestYourInfo(this);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyNetWaitMessage() throws IOException {
        synchronized (this.session.obj) {
            this.session.obj.notifyAll();
        }
    }
    public void attackNpc(Npc npc, int damage) {
        // Giảm HP toàn cục của NPC khi bị tấn công
        Npc.decreaseGlobalHp(damage);
    }

    public void skillUidToBoss(List<User> players,int us ,int npcID,byte skill1,byte skill2){
        for (User player : players) {
            EffectService.createEffect()
                    .session(player.session)
                    .id(skill1)
                    .style((byte) 0)
                    .loopLimit((byte) 5)
                    .loop((short) 1)
                    .loopType((byte) 1)
                    .radius((short) 25)
                    .idPlayer(us)
                    .send();
            EffectService.createEffect()
                    .session(player.session)
                    .id(skill2)
                    .style((byte) 0)
                    .loopLimit((byte) 5)
                    .loop((short) 1)
                    .loopType((byte) 1)
                    .radius((short) 25)
                    .idPlayer(npcID)
                    .send();
        };
    }
}
