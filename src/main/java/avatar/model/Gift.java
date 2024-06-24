/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avatar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author kitakeyos - Hoàng Hữu Dũng
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class Gift {

    private int id;
    private byte type;
    private int xu;
    private int xp;
    private int luong;
    private int expireDay;
}
