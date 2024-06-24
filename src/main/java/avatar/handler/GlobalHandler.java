package avatar.handler;

import avatar.constants.NpcName;
import avatar.item.Item;
import avatar.model.Menu;
import avatar.model.Npc;
import avatar.model.User;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;


import avatar.network.Message;
import avatar.play.NpcManager;
import avatar.play.Zone;
import avatar.server.Avatar;
import avatar.server.ServerManager;
import avatar.server.UserManager;

public class GlobalHandler {
    private User us;

    public GlobalHandler(User user) {
        this.us = user;
    }


    public void handleOptionMenu(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        byte menuId = ms.reader().readByte();
        byte select = ms.reader().readByte();
        System.out.println("userId = " + userId + ", menuId = " + menuId + ", select = " + select);
        if (userId >= 2000000000 || userId == 7) {
            NpcHandler.handlerAction(this.us, userId, menuId, select);
            return;
        } else {
            menuOptionHandle(userId, menuId, select);
        }
    }

    private void menuOptionHandle(int npcId, byte menuId, byte select) {
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

    public void comingSoon() {
        us.getAvatarService().serverDialog("Chức năng đang được xây dựng, vui lòng thử lại sau");
    }

    public void handleSicbo() {

    }

    public void handleTextBox(Message ms) throws IOException {
        int userId = ms.reader().readInt();
        byte menuId = ms.reader().readByte();
        String text = ms.reader().readUTF();
        List<User> lst = UserManager.users;
        switch (menuId) {
            case 7:
                try {
                    short itemCode = Short.parseShort(text);
                    Item item = new Item(itemCode, -1, 0);
                    this.us.addItemToChests(item);
                    us.getAvatarService().serverDialog("added " + item.getPart().getName() + " into my chests");
                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("invalid input, item code must be number");
                }
                break;
            case 8:
                try {
                    if (us.getId() == 7) {
                        String noidung = text.toString();
                        for (int i = 0; i < lst.stream().count(); i++) {
                            lst.get(i).getAvatarService().serverInfo(noidung);
                        }
                    }

                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("invalid input, item code must be number");
                }
                break;
            case 9:
                try {
                    if (us.getId() == 7) {
                        byte weather = Byte.parseByte(text);
                        us.getAvatarService().weather(weather);
                    }

                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("invalid input, item code must be number");
                }
                break;
            case 10:
                try {
                    if (us.getId() == 7) {
                        if (Integer.parseInt(text) == 1)
                            for (int i = 0; i < lst.stream().count(); i++) {
                                lst.get(i).getAvatarService().serverDialog("server sẽ bảo trì sau 2 phút.vui lòng off để tránh mất đồ");
                            }
                    }

                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("invalid input, item code must be number");
                }
                break;
            case 11:
                try {
                    if (us.getId() == 7) {
                        if (Integer.parseInt(text) == 1) {
                            for (int i = 0; i < lst.stream().count(); i++) {
                                lst.get(i).getAvatarService().serverInfo((String.format("ad : thành phố  %s. có %d  đang online. chúc mọi người vui vẻ", ServerManager.cityName, ServerManager.clients.size())));
                            }
                        }
                    }

                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("invalid input, item code must be number");
                }
                break;
            case 12:
                try {
                    if (us.datCuoc == 0) {
                        long xu = Long.parseLong(text);
                        if (us.getXu() >= xu) {
                            if (xu < 1000000010) {
                                us.getAvatarService().serverDialog("Bạn Đã  Cược Tài " + xu + " Xu Thành Công!");
                                us.updateXu(-xu);
                                us.getAvatarService().updateMoney(0);
                                us.datCuoc = 1;
                                us.SicboNhanTien = false;
                                us.tienCuoc = xu;
                            } else
                                us.getAvatarService().serverDialog("Đặt nhỏ hơn 100met Thôi");
                        } else {
                            us.getAvatarService().serverDialog("Đặt nhỏ hơn " + us.getXu() + " Xu thôi định bịp tao hả!");
                        }
                    } else if (us.datCuoc == 1) {
                        us.getAvatarService().serverDialog("Bạn Đã Cược Tài " + us.tienCuoc + " Rồi!");
                    } else if (us.datCuoc == 2) {
                        us.getAvatarService().serverDialog("Bạn Đã Cược xỉu " + us.tienCuoc + " Rồi!");
                    }
                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("Vui Lòng Nhập Số Để Cược");
                }
                break;
            case 13:
            case 15:
                try {
                    if (us.datCuoc == 0) {
                        long xu = Long.parseLong(text);
                        if (us.getXu() >= xu) {
                            if (xu < 1000000010) {
                                us.getAvatarService().serverDialog("Bạn Đã  Cược xỉu " + xu + " Xu Thành Công!");
                                us.updateXu(-xu);
                                us.getAvatarService().updateMoney(0);
                                us.datCuoc = 2;
                                us.SicboNhanTien = false;
                                us.tienCuoc = xu;
                            } else
                                us.getAvatarService().serverDialog("Đặt nhỏ hơn 100met Thôi");
                        } else {
                            us.getAvatarService().serverDialog("Đặt nhỏ hơn " + us.getXu() + " Xu thôi định bịp tao hả!");
                        }
                    } else if (us.datCuoc == 1) {
                        us.getAvatarService().serverDialog("Bạn Đã Cược Tài " + us.tienCuoc + " Rồi!");
                    } else if (us.datCuoc == 2) {
                        us.getAvatarService().serverDialog("Bạn Đã Cược xỉu " + us.tienCuoc + " Rồi!");
                    }
                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("Vui Lòng Nhập Số Để Cược");
                }
                break;
            case 14:
                try {
                    if (us.datCuoc == 0) {
                        long xu = Long.parseLong(text);
                        if (us.getXu() >= xu) {
                            if (xu < 1000000010) {
                                us.getAvatarService().serverDialog("Bạn Đã  Cược Tài " + xu + " Xu Thành Công!");
                                us.updateXu(-xu);
                                us.getAvatarService().updateMoney(0);
                                us.datCuoc = 1;
                                us.SicboNhanTien = false;
                                us.tienCuoc = xu;
                            } else
                                us.getAvatarService().serverDialog("Đặt nhỏ hơn 100met Thôi");
                        } else {
                            us.getAvatarService().serverDialog("Đặt nhỏ hơn " + us.getXu() + " Xu thôi định bịp tao hả!");
                        }
                    } else if (us.datCuoc == 1) {
                        us.getAvatarService().serverDialog("Bạn Đã Cược Tài " + us.tienCuoc + " Rồi!");
                    } else if (us.datCuoc == 2) {
                        us.getAvatarService().serverDialog("Bạn Đã Cược xỉu " + us.tienCuoc + " Rồi!");
                    }
                } catch (NumberFormatException e) {
                    us.getAvatarService().serverDialog("Vui Lòng Nhập Số Để Cược");
                }
                break;
        }

    }

    private void sendCityMap() throws IOException {
        String folder = "res/map/";
        byte[] data = Avatar.getFile(folder + "cityMap.dat");
        byte[] image = Avatar.getFile(folder + "cityMap.png");
        byte[] map27 = Avatar.getFile(folder + "27.dat");
        byte[] map_bg = Avatar.getFile(folder + "bg/27.png");
        Message ms = new Message(-92);
        DataOutputStream ds = ms.writer();
        ds.writeByte(1);
        ds.writeInt(image.length);
        ds.write(image);
        ds.writeInt(data.length);
        ds.writeByte(34);
        ds.write(data);
        short[] idImg = new short[]{821, 827, 850};
        String[] doorName = new String[]{"Sân Bay", "Bãi Biển", "Trung Tâm Giải Trí"};
        byte[] x = new byte[]{23, 9, 21};
        byte[] y = new byte[]{7, 13, 18};
        ds.writeByte(idImg.length);
        int i = 0;
        while (i < idImg.length) {
            ds.writeByte(i);
            ds.writeShort(idImg[i]);
            ds.writeUTF(doorName[i]);
            ds.writeByte(x[i]);
            ds.writeByte(y[i]);
            ++i;
        }
        ds.flush();
        this.us.sendMessage(ms);
        // ms = new Message(-93);
        // ds = ms.writer();
        // ds.writeByte(27);
// ds.writeByte(1);
        // ds.writeShort(306);
        // ds.writeByte(34);
        // ds.writeShort(map27.length);
        // ds.write(map27);
        // short[] arr = new short[] { 828, -1, 835, 853, 852, 836, 832, 832, 851, 833,
        // 837, 842, 843, -1, -1 };
        // ds.writeByte(arr.length);
        // int j = 0;
        // while (j < arr.length) {
        // ds.writeShort(arr[j]);
        // ++j;
        // }
        // ds.writeShort(map_bg.length);
        // ds.write(map_bg);
        // try {
        // int mapId = 27;
        // String GET_MAP_ITEM_TYPE = "SELECT * FROM `map_item_type` WHERE `map_id` =
        // ?";
        // PreparedStatement ps =
// DbManager.getInstance().getConnectionForGame().prepareStatement(GET_MAP_ITEM_TYPE,
        // 1005, 1007);
        // ps.setInt(1, mapId);
        // ResultSet res = ps.executeQuery();
        // if (res != null) {
        // ds.writeShort(204);
        // res.last();
        // int rows = res.getRow();
        // ds.writeByte(rows);
        // res.beforeFirst();
        // while (res.next()) {
        // ds.writeByte(res.getByte("id_type"));
        // ds.writeShort(res.getShort("id_img"));
        // ds.writeByte(res.getByte("icon_id"));
        // ds.writeShort(res.getShort("dx"));
        // ds.writeShort(res.getShort("dy"));
        // JSONArray av_position = (JSONArray)
        // JSONValue.parse(res.getString("av_position"));
        // ds.writeByte(av_position.size());
        // int k = 0;
        // while (k < av_position.size()) {
        // JSONObject av_position_element = (JSONObject) av_position.get(k);
        // ds.writeByte(((Long) av_position_element.get("x")).shortValue());
        // ds.writeByte(((Long) av_position_element.get("y")).shortValue());
        // ++k;
        // }
        // }
        // byte[] mapItemA = new byte[] { 26, 27, 28, 29, 31, 64 };
        // byte[] byArray = new byte[6];
        // byArray[5] = 4;
        // byte[] mapItemB = byArray;
        // byte[] mapItemC = new byte[] { 2, 7, 27, 20, 33, 15 };
        // byte[] byArray2 = new byte[6];
        // byArray2[0] = 1;
        // byArray2[1] = 1;
        // byArray2[2] = 1;
        // byArray2[3] = 1;
        // byArray2[4] = 1;
        // byte[] mapItemD = byArray2;
        // ds.writeByte(mapItemA.length);
        // int l = 0;
        // while (l < mapItemA.length) {
        // ds.writeByte(mapItemA[l]);
        // ds.writeByte(mapItemB[l]);
        // ds.writeByte(mapItemC[l]);
        // ds.writeByte(mapItemD[l]);
        // ++l;
        // }
        // } else {
        // ds.writeShort(0);
        // }
        // } catch (SQLException e) {
        // e.printStackTrace();
        // }
        // ds.flush();
        // this.us.sendMessage(ms);
    }
}