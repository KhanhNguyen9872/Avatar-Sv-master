package avatar.server;

import avatar.model.User;
import avatar.db.DbManager;
import avatar.item.Item;
import avatar.item.PartManager;
import avatar.model.FoodManager;
import avatar.model.GameData;
import avatar.network.Session;
import avatar.play.Zone;

import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import avatar.model.Npc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;

import avatar.network.Message;

import java.sql.ResultSet;
import java.sql.PreparedStatement;

import avatar.play.Map;
import avatar.play.MapManager;
import avatar.play.NpcManager;

import java.net.ServerSocket;

import static avatar.constants.NpcName.boss;
import static avatar.model.Npc.currentNpcCount;

public class ServerManager {

    public static String cityName;
    public static String hashSettings;
    public static boolean active;
    protected static short port;
    public static String notify;
    public static int bigImgVersion;
    public static int partVersion;
    public static int bigItemImgVersion;
    public static int itemTypeVersion;
    public static int itemVersion;
    public static int objectVersion;
    public static String resHDPath;
    public static String resMediumPath;
    protected static int numClients;
    public static ArrayList<Session> clients;
    protected static ServerSocket server;
    protected static boolean start;
    protected static int id;
    private static boolean debug;

    private static void loadConfigFile() {
        try {
            FileInputStream input = new FileInputStream(new File("config.properties"));
            Properties props = new Properties();
            props.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            ServerManager.port = Short.parseShort(props.getProperty("server.port"));
            ServerManager.cityName = props.getProperty("game.city.name");
            ServerManager.active = Boolean.parseBoolean(props.getProperty("server.active"));
            ServerManager.debug = Boolean.parseBoolean(props.getProperty("server.debug"));
            if (props.containsKey("game.notify")) {
                ServerManager.notify = props.getProperty("game.notify");
            }
            ServerManager.bigImgVersion = Short.parseShort(props.getProperty("game.big.image.version"));
            ServerManager.partVersion = Short.parseShort(props.getProperty("game.part.version"));
            ServerManager.bigItemImgVersion = Short.parseShort(props.getProperty("game.big.item.image.version"));
            ServerManager.itemTypeVersion = Short.parseShort(props.getProperty("game.itemtype.version"));
            ServerManager.itemVersion = Integer.parseInt(props.getProperty("game.item.version"));
            ServerManager.objectVersion = Integer.parseInt(props.getProperty("game.object.version"));
            ServerManager.resHDPath = props.getProperty("game.resources.hd.path");
            ServerManager.resMediumPath = props.getProperty("game.resources.medium.path");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected static void loadSettings() {
        System.out.println("Load settings in database");
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `settings`;");
             ResultSet res = ps.executeQuery();) {
            HashMap<String, String> settings = new HashMap<>();
            while (res.next()) {
                String name = res.getString("name");
                String value = res.getString("value");
                settings.put(name, value);
            }
            if (settings.containsKey("hash_settings")) {
                ServerManager.hashSettings = settings.get("hash_settings");
            }
            if (settings.containsKey("bao_tri")) {
                ServerManager.active = Boolean.parseBoolean(settings.get("bao_tri"));
            }
            if (settings.containsKey("thong_bao")) {
                ServerManager.notify = settings.get("thong_bao");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    protected static void init() {
        ServerManager.start = false;
        DbManager.getInstance().start();
        loadConfigFile();
        loadSettings();
        GameData.getInstance().load();
        PartManager.getInstance().load();
        FoodManager.getInstance().load();
        int numMap = 60;
        for (int i = 0; i < numMap; ++i) {
            MapManager.getInstance().add(new Map(i, 0, 30));
        }
        System.out.println("Load NPC data start ...");
        loadNpcData();
        initZombie();
        System.out.println("Reset player online ...");
        DbManager.getInstance().executeUpdate("UPDATE `players` SET `is_online` = 0, `client_id` = -1");
        System.out.println("Reset player online successfully");
    }
    public static void initZombie(){
        Thread t = new Thread(() -> {
            Random random = new Random(); // Đối tượng Random để sinh ngẫu nhiên
            while (true) { // Vòng lặp vô hạn để NPC xuất hiện liên tục
                if (currentNpcCount.get() < 10) { // Kiểm tra xem có thể xuất hiện thêm NPC không
                    List<Integer> availableItems = Arrays.asList(2401, 4552, 6314, 6432);
                    int randomItemId = availableItems.get(random.nextInt(availableItems.size()));
                    Map m = MapManager.getInstance().find(11);
                    if (m != null) {
                        List<Zone> zones = m.getZones();
                        List<String> chatMessages = Arrays.asList(
                                "gãi ngứa à",
                                "hãy xem đây"
                        );
                        Zone randomZone = zones.get(random.nextInt(zones.size())); // Chọn ngẫu nhiên một khu vực từ danh sách
                        Npc zomber = Npc.builder()
                                .id(Npc.ID_ADD + boss) // ID ngẫu nhiên cho NPC
                                .name("boss")
                                .wearing(new ArrayList<>())
                                .build();
                        zomber.addItemToWearing(new Item(randomItemId)); // Thêm một item mặc định cho NPC
                        zomber.setTextChats(chatMessages); // Thêm một thông điệp chat mặc định
                        NpcManager.getInstance().add(zomber); // Thêm NPC vào quản lý NPC
                        // Ngẫu nhiên vị trí xuất hiện trong khu vực
                        short randomX = (short) 250;
                        short randomY = (short) 50;
                        // Nhập NPC vào khu vực với vị trí ngẫu nhiên

                        randomZone.enter(zomber, randomX, randomY);
                        System.out.println("khu :"+randomZone.getId());
                        // Tăng số lượng NPC hiện tại trong khu vực lên 1
                        currentNpcCount.incrementAndGet();
                        try {
                            Thread.sleep(10000); // Chờ 10 giây trước khi xuất hiện NPC tiếp theo
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        // Kiểm tra và xử lý di chuyển và gửi kỹ năng cho NPC
                        List<User> players = randomZone.getPlayers();
                        for (User u : players) {
                            if (u.getId() == 6) { // Ví dụ: nếu người chơi có ID là 6
                                zomber.NpcMove(zomber, u.getX(), u.getY()); // Di chuyển NPC đến vị trí của người chơi
                                System.out.println("Move x: " + u.getX() + " Y: " + u.getY());
                                try {
                                    Thread.sleep(2000); // Chờ 2 giây trước khi gửi kỹ năng
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                zomber.skill(zomber, (byte) 27); // Gửi kỹ năng từ NPC đến người chơi
                                System.out.println("Send skill");
                            }
                        }
                    }
                } else {
                    // Nếu số lượng NPC đạt đến giới hạn, không xuất hiện thêm cho đến khi có NPC bị giết
                    try {
                        Thread.sleep(1000); // Chờ 1 giây trước khi kiểm tra lại
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        t.start();
    }

    private static void loadNpcData() {
        int numNPC = 0;
        try (Connection connection = DbManager.getInstance().getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `npc`;");
             ResultSet res = ps.executeQuery();) {
            while (res.next()) {
                int botID = res.getInt("id");
                String botName = res.getString("name");
                byte map = res.getByte("map");
                short X = res.getShort("x");
                short Y = res.getShort("y");
                ArrayList<Item> items = new ArrayList<>();
                JSONArray listItem = (JSONArray) JSONValue.parse(res.getString("items"));
                for (Object o : listItem) {
                    Item item = new Item(((Long) o).intValue());
                    items.add(item);
                }
                List<String> chat = new ArrayList<>();
                JSONArray listChat = (JSONArray) JSONValue.parse(res.getString("chat"));
                for (Object o : listChat) {
                    chat.add((String) o);
                }
                Map m = MapManager.getInstance().find(map);
                if (m != null) {
                    List<Zone> zones = m.getZones();
                    for (Zone z : zones) {
                        Npc npc = Npc.builder()
                                .id(botID)
                                .name(botName)
                                .x(X)
                                .y(Y)
                                .wearing(items)
                                .build();
                        npc.setTextChats(chat);
                        NpcManager.getInstance().add(npc);
                        z.enter(npc, X, Y);
                    }
                }
                System.out.println("  + NPC " + Utils.removeAccent(botName) + " - " + botID);
                ++numNPC;
            }
            System.out.println("Load success " + numNPC + " NPC !");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }


    protected static void start() {
        System.out.println("Start socket port = " + ServerManager.port);
        try {
            ServerManager.clients = new ArrayList<>();
            ServerManager.server = new ServerSocket(ServerManager.port);
            ServerManager.id = 0;
            ServerManager.numClients = 0;
            ServerManager.start = true;
            System.out.println("Start server Success !");
            while (ServerManager.start) {
                try {
                    Socket client = ServerManager.server.accept();
                    Session cl = new Session(client, ++ServerManager.id);
                    ServerManager.clients.add(cl);
                    ++ServerManager.numClients;
                    log("Accept socket " + cl + " done!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void stop() {
        if (ServerManager.start) {
            System.out.println("--->[ CLOSE ]<---");
            close();
            ServerManager.start = false;
            System.gc();
        }
    }

    protected static void close() {
        try {
            ServerManager.server.close();
            ServerManager.server = null;
            while (ServerManager.clients.size() > 0) {
                Session c = ServerManager.clients.get(0);
                c.close();
                --ServerManager.numClients;
            }
            ServerManager.clients = null;
            DbManager.getInstance().shutdown();
            System.gc();
            System.out.println("End socket");
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public static void log(String s) {
        if (ServerManager.debug) {
            System.out.println(s);
        }
    }

    public static void disconnect(Session cl) {
        synchronized (ServerManager.clients) {
            ServerManager.clients.remove(cl);
            --ServerManager.numClients;
            System.out.println("Disconnect client: " + cl);
        }
    }

    public static void joinAreaMessage(User us, Message ms) throws IOException {
        byte map = ms.reader().readByte();
        byte area = ms.reader().readByte();
        short x = ms.reader().readShort();
        short y = ms.reader().readShort();
        System.out.println("map: " + map + " area: " + area + " x: " + x + " y: " + y);
        if (area < 0) {
            area = joinAreaAutoNumber(map);
        }
        Map m = MapManager.getInstance().find(map);
        if (m != null) {
            List<Zone> zones = m.getZones();
            Zone zone = zones.get(area);
            zone.enter(us, x, y);
        }
    }

    private static byte joinAreaAutoNumber(byte map) {
        Map m = MapManager.getInstance().find(map);
        List<Zone> zones = m.getZones();
        int i = 0;
        for (Zone z : zones) {
            if (z.getPlayers().size() <= 15) {
                return (byte) i;
            }
            i++;
        }
        return 0;
    }
}
