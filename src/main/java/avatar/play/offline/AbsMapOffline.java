/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.play.offline;

import avatar.model.Npc;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public abstract class AbsMapOffline {

    @Getter
    @Setter
    private int id;

    @Getter
    private List<Npc> npcs = new ArrayList<>();

    public AbsMapOffline(int id) {
        setId(id);
        init();
    }

    public abstract void init();

    public void addNpc(Npc npc) {
        synchronized (npcs) {
            npcs.add(npc);
        }
    }

    public void removeNpc(Npc npc) {
        synchronized (npcs) {
            npcs.remove(npc);
        }
    }
}
