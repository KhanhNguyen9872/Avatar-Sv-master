package avatar.model;
import avatar.item.Item;

import java.math.BigInteger;
import java.util.*;

import avatar.network.Message;
import avatar.play.NpcManager;
import avatar.play.Zone;
import avatar.model.User;
import avatar.server.UserManager;
import avatar.constants.NpcName;
import avatar.item.Item;
import avatar.network.Message;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import avatar.service.EffectService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class Npc extends User {

    public static final int ID_ADD = 2000000000;
    private static boolean Cuoc = false;
    private static boolean Sicboquay = false;
    @Getter
    @Setter
    private List<String> textChats;
    private List<User> lstUser = UserManager.users;
    public User user;
    private static int globalHp = 1000;
    public static AtomicInteger currentNpcCount = new AtomicInteger(0);
    private int id;
    private int hp;
    // Phương thức để giảm HP toàn cục của NPC khi bị tấn công
    public static synchronized void decreaseGlobalHp(int damage) {
        globalHp -= damage;
        if (globalHp <= 0) {
            globalHp = 0;
        }
    }
    public static synchronized int getGlobalHp() {
        return globalHp;
    }
    public static synchronized void resetGlobalHp(int hp) {
        globalHp = hp; // Reset lại HP khi NPC được khởi tạo lại hoặc đặt lại
    }
    private static List<String> textChatsTaiXiu = Arrays.asList("Xin chào!","Hôm nay thời tiết đẹp nhỉ?","5","4","3","2","1","top");
    private Thread autoChatBot = new Thread(() -> {
        while (true) {
            try {
                if(this.getId() == 2000000856){
                    for (String text : textChatsTaiXiu) {
                        getMapService().chat(this, text);
                        Thread.sleep(1000);
                        if(text.contains("Tài")) {
                            Npc.Cuoc = false;
                            for (int i = 0; i < lstUser.stream().count(); i++) {
                                if(lstUser.get(i).datCuoc == 0){break;}
                                if(lstUser.get(i).datCuoc == 1)
                                {
                                    if(lstUser.get(i).SicboNhanTien == false){
                                        lstUser.get(i).SicboNhanTien = true;
                                        long sum = (lstUser.get(i).tienCuoc)*2;
                                        lstUser.get(i).updateXu(sum);
                                        lstUser.get(i).getAvatarService().serverDialog("Chúc Mừng Bạn Đã Bú " + text + " và thắng " + sum + " Tiền.");
                                        lstUser.get(i).getAvatarService().updateMoney(0);
                                    }
                                } else {
                                    long XuCuoc = lstUser.get(i).tienCuoc;
                                    lstUser.get(i).getAvatarService().serverDialog("Cút Rồi "+ text+ " Bạn đã gãy "+XuCuoc+" Xu");
                                }
                                lstUser.get(i).SicboNhanTien = true;
                                lstUser.get(i).datCuoc = 0;
                                lstUser.get(i).tienCuoc = 0;
                                NewSicbo();
                            }
                        } else if (text.contains("Xỉu")) {
                            Npc.Cuoc = false;
                            for (int i = 0; i < lstUser.stream().count(); i++) {
                                {
                                    if(lstUser.get(i).datCuoc == 0){break;}
                                    if (lstUser.get(i).datCuoc == 2) {
                                        if(lstUser.get(i).SicboNhanTien == false){
                                            lstUser.get(i).SicboNhanTien = true;
                                            long sum = (lstUser.get(i).tienCuoc)*2;
                                            lstUser.get(i).updateXu(sum);
                                            lstUser.get(i).getAvatarService().serverDialog("Chúc Mừng Bạn Đã Bú " + text + " và thắng " + sum + " Tiền.");
                                            lstUser.get(i).getAvatarService().updateMoney(0);
                                        }
                                    } else {
                                        long XuCuoc = lstUser.get(i).tienCuoc;
                                        lstUser.get(i).getAvatarService().serverDialog("Cút Rồi " + text + " Bạn đã gãy " + XuCuoc + " Xu");
                                    }
                                }
                                lstUser.get(i).SicboNhanTien = true;
                                lstUser.get(i).datCuoc = 0;
                                lstUser.get(i).tienCuoc = 0;
                                NewSicbo();
                            }

                        } else if (text.contains("top")) {
                            Npc.Cuoc = false;
                            NewSicbo();
                        }
                    }
                }
                for (String text : textChats) {
                    getMapService().chat(this, text);
                    Thread.sleep(6000);
                }

                if (textChats == null || textChats.size() == 0) {
                    Thread.sleep(10000);
                }
            } catch (InterruptedException ignored) {

            }
        }
    });
    public void NewSicbo(){
        if(Npc.Cuoc==false||Sicboquay == false) {
            Random random = new Random();
            int dice1 = random.nextInt(6) + 1;
            int dice2 = random.nextInt(6) + 1;
            int dice3 = random.nextInt(6) + 1;
            int total = dice1 + dice2 + dice3;
            Npc.Cuoc = true;
            Sicboquay = true;
            //System.out.println("Dice 1: " + dice1);
            ///System.out.println("Dice 2: " + dice2);
           // System.out.println("Dice 3: " + dice3);
            //System.out.println("Total: " + total);
            List<String> stringList = new ArrayList<>();
            for (int i = 30; i >= 1; i--) {
                stringList.add("Thời Gian Đặt Cược : " + i + " S");
                System.out.print(i);
            }
            if (total <= 10) {
                stringList.add("Xỉu " + dice1 + "," + dice2 + "," + dice3);
               // System.out.println("xỉu (Small)");
            } else {
                stringList.add("Tài " + dice1 + "," + dice2 + "," + dice3);
               // System.out.println("Tài (Big)");
            }

           // System.out.println("new sicbo");
            stringList.add("Top Win 1: comingSoon |top 2: comingSoon | Top 3: comingSoon");
            Npc.textChatsTaiXiu = stringList;

        }else return;

    }
    @Builder
    public Npc(int id, String name, short x, short y, ArrayList<Item> wearing) {
        setId(id > ID_ADD ? id : id + ID_ADD);
        setUsername(name);
        setRole((byte) 0);
        setX(x);
        setY(y);
        setWearing(wearing);
        textChats = new ArrayList<>();
        //if(this.getId() == 2000000856){
            //autoChatBotTaiXiu.start();
        //}else{
            autoChatBot.start();
       // }

    }

    public void addChat(String chat) {
        textChats.add(chat);
    }


    @Override
    public void sendMessage(Message ms) {

    }
    public void NpcMove(Npc npc ,short x, short y){
        npc.setX(x);
        npc.setX(y);
        npc.setDirect((byte)2);
        npc.getMapService().move(npc);
    }

    ////pem bosss
    public void skill(Npc npc,byte id){
        List<User> players = npc.getZone().getPlayers();
        for (User player : players) {
            EffectService.createEffect()
                    .session(player.session)
                    .id(id)
                    .style((byte) 0)
                    .loopLimit((byte) 5)
                    .loop((short) 5)
                    .loopType((byte) 1)
                    .radius((short) 50)
                    .idPlayer(npc.getId())
                    .send();
        };
    }
}