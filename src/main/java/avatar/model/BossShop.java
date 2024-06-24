package avatar.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BossShop {
    private byte typeShop;
    private int idBoss;
    private byte idShop;
    private String name;
}
