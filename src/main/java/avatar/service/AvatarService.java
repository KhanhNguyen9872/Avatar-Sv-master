package avatar.service;

import avatar.common.BossShopItem;
import avatar.item.Item;
import avatar.item.PartManager;
import avatar.item.Part;
import avatar.model.*;
import avatar.server.Avatar;
import avatar.server.ServerManager;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.File;
import java.util.List;

import avatar.constants.Cmd;
import avatar.network.Message;
import avatar.network.Session;
import avatar.play.Map;
import avatar.play.Zone;
import lombok.Builder;
import org.apache.log4j.Logger;

public class AvatarService extends Service {

    private static final Logger logger = Logger.getLogger(AvatarService.class);

    public AvatarService(Session cl) {
        super(cl);
    }

    public void openUIShop(int id, String name, List<Item> items) {
        try {
            System.out.println("openShop lent: " + items.size());
            Message ms = new Message(Cmd.OPEN_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(id);
            ds.writeUTF(name);
            ds.writeShort(items.size());
            for (Item i : items) {
                ds.writeShort(i.getId());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }

    public void openUIBossShop(BossShop bossShop, List<BossShopItem> items) {
        try {
            System.out.println("openShop lent: " + items.size());
            Message ms = new Message(Cmd.BOSS_SHOP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bossShop.getTypeShop());
            ds.writeInt(bossShop.getIdBoss());
            ds.writeByte(bossShop.getIdShop());
            ds.writeUTF(bossShop.getName());
            ds.writeShort(items.size());
            for (BossShopItem item : items) {
                ds.writeShort(item.getItemRequest());
                ds.writeUTF(item.initDialog(bossShop));
                if (bossShop.getTypeShop() == 1) {
                    ds.writeUTF(item.initDialog(bossShop));
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }

    public void doRequestExpicePet(Message mss) {
        try {
            int userID = mss.reader().readInt();
            Message ms = new Message(Cmd.REQUEST_EXPICE_PET);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(0);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("doRequestExpicePet ", ex);
        }
    }

    public void showUICreateChar(byte type) {
        try {
            Message ms = new Message(Cmd.CREATE_CHAR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("showUICreateChar ", ex);
        }
    }

    public void viewChest(List<Item> chests) {
        try {
            Message ms = new Message(Cmd.CONTAINER);
            DataOutputStream ds = ms.writer();
            ds.writeShort(chests.size());
            for (Item item : chests) {
                ds.writeShort(item.getId());
                ds.writeByte(100 - item.reliability());
                ds.writeUTF(item.expiredString());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("viewChest ", e);
        }
    }

    public void chatTo(String sender, String content) {
        try {
            Message ms = new Message(Cmd.CHAT_TO);
            DataOutputStream ds = ms.writer();
            ds.writeInt(1);
            ds.writeUTF(sender);
            ds.writeUTF(content);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("chatTo ", ex);
        }
    }

    public void onLoginSuccess() {
        try {
            User us = session.user;
            List<Item> wearing = us.getWearing();
            List<Command> listCmd = us.getListCmd();
            List<Command> listCmdRotate = us.getListCmdRotate();
            Message ms5 = new Message(Cmd.LOGIN_SUCESS);
            DataOutputStream ds = ms5.writer();
            ds.writeInt(us.getId());
            ds.writeByte(wearing.size());
            for (Item itm : wearing) {
                ds.writeShort(itm.getId());
            }
            ds.writeByte(us.getGender());
            ds.writeByte(us.getLeverMain());
            ds.writeByte(us.getLeverMainPercen());
            ds.writeInt(Math.toIntExact(us.getXu()));
            ds.writeByte(us.getFriendly());
            ds.writeByte(us.getCrazy());
            ds.writeByte(us.getStylish());
            ds.writeByte(us.getHappy());
            ds.writeByte(100 - us.getHunger());
            ds.writeInt(us.getLuong());
            ds.writeByte(us.getStar());
            for (Item itm : wearing) {
                ds.writeByte(0);
                ds.writeUTF(itm.expiredString());
            }
            ds.writeShort(us.getIdImg());
            ds.writeByte(listCmd.size());
            for (Command cmd : listCmd) {
                ds.writeUTF(cmd.getName());
                ds.writeShort(cmd.getIcon());
            }
            ds.writeByte(listCmdRotate.size());
            for (Command cmd : listCmdRotate) {
                ds.writeShort(cmd.getAnthor());
                ds.writeUTF(cmd.getName());
                ds.writeShort(cmd.getIcon());
            }
            ds.writeBoolean(true);// isTour
            for (Command cmd : listCmdRotate) {
                ds.writeByte(cmd.getType());
            }
            ds.writeByte(1);
            ds.writeShort(us.getLeverMain());
            ds.writeShort(-1);
            ds.writeBoolean(session.isNewVersion());//new version
            if (session.isNewVersion()) {
                ds.writeInt(us.getXeng());
            }
            int m = 4;
            ds.writeByte((byte) m);
            short[] IDAction = {103, 102, 104, 107};
            String[] actionName = new String[]{"Tặng Hoa Violet", "Hôn", "Tặng cánh hoa",
                    "Tặng Hoa Tuyết"};
            short[] IDIcon = {1124, 1188, 1187, 1173};
            int[] money = {20000, 2000, 10000, 5};
            byte[] typeMoney = {0, 0, 0, 1};
            for (int i2 = 0; i2 < m; ++i2) {
                ds.writeShort(IDAction[i2]);
                ds.writeUTF(actionName[i2]);
                ds.writeShort(IDIcon[i2]);
                ds.writeInt(money[i2]);
                ds.writeByte(typeMoney[i2]);
            }
            ds.writeInt(us.getLuong());
            ds.writeInt(us.getLuongKhoa());
            ds.writeByte(1);
            ds.writeUTF(us.getUsername());
            ds.flush();
            sendMessage(ms5);
        } catch (IOException ex) {
            logger.error("onLoginSuccess err", ex);
        }
    }

    public void getAvatarPart() {
        try {
            List<Part> parts = PartManager.getInstance().getAvatarPart();
            Message ms = new Message(Cmd.GET_AVATAR_PART);
            DataOutputStream ds = ms.writer();
            ds.writeShort(parts.size());
            for (Part part : parts) {
                ds.writeShort(part.getId());
                ds.writeInt(part.getCoin());
                ds.writeShort(part.getGold());
                short type = part.getType();
                ds.writeShort(type);
                switch (type) {
                    case -2:
                        ds.writeUTF(part.getName());
                        ds.writeByte(part.getSell());
                        ds.writeShort(part.getIcon());
                        break;

                    case -1:
                        ds.writeUTF(part.getName());
                        ds.writeByte(part.getSell());
                        ds.writeByte(part.getZOrder());
                        ds.writeByte(part.getGender());
                        ds.writeByte(part.getLevel());
                        ds.writeShort(part.getIcon());
                        short[] imgID = part.getImgID();
                        byte[] dx = part.getDx();
                        byte[] dy = part.getDy();
                        for (int i = 0; i < 15; i++) {
                            ds.writeShort(imgID[i]);
                            ds.writeByte(dx[i]);
                            ds.writeByte(dy[i]);
                        }
                        break;

                    default:
                        ds.writeShort(part.getIcon());
                        break;
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (Exception e) {
            logger.error("getAvatarPart() ", e);
        }
    }

    /**
     * Lấy thông tin item và giá tiền để in lên shop?
     *
     * @param ms
     */
    public void requestPartDynaMic(Message ms) {
        try {
            short itemID = ms.reader().readShort();
            Part part = PartManager.getInstance().findPartByID(itemID);
            // cmd -97
            ms = new Message(Cmd.REQUEST_DYNAMIC_PART);
            DataOutputStream ds = ms.writer();
            ds.writeShort(part.getId());
            ds.writeInt(part.getCoin());
            ds.writeShort(part.getGold());
            short type = part.getType();
            ds.writeShort(type);
            switch (type) {
                case -2:
                    ds.writeUTF(part.getName());
                    ds.writeByte(part.getSell());
                    ds.writeShort(part.getIcon());
                    break;

                case -1:
                    ds.writeUTF(part.getName());
                    ds.writeByte(part.getSell());
                    ds.writeByte(part.getZOrder());
                    ds.writeByte(part.getGender());
                    ds.writeByte(part.getLevel());
                    ds.writeShort(part.getIcon());
                    short[] imgID = part.getImgID();
                    byte[] dx = part.getDx();
                    byte[] dy = part.getDy();
                    for (int i = 0; i < 15; i++) {
                        ds.writeShort(imgID[i]);
                        ds.writeByte(dx[i]);
                        ds.writeByte(dy[i]);
                    }
                    break;
                default:
                    ds.writeShort(part.getIcon());
                    break;
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException ex) {
            logger.error("requestPartDynaMic() ", ex);
        }
    }

    public void enter(Zone z) {
        try {
            List<User> players = z.getPlayers();
            Map map = z.getMap();
            Message ms = new Message(Cmd.AVATAR_JOIN_PARK);
            DataOutputStream ds = ms.writer();
            ds.writeByte(map.getId());
            ds.writeByte(z.getId());
            ds.writeShort(-1);
            ds.writeShort(-1);
            int numUser = players.size();
            ds.writeByte((byte) numUser);
            for (User pl : players) {
                ds.writeInt(pl.getId());
                ds.writeUTF(pl.getUsername());
                ds.writeByte(pl.getWearing().size());
                for (Item item : pl.getWearing()) {
                    ds.writeShort(item.getId());
                }
                ds.writeShort(pl.getX());
                ds.writeShort(pl.getY());
                ds.writeByte(pl.getRole());//0 la npc
            }
            for (User pl : players) {
                ds.writeByte(pl.getDirect());
            }
            for (int i = 0; i < numUser; ++i) {
                ds.writeByte(101);
            }
            for (int i = 0; i < numUser; ++i) {
                ds.writeShort(-1);
            }
            ds.writeByte(0);
            ds.writeByte(0);
            List<MapItem> mapItems = map.getMapItems();
            List<MapItemType> mapItemTypes = map.getMapItemTypes();
            ds.writeShort(mapItems.size());
            ds.writeByte(mapItemTypes.size());
            for (MapItemType mapItemType : mapItemTypes) {
                ds.writeByte(mapItemType.getId());
                ds.writeShort(mapItemType.getImgID());
                ds.writeByte(mapItemType.getIconID());
                ds.writeShort(mapItemType.getDx());
                ds.writeShort(mapItemType.getDy());
                List<Position> positions = mapItemType.getListNotTrans();
                ds.writeByte(positions.size());
                for (Position position : positions) {
                    ds.writeByte(position.getX());
                    ds.writeByte(position.getY());
                }
            }
            ds.writeByte(mapItems.size());
            for (MapItem mapItem : mapItems) {
                ds.writeByte(mapItem.getType());
                ds.writeByte(mapItem.getTypeID());
                ds.writeByte(mapItem.getX());
                ds.writeByte(mapItem.getY());
            }
            for (int i = 0; i < numUser; ++i) {
                ds.writeShort(-1);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("enter() ", ex);
        }
    }

    public void getImageData() {
        try {
            List<ImageInfo> imageInfos = GameData.getInstance().getItemImageDatas();
            Message ms = new Message(Cmd.GET_IMAGE);
            DataOutputStream ds = ms.writer();
            ds.writeShort(imageInfos.size());
            for (ImageInfo imageInfo : imageInfos) {
                ds.writeShort(imageInfo.getId());
                ds.writeShort(imageInfo.getBigImageID());
                ds.writeByte(imageInfo.getX());
                ds.writeByte(imageInfo.getY());
                ds.writeByte(imageInfo.getW());
                ds.writeByte(imageInfo.getH());
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getImageData() ", e);
        }
    }

    public void getMapItemType() {
        try {
            System.out.println("get map item type");
            List<MapItemType> mapItemTypes = GameData.getInstance().getMapItemTypes();
            Message ms = new Message(Cmd.MAP_ITEM_TYPE);
            DataOutputStream ds = ms.writer();
            ds.writeShort(mapItemTypes.size());
            for (MapItemType mapItemType : mapItemTypes) {
                ds.writeShort(mapItemType.getId());
                ds.writeUTF(mapItemType.getName());
                ds.writeUTF(mapItemType.getDes());
                ds.writeShort(mapItemType.getImgID());
                ds.writeShort(mapItemType.getIconID());
                ds.writeByte(mapItemType.getDx());
                ds.writeByte(mapItemType.getDy());
                ds.writeShort(mapItemType.getPriceXu());
                ds.writeShort(mapItemType.getPriceLuong());
                ds.writeByte(mapItemType.getBuy());
                List<Position> positions = mapItemType.getListNotTrans();
                ds.writeByte(positions.size());
                for (Position p : positions) {
                    ds.writeByte(p.getX());
                    ds.writeByte(p.getY());
                }
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getMapItemType() ", e);
        }
    }

    public void getTileMap() {
        try {
            byte[] dat = Avatar.getFile(session.getResourcesPath() + "house/tile.png");
            if (dat == null) {
                return;
            }
            Message ms = new Message(Cmd.GET_TILE_MAP);
            DataOutputStream ds = ms.writer();
            ds.writeShort(21);
            ds.writeInt(dat.length);
            ds.write(dat);
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getTileMap() ", e);
        }
    }

    public void getMapItem() {
        try {
            System.out.println("get map item");
            List<MapItem> mapItems = GameData.getInstance().getMapItems();
            Message ms = new Message(Cmd.MAP_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeShort(mapItems.size());
            for (MapItem mapItem : mapItems) {
                ds.writeShort(mapItem.getId());
                ds.writeShort(mapItem.getTypeID());
                ds.writeByte(mapItem.getType());
                ds.writeByte(mapItem.getX());
                ds.writeByte(mapItem.getY());
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getMapItem() ", e);
        }
    }

    public void getBigImage(Message ms) {
        try {
            short id = ms.reader().readShort();
            String folder = session.getResourcesPath() + "big/";
            byte[] dat = Avatar.getFile(folder + id + ".png");
            if (dat == null) {
                return;
            }
            ms = new Message(Cmd.GET_BIG);
            DataOutputStream ds = ms.writer();
            ds.writeShort(id);
            ds.writeShort(dat.length);
            ds.writeShort(dat.length);
            ds.write(dat);
            if (id > 20) {
                ds.writeShort(2);
            } else if (id > 10) {
                ds.writeShort(1);
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getBigImage() ", e);
        }
    }

    public void getBigData() {
        try {
            Message ms = new Message(Cmd.SET_BIG);
            DataOutputStream ds = ms.writer();
            File file = new File(session.getResourcesPath() + "big/");
            File[] listFiles = file.listFiles();
            ds.writeByte(listFiles.length);
            for (File f : listFiles) {
                String name = f.getName().split("\\.")[0];
                int id = Integer.parseInt(name);
                int size = (int) f.length();
                ds.writeShort(id);
                ds.writeShort(size);
            }
            ds.writeShort(ServerManager.bigImgVersion);
            ds.writeShort(ServerManager.partVersion);
            ds.writeShort(ServerManager.bigItemImgVersion);
            ds.writeShort(ServerManager.itemTypeVersion);
            ds.writeShort(ServerManager.itemVersion);
            ds.writeByte(0);
            ds.writeInt(ServerManager.objectVersion);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("getBigData() ", ex);
        }
    }

    public void updateMoney(int type) {
        try {
            Message ms = new Message(Cmd.UPDATE_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(session.user.xeng);
            ds.writeByte((byte) type);
            ds.writeInt(Math.toIntExact(session.user.xu));
            ds.writeInt(session.user.luong);
            ds.writeInt(session.user.luongKhoa);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("updateMoney ", ex);
        }
    }

    public void openMenuOption(int userID, int menuID, String... menus) {
        try {
            Message ms = new Message(Cmd.MENU_OPTION);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(menuID);
            ds.writeByte(menus.length);
            for (String menu : menus) {
                ds.writeUTF(menu);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("openMenuOption ", e);
        }
    }

    public void openMenuOption(int userID, int menuID, List<Menu> menus) {
        try {
            Message ms = new Message(Cmd.MENU_OPTION);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(menuID);
            ds.writeByte(menus.size());
            for (Menu menu : menus) {
                ds.writeUTF(menu.getName());
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("openMenuOption ", e);
        }
    }

    public void openUIMenu(int userID, int menuID, List<Menu> menus, String npcName, String npcChat) {
        try {
            Message ms = new Message(Cmd.MENU_OPTION);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userID);
            ds.writeByte(menuID);
            ds.writeByte(menus.size());
            for (Menu m : menus) {
                ds.writeUTF(m.getName());
            }
            for (Menu m : menus) {
                ds.writeShort(m.getId());
            }
            if (npcName != null) {
                ds.writeUTF(npcName);
                ds.writeUTF(npcChat);
                for (Menu m : menus) {
                    ds.writeBoolean(m.isMenu());
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            logger.error("openMenuOption ", e);
        }
    }

    public void requestYourInfo(User us) {
        try {
            Message ms = new Message(Cmd.REQUEST_YOUR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeInt(us.getId());
            ds.writeByte(us.getLeverMain());
            ds.writeByte(us.getLeverMainPercen());
            ds.writeByte(us.getFriendly());
            ds.writeByte(us.getCrazy());
            ds.writeByte(us.getStylish());
            ds.writeByte(us.getHappy());
            ds.writeByte(100 - us.getHunger());
            ds.writeInt(-1);//keets hon
            ds.writeShort(us.getLeverMain());
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void getFoodData() {
        try {
            Message ms = new Message(Cmd.GET_ITEM_INFO);
            DataOutputStream ds = ms.writer();
            List<Food> foods = FoodManager.getInstance().getFoods();
            ds.writeShort(foods.size());
            for (Food food : foods) {
                ds.writeShort(food.getId());
                ds.writeUTF(food.getName());
                ds.writeUTF(food.getDescription());
                ds.writeInt(food.getPrice());
                ds.writeByte(food.getShop());
                ds.writeShort(food.getIcon());
            }
            ds.flush();
            this.sendMessage(ms);
        } catch (IOException e) {
            logger.error("getFoodData ", e);
        }
    }

    public void customTab(String title, String content) {
        try {
            Message ms = new Message(Cmd.CUSTOM_TAB);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(title);
            ds.writeUTF(content);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("customTab ", ex);
        }
    }


    public void sendEffectStyle4(byte id, byte loopLimit, short num, byte timeStop) {
        try {
            Message ms = new Message(Cmd.EFFECT_OBJ);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(id);
            ds.writeByte(4);
            ds.writeByte(loopLimit);
            ds.writeShort(num);
            ds.writeByte(timeStop);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ex) {
            logger.error("send eff ", ex);
        }
    }

    public void sendEffectData(Message mss) {
        try {
            byte id = mss.reader().readByte();
            String folder = session.getResourcesPath() + "effect/";
            byte[] imageData = Avatar.getFile(folder + id + ".png");
            byte[] effData = Avatar.getFile("res/data/effect/" + id + ".dat");


            Message ms = new Message(Cmd.EFFECT_OBJ);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(id);
            ds.writeShort(imageData.length);
            ds.write(imageData);
            ds.write(effData);
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
