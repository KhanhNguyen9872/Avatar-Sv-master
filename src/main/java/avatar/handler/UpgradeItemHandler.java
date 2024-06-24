package avatar.handler;

import avatar.constants.NpcName;
import avatar.common.BossShopItem;
import avatar.item.PartManager;
import avatar.model.*;
import avatar.service.AvatarService;
import java.util.List;
import java.util.stream.Collectors;

//@Log4j2
public class UpgradeItemHandler {
    public static final byte SELECT_XU = 0;
    public static final byte SELECT_LUONG = 1;

    public static void doShowUpgradeItems(User us, byte type, int from, int to) {
        AvatarService service = us.getAvatarService();
        List<BossShopItem> upgradeItems = PartManager.getInstance().getUpgradeItems()
                .stream()
                .filter(
                        upgradeItem -> upgradeItem.getItem().getId() >= from && upgradeItem.getItem().getId() <= to)
                .collect(Collectors.toList());
        us.setBossShopItems(upgradeItems);
        service.openUIBossShop(
                BossShop.builder()
                        .idBoss(NpcName.THO_KIM_HOAN + Npc.ID_ADD)
                        .idShop((byte) type)
                        .typeShop((byte) 0)
                        .name("Nâng cấp")
                        .build()
                , upgradeItems);
    }

}
