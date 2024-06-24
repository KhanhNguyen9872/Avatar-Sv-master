/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.model;

import avatar.item.Item;
import avatar.item.Part;
import avatar.item.PartManager;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class CreateChar {

    private static final CreateChar instance = new CreateChar();

    public static CreateChar getInstance() {
        return instance;
    }

    private final List<Part> listHair = new ArrayList<>();
    private final List<Part> listClothing = new ArrayList<>();
    private final List<Part> listPant = new ArrayList<>();

    public CreateChar() {
        init();
    }

    public void init() {
        List<Part> parts = PartManager.getInstance().getAvatarPart();
        for (Part p : parts) {
            if (p.getLevel() == 0 && p.getGender() <= 2) {
                switch (p.getZOrder()) {
                    case 50:
                        listHair.add(p);
                        break;
                    case 20:
                        listClothing.add(p);
                        break;
                    case 10:
                        listPant.add(p);
                        break;
                }
            }
        }
    }

    public boolean check(byte type, Item item) {
        switch (type) {
            case 0:
                return listHair.stream().anyMatch((t) -> t.getId() == item.getId());

            case 1:
                return listClothing.stream().anyMatch((t) -> t.getId() == item.getId());

            case 2:
                return listPant.stream().anyMatch((t) -> t.getId() == item.getId());
        }
        return false;
    }

    public boolean check(byte gender, List<Item> wearing) {
        Item pant = wearing.get(0);
        if (!CreateChar.this.check((byte) 2, pant)) {
            return false;
        }

        Item clothing = wearing.get(1);
        if (!CreateChar.this.check((byte) 1, clothing)) {
            return false;
        }
        if (wearing.get(2).getId() != 0) {
            return false;
        }

        if (wearing.get(3).getId() != 4) {
            return false;
        }
        Item hair = wearing.get(4);
        if (!CreateChar.this.check((byte) 0, hair)) {
            return false;
        }
        return true;
    }
}
