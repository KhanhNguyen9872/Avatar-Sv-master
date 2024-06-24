/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.play;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class HouseItem {

    public short itemId;
    public short x;
    public short y;
    public byte rotate;

    public HouseItem() {
    }

    public HouseItem(int itemId, int x, int y, int rotate) {
        this.itemId = (short) itemId;
        this.x = (short) (x / 24);
        this.y = (short) (y / 24);
        this.rotate = (byte) rotate;
    }
}
