package avatar.common;

import avatar.item.Item;
import avatar.model.BossShop;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@SuperBuilder
public abstract class BossShopItem {
    private int id;
    private int itemRequest;
    private Item item;

    public abstract String initDialog(BossShop bossShop);

}

