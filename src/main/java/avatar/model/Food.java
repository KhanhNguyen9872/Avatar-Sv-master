/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@AllArgsConstructor
@Builder
@Getter
public class Food {

    private int id;
    private String name;
    private String description;
    private int shop;
    private int icon;
    private int price;
    private int percentHelth;
}
