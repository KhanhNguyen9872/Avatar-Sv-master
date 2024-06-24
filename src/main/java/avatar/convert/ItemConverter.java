/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.convert;

import avatar.item.Item;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
public class ItemConverter {
    
    private static final ItemConverter instance = new ItemConverter();
    
    public static ItemConverter getInstance() {
        return instance;
    }
    
    public Item newItem(Item oldItem) {
        return Item.builder()
                .id(oldItem.getId())
                .quantity(oldItem.getQuantity())
                .expired(oldItem.getExpired())
                .build();
    }
}
