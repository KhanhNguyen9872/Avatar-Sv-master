/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.model;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@Getter
@Setter
public class Command {

    private int icon;
    private String name;
    private short anthor;
    private byte type;

    public Command(String name, int icon) {
        this.icon = icon;
        this.name = name;
    }

    public Command(short anthor, String name, int icon, byte type) {
        this(name, icon);
        this.anthor = anthor;
        this.type = type;
    }
}
