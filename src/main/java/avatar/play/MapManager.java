/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.play;

import java.util.ArrayList;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class MapManager {
    private static final MapManager instance = new MapManager();

    public static MapManager getInstance() {
        return instance;
    }
    
    private final ArrayList<Map> maps = new ArrayList<>();

    public void add(Map map) {
        synchronized (maps) {
            maps.add(map);
        }
    }

    public void remove(Map map) {
        synchronized (maps) {
            maps.remove(map);
        }
    }

    public Map find(int id) {
        synchronized (maps) {
            for (Map map : maps) {
                if (map.getId() == id) {
                    return map;
                }
            }
        }
        return null;
    }
    
    public void update() {
        maps.forEach((t) -> {
            t.update();
        });
    }
}
