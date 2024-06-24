/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.model;

import java.util.List;

import lombok.*;

/**
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@Getter
@Setter
@Builder
public class Menu {

    private int id;
    private String name;
    private List<Menu> menus;
    private Runnable action;
    private String npcName;
    private String npcChat;


    public void addMenu(Menu menu) {
        this.menus.add(menu);
    }

    public boolean isMenu() {
        return this.menus != null && this.menus.size() > 0;
    }

    public void perform() {
        if (action != null) {
            action.run();
        }
    }
}
