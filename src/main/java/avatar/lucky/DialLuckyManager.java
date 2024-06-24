/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.lucky;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class DialLuckyManager {

    public static final byte XU = 0;
    public static final byte LUONG = 1;
    public static final byte MIEN_PHI = 2;
    
    private static final DialLuckyManager instance = new DialLuckyManager();
    
    public static DialLuckyManager getInstance() {
        return instance;
    }
    
    private final List<DialLucky> list = new ArrayList<>();
    
    public DialLuckyManager() {
        add(new DialLucky(XU));
        add(new DialLucky(LUONG));
        add(new DialLucky(MIEN_PHI));
    }
    
    public void add(DialLucky dialLucky) {
        list.add(dialLucky);
    }
    
    public DialLucky find(byte type) {
        for (DialLucky dl : list) {
            if (dl.getType() == type) {
                return dl;
            }
        }
        return null;
    }
}
