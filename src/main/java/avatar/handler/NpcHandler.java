package avatar.handler;

import avatar.constants.Cmd;
import avatar.constants.NpcName;
import avatar.item.Item;
import avatar.model.Command;
import java.math.BigInteger;
import avatar.model.User;
import avatar.lucky.DialLucky;
import avatar.lucky.DialLuckyManager;
import avatar.model.Menu;
import avatar.model.Npc;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.io.IOException;
import java.util.concurrent.*;
import avatar.network.Message;
import avatar.play.Map;
import avatar.play.MapManager;
import avatar.play.NpcManager;
import avatar.play.Zone;
import avatar.server.ServerManager;
import avatar.server.UserManager;
import avatar.server.Utils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static avatar.constants.NpcName.boss;
import static avatar.model.Npc.getGlobalHp;

public class NpcHandler {

    public static void handleDiaLucky(User us, byte type) {
        DialLucky dl = DialLuckyManager.getInstance().find(type);
        if (dl != null) {
            if (dl.getType() == DialLuckyManager.MIEN_PHI) {
                Item itm = us.findItemInChests(593);
                if (itm == null || itm.getQuantity() <= 0) {
                    us.getAvatarService().serverDialog("Bạn không có Vé quay số miễn phí!");
                    return;
                }
            }
            if (dl.getType() == DialLuckyManager.XU) {
                if (us.getXu() < 15000) {
                    us.getAvatarService().serverDialog("Bạn không đủ xu!");
                    return;
                }
            }
            if (dl.getType() == DialLuckyManager.LUONG) {
                if (us.getLuong() < 5) {
                    us.getAvatarService().serverDialog("Bạn không đủ lượng!");
                    return;
                }
            }
        }
        us.setDialLucky(dl);
        dl.show(us);
    }

    public void handlerTaiXiu(Message ms) throws IOException {

    }
    public static void handlerCommunicate(int npcId, User us) throws IOException {
        Zone z = us.getZone();
        if (z != null) {
            User u = z.find(npcId);
            if (u == null) {
                return;
            }
        } else {
            return;
        }
        int npcIdCase = npcId - Npc.ID_ADD;

        switch (npcIdCase) {
            case boss:{
                List<Menu> list = List.of(
                        Menu.builder().name("damage").action(() -> {
                            List<User> players = us.getZone().getPlayers();
                            for (int i = 0; i < players.size(); i++) {
                                if(players.get(i).getUsername() == "boss")
                                {
                                    List<Integer> availableItems = Arrays.asList(2401, 4552, 6314, 6432);
                                    Utils random = null;
                                    int randomItemId = availableItems.get(random.nextInt(availableItems.size()));
                                    Map m = MapManager.getInstance().find(11);
                                    Npc npc = (Npc) players.get(i);

                                    us.attackNpc(npc,10);
                                    us.skillUidToBoss(players,us.getId(),npcId, (byte) 25, (byte) 26);
                                    int bosItem = npc.getWearing().get(0).getId();
                                    switch (bosItem) {
                                        case 2401:
                                            npc.skill(npc,(byte)27);
                                            break;
                                        case 4552:
                                            npc.skillUidToBoss(players,npc.getId(), us.getId(), (byte) 25, (byte) 26);
                                            break;
                                        case 6314:
                                            npc.skillUidToBoss(players,npc.getId(), us.getId(), (byte) 40, (byte) 41);
                                            break;
                                        case 6432:
                                            npc.skillUidToBoss(players,npc.getId(), us.getId(), (byte) 42, (byte) 43);
                                            break;
                                    }
                                    if (npc.getGlobalHp()<=0){
                                        npc.skill(npc,(byte)45);
                                        List<String> chatMessages = Arrays.asList(
                                                "tạm biệt mấy ông chau",
                                                "ta chuyển sinh đây"
                                        );
                                        npc.setTextChats(chatMessages);
                                        try {
                                            Thread.sleep(3000);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        npc.getZone().leave(npc);
                                        List<Zone> zones = m.getZones();
                                        random = null;
                                        Zone randomZone = zones.get(random.nextInt(zones.size())); // Chọn ngẫu nhiên một khu vực từ danh sách
                                        Npc zomber = Npc.builder()
                                                .id(Npc.ID_ADD + boss) // ID ngẫu nhiên cho NPC
                                                .name("boss")
                                                .wearing(new ArrayList<>())
                                                .build();
                                        zomber.addItemToWearing(new Item(randomItemId));; // Thêm một item mặc định cho NPC
                                        zomber.addChat("Tao bất tử OK"); // Thêm một thông điệp chat mặc định
                                        zomber.resetGlobalHp(1000*2);
                                        NpcManager.getInstance().add(zomber); // Thêm NPC vào quản lý NPC
                                        // Ngẫu nhiên vị trí xuất hiện trong khu vực
                                        short randomX = (short) 250;
                                        short randomY = (short) 50;
                                        // Nhập NPC vào khu vực với vị trí ngẫu nhiên

                                        randomZone.enter(zomber, randomX, randomY);
                                        System.out.println("khu :"+randomZone.getId());
                                    }
                                }
                            }
                        }).build()
                );
                us.setMenus(list);
                us.getAvatarService().openUIMenu(npcId, 0, list, "boss", "hp tao:"+ getGlobalHp());
                break;
            }
            case NpcName.Tai_Xiu: {
                List<Menu> list = new ArrayList<>();
                Menu taiXiu = Menu.builder().name("Chơi Tài Xỉu").menus(
                                List.of(
                                        Menu.builder().name("Cược Tài").action(() -> {
                                            us.getAvatarService().sendTextBoxPopup(us.getId(), 12, "Nhập Số Tiền Cược Tài", 1);
                                        }).build(),
                                        Menu.builder().name("Cược Xỉu").action(() -> {
                                            us.getAvatarService().sendTextBoxPopup(us.getId(), 13, "Nhập Số Tiền Cược Xỉu", 1);
                                        }).build(),
                                        Menu.builder().name("Tất Tay tài").action(() -> {
                                            us.getAvatarService().sendTextBoxPopup(us.getId(), 14, "Nhập Bừa 1 Số Để Tất Tay Tài 100.000.000 Xu", 1);
                                        }).build(),
                                        Menu.builder().name("Tất Tay xỉu").action(() -> {
                                            us.getAvatarService().sendTextBoxPopup(us.getId(), 15, "Nhập Bừa 1 Số Để Tất Tay Xỉu 100.000.000 Xu", 1);
                                        }).build()
                                ))
                        .id(npcId)
                        .npcName("Không Tài Thì Xỉu")
                        .npcChat("xuc xac nao")
                        .build();
                list.add(taiXiu);
                list.add(Menu.builder().name("Lịch sử").action(() -> {
                    us.getAvatarService().customTab("Lịch sử kết quả", "lịch sử kết quả và lịch sử cược? co cai db chưa làm !!!");
                }).build());
                list.add(Menu.builder().name("Hướng dẫn").action(() -> {
                    us.getAvatarService().customTab("Hướng dẫn", "cứ ôn in bừa");
                }).build());
                list.add(Menu.builder().name("Thoát").build());
                us.setMenus(list);
                us.getAvatarService().openUIMenu(npcId, 0, list, "tài xỉu", "");
                break;
            }
            case NpcName.binzoet:{

                List<Menu> list = new ArrayList<>();
                list.add(Menu.builder().name("reset xu luong").action(() -> {
                    //us.setXu(999999999);
                    //us.setLuong(9999);
                    us.getAvatarService().serverDialog("reset cc");
                }).build());
                list.add(Menu.builder().name("Thoát").build());
                us.setMenus(list);
                us.getAvatarService().openUIMenu(npcId, 0, list, "", "");
                break;
            }
            case NpcName.em_Thinh:{
                List<Menu> list = new ArrayList<>();
                List<Item> Items = new ArrayList<>();
                Menu quaySo = Menu.builder().name("vật phẩm").menus(
                                List.of(
                                        Menu.builder().name("demo item").action(() -> {
                                            for (int i = 2000; i < 6676; i++) {
                                                Item item = new Item((short) i);
                                                Items.add(item);
                                            }
                                            us.getAvatarService().openUIShop(-49,"em.Thinh",Items);
                                        }).build()
                                ))
                        .id(npcId)
                        .npcName("donate đi")
                        .npcChat("show Item")
                        .build();
                list.add(quaySo);
                list.add(Menu.builder().name("Hướng dẫn").action(() -> {
                    us.getAvatarService().customTab("Hướng dẫn", "hãy nạp lần đầu để mở khóa mua =)))");
                }).build());
                list.add(Menu.builder().name("Thoát").build());
                us.setMenus(list);
                us.getAvatarService().openUIMenu(npcId, 0, list, "donate đi", "");
                break;
            }


            case NpcName.QUAY_SO: {
                List<Menu> list = new ArrayList<>();
                Menu quaySo = Menu.builder().name("Quay số").menus(
                                List.of(
                                        Menu.builder().name("5 lượng").action(() -> {
                                            handleDiaLucky(us, DialLuckyManager.LUONG);
                                        }).build(),
                                        Menu.builder().name("15.000 xu").action(() -> {
                                            handleDiaLucky(us, DialLuckyManager.XU);
                                        }).build(),
                                        Menu.builder().name("Q.S miễn phí").action(() -> {
                                            handleDiaLucky(us, DialLuckyManager.MIEN_PHI);
                                        }).build(),
                                        Menu.builder().name("Thoát").action(() -> {
                                        }).build()
                                ))
                        .id(npcId)
                        .npcName("Quay số")
                        .npcChat("Quay số may mắn đây")
                        .build();
                list.add(quaySo);
                list.add(Menu.builder().name("Xem hướng dẫn").action(() -> {
                    us.getAvatarService().customTab("Hướng dẫn", "Để tham gia quay số bạn phải có ít nhất 5 lượng hoặc 25 ngàn xu trong tài khoản và 3 ô trống trong rương\n Bạn sẽ nhận được danh sách những món đồ đặc biệt mà bạn muốn quay. Những món đồ đặc biệt này bạn sẽ không thể tìm thấy trong bất cứ shop nào của thành phố.\n Sau khi chọn được món đồ muốn quay bạn sẽ bắt đầu chỉnh vòng quay để quay\n Khi quay bạn giữ phím 5 để chỉnh lực quay sau đó thả ra để bắt đầu quay\n Khi quay bạn sẽ có cơ hội trúng từ 1 đến 3 món quà\n Quà của bạn nhận được có thể là vật phẩm bất kì, xu, hoặc điểm kinh nghiệm\n Bạn có thể quay được những bộ đồ bán bằng lượng như đồ hiệp sĩ, pháp sư...\n Tuy nhiên vật phẩm bạn quay được sẽ có hạn sử dụng trong một số ngày nhất định.\n Nếu bạn quay được đúng món đồ mà bạn đã chọn thì bạn sẽ được sở hữu món đồ đó vĩnh viễn.\n Hãy thử vận may để sở hữa các món đồ cực khủng nào !!!");
                }).build());
                list.add(Menu.builder().name("Thoát").build());
                us.setMenus(list);
                us.getAvatarService().openUIMenu(npcId, 0, list, "quay số", "Vòng quay may mắn nhận những vật phẩm quí hiếm đây! Mại dô!");
                break;
            }
            case NpcName.THO_KIM_HOAN: {
                List<Menu> list = new ArrayList<>();
                String npcName = "Thợ KH";
                String npcChat = "Muốn đồ đang mặc đẹp hơn không? Ta có thể giúp bạn đấy";
                Menu upgrade = Menu.builder().name("Nâng cấp").id(npcId).npcName(npcName).npcChat(npcChat).menus(
                                List.of(
                                        Menu.builder().name("Nâng cấp xu").id(npcId).npcName(npcName).npcChat(npcChat)
                                                .menus(listItemUpgrade(npcId, us, BossShopHandler.SELECT_XU))
                                                .build(),
                                        Menu.builder().name("Nâng cấp lượng").id(npcId).npcName(npcName).npcChat(npcChat)
                                                .menus(listItemUpgrade(npcId, us, BossShopHandler.SELECT_LUONG))
                                                .id(npcId)
                                                .build(),
                                        Menu.builder().name("Thoát").id(npcId).build()
                                )
                        )
                        .build();
                list.add(upgrade);
                list.add(Menu.builder().name("Xem hướng dẫn")
                        .action(() -> {
                            us.getAvatarService().customTab("Hướng dẫn", "Nâng thì nâng không nâng thì cút!");
                        })
                        .build());
                list.add(Menu.builder().name("Thoát").id(npcId).build());
                us.setMenus(list);
                us.getAvatarService().openUIMenu(npcId, 0, list, npcName, npcChat);
                break;
            }
            case NpcName.LAI_BUON: {
                List<Menu> list = List.of(
                        Menu.builder().action(() -> {
                            Item item = new Item(593, -1, 1);
                            us.addItemToChests(item);
                            us.addExp(5);
                            us.getService().serverMessage("Bạn nhận được 1 điểm chuyên cần + 1 thẻ quay số miễn phí");
                        }).build(),
                        Menu.builder().name("Thông tin chuyển cần").action(() -> {
                        }).build(),
                        Menu.builder().name("Đổi quà").action(() -> {
                        }).build(),
                        Menu.builder().name("Hướng dẫn").action(() -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Đăng nhập mỗi ngày để nhận quà:").append("\n");
                            sb.append("Báo danh mỗi ngày để nhận 1 bịch phân bón giảm 15 phút").append("\n");
                            sb.append("Báo danh mỗi 3 ngày để nhận 1 bịch phân bón giảm 30 phút").append("\n");
                            sb.append("Báo danh mỗi 6 ngày để nhận 1 bịch phân bón giảm 60 phút").append("\n");
                            sb.append("Bên cạnh đó  báo danh mỗi ngày nhận dduocww 5 điểm chuyên cần và 1 thẻ quay số miễn phí").append("\n");
                            sb.append("Dùng điểm chuyên cần để nhận đucợ những món quà có giá trị trong tương lai").append("\n");
                            us.getAvatarService().customTab("Báo danh hàng ngày", sb.toString());
                        }).build()
                );
                us.setMenus(list);
                us.getAvatarService().openUIMenu(npcId, 0, list, "quay số", "Vòng quay may mắn nhận những vật phẩm quí hiếm đây! Mại dô!");
            }
            break;
        }
    }

    public static List<Menu> listItemUpgrade(int npcId, User us, byte type) {
        String npcName = "Thợ KH";
        String npcChat = "Muốn đồ đang mặc đẹp hơn không? Ta có thể giúp bạn đấy";
        return List.of(
                Menu.builder().name("Quà cầm tay").id(npcId).npcName(npcName).npcChat(npcChat)
                        .menus(List.of(
                                        Menu.builder().name("Bông hoa cổ tích").action(() -> {
                                            BossShopHandler.displayUI(us, type, 6212, 6213, 6214);
                                        }).build(),
                                        Menu.builder().name("Hoa hồng phong thần").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5321, 5322, 5323);
                                        }).build(),
                                        Menu.builder().name("Hoa hồng xanh pha lê thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5286, 5287, 5288);
                                        }).build(),
                                        Menu.builder().name("Mộc thảo hồ điệp").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4160, 4161, 4162, 4163, 5050);
                                        }).build(),
                                        Menu.builder().name("Cung thần tình yêu thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4893, 4894, 4895);
                                        }).build(),
                                        Menu.builder().name("Cung xanh thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4890, 4891, 4892);
                                        }).build(),
                                        Menu.builder().name("Gậy thả thính mê hoặc").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3507, 4218);
                                        }).build(),
                                        Menu.builder().name("Chong chóng thiên thần").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2238, 2239, 2274, 2275, 2404);
                                        }).build(),
                                        Menu.builder().name("Cục vàng huyền thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2217, 2218, 2219, 2220, 2221, 2222, 2223);
                                        }).build()

                                )
                        )
                        .build(),
                Menu.builder().name("Nón").npcName(npcName).npcChat(npcChat).menus(List.of(
                                Menu.builder().name("Nón phù thuỷ hoả ngục truyền thuyết").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2411, 2412, 2413, 2414, 5503, 5504);
                                }).build(),
                                Menu.builder().name("Vương miện hoàng thân").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5394);
                                }).build(),
                                Menu.builder().name("Vương miện hoàng thân").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5391);
                                }).build(),
                                Menu.builder().name("Tôi thấy hoa vàng trên cỏ xanh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3266, 3267, 3268, 3269, 3954);
                                }).build(),
                                Menu.builder().name("Vương miện phép màu").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3422, 3423, 3639, 3640);
                                }).build(),
                                Menu.builder().name("Mũ ảo thuật tinh anh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2899, 2900, 2901, 2902, 2903, 3037, 3038, 3039);
                                }).build(),
                                Menu.builder().name("Ma vương").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4096, 4731);
                                }).build(),
                                Menu.builder().name("Cửu vỹ hồ ly").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4724, 4728, 4729);
                                }).build(),
                                Menu.builder().name("Lợn lém lỉnh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4376);
                                }).build(),
                                Menu.builder().name("Tuần lộc tinh anh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4323, 4324);
                                }).build(),
                                Menu.builder().name("Bay nax 2.0").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4079, 4080);
                                }).build(),
                                Menu.builder().name("Phương hoàng lửa").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3668, 3771, 3772, 3773, 3854);
                                }).build(),
                                Menu.builder().name("King Kong").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3744);
                                }).build(),
                                Menu.builder().name("Kỳ lân truyền thuyết").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2726, 2727, 2728, 2729, 2730);
                                }).build()
                        ))
                        .build(),
                Menu.builder().name("Trang phục").npcName(npcName).npcChat(npcChat)
                        .menus(
                                List.of(
                                        Menu.builder().name("Danh gia vọng tộc").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5395, 5396);
                                        }).build(),
                                        Menu.builder().name("Danh gia vọng tộc").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5392, 5393);
                                        }).build(),
                                        Menu.builder().name("Nữ hoàng sương mai").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5054, 5055);
                                        }).build(),
                                        Menu.builder().name("Bá tước bóng đêm").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2876, 2877);
                                        }).build(),
                                        Menu.builder().name("Napoleon").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2231, 2232);
                                        }).build(),
                                        Menu.builder().name("Elizabeth").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2229, 2230);
                                        }).build()
                                )
                        )
                        .build(),
                Menu.builder().name("Cánh").npcName(npcName).npcChat(npcChat)
                        .menus(
                                List.of(
                                        Menu.builder().name("Cánh chiến thần hắc hoá").action(() -> {
                                            BossShopHandler.displayUI(us, type, 5971, 5972, 5973, 5974);
                                        }).build(),
                                        Menu.builder().name("Cánh quạ đen hoả ngục").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4332, 5313);
                                        }).build(),
                                        Menu.builder().name("Cánh tiểu thần phong linh").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2419, 2482, 2483, 2505, 2506, 5252, 5253);
                                        }).build(),
                                        Menu.builder().name("Cửu vỹ hồ ly thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4333, 4910, 4911, 4912, 4913, 4914, 4915, 4916, 4334, 4889);
                                        }).build(),
                                        Menu.builder().name("Cánh vàng ròng đa sắc").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3376, 3377, 3404, 4897);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên thần tiên bướm").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4056, 4796);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên hồ tình yêu vĩnh cửu").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4196, 4435);
                                        }).build(),
                                        Menu.builder().name("Cánh băng hoả thần thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3448, 4057, 4375);
                                        }).build(),
                                        Menu.builder().name("Cánh hoả thần").action(() -> {
                                            BossShopHandler.displayUI(us, type, 4311, 4312, 4313);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên sứ tình yêu").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2148, 2149, 2150, 2151, 2152, 3637);
                                        }).build(),
                                        Menu.builder().name("Cánh thiên sứ").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2142, 2143, 2144, 2145, 2146, 3635);
                                        }).build(),
                                        Menu.builder().name("Cánh địa ngục hắc ám").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3529, 3530, 3531, 3532);
                                        }).build(),
                                        Menu.builder().name("Cánh cổng địa ngục").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3522, 3523, 3524, 3525, 3526, 3527);
                                        }).build(),
                                        Menu.builder().name("Cánh bướm đêm huyền thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3366, 3379);
                                        }).build(),
                                        Menu.builder().name("Cánh băng giá huyền thoại").action(() -> {
                                            BossShopHandler.displayUI(us, type, 3365, 3378);
                                        }).build(),
                                        Menu.builder().name("Cánh phép màu ước mơ").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2793, 2794, 2795, 2796);
                                        }).build(),
                                        Menu.builder().name("Cánh blue vững vàng").action(() -> {
                                            BossShopHandler.displayUI(us, type, 2788, 2789, 2790, 2791);
                                        }).build()
                                )
                        )
                        .build(),
                Menu.builder().name("Thú cưng").npcName(npcName).npcChat(npcChat)
                        .menus(List.of(
                                Menu.builder().name("Lang thần lãnh nguyên").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5517, 5518);
                                }).build(),
                                Menu.builder().name("Thiên thần hồ điệp").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5486, 5487);
                                }).build(),
                                Menu.builder().name("Thiên thần hộ mệnh toàn năng").action(() -> {
                                    BossShopHandler.displayUI(us, type, 5224, 5225, 5226);
                                }).build(),
                                Menu.builder().name("Tiểu tiên bướm").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4305, 5058);
                                }).build(),
                                Menu.builder().name("Cáo tuyết cửu vỹ").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4904, 4905);
                                }).build(),
                                Menu.builder().name("Ma vương").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4096, 4731);
                                }).build(),
                                Menu.builder().name("Cửu vỹ hồ ly").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4724, 4728, 4729);
                                }).build(),
                                Menu.builder().name("Lợn lém lỉnh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4376);
                                }).build(),
                                Menu.builder().name("Tuần lộc tinh anh").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4323, 4324);
                                }).build(),
                                Menu.builder().name("Bay nax 2.0").action(() -> {
                                    BossShopHandler.displayUI(us, type, 4079, 4080);
                                }).build(),
                                Menu.builder().name("Phương hoàng lửa").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3668, 3771, 3772, 3773, 3854);
                                }).build(),
                                Menu.builder().name("King Kong").action(() -> {
                                    BossShopHandler.displayUI(us, type, 3744);
                                }).build(),
                                Menu.builder().name("Kỳ lân truyền thuyết").action(() -> {
                                    BossShopHandler.displayUI(us, type, 2726, 2727, 2728, 2729, 2730);
                                }).build()
                        ))
                        .build()
        );
    }

    public static void handlerAction(User us, int npcId, byte menuId, byte select) throws IOException {
        Zone z = us.getZone();
        if (z != null) {
            User u = z.find(npcId);
            if (u == null) {
                return;
            }
        } else {
            return;
        }
//        int npcIdCase = npcId - 2000000000;
        List<Menu> menus = us.getMenus();
        if (menus != null && select < menus.size()) {
            Menu menu = menus.get(select);
            if (menu.isMenu()) {
                us.setMenus(menu.getMenus());
                us.getAvatarService().openUIMenu(npcId, menuId + 1, menu.getMenus(), menu.getNpcName(), menu.getNpcChat());
            } else if (menu.getAction() != null) {
                menu.perform();
            } else {
                switch (menu.getId()) {

                }
            }
            return;
        }
    }
}
