package avatar.handler;

import avatar.common.BossShopItem;
import avatar.constants.NpcName;
import avatar.item.PartManager;
import avatar.model.BossShop;
import avatar.model.Npc;
import avatar.model.User;
import avatar.network.Message;
import avatar.service.AvatarService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BossShopHandler {
    public static final byte SELECT_XU = 0;
    public static final byte SELECT_LUONG = 1;

    public static void displayUI(User us, byte type, int... itemIds) {
        AvatarService service = us.getAvatarService();
        List<Integer> itemIdList = IntStream.of(itemIds)
                .boxed()
                .collect(Collectors.toList());
        List<BossShopItem> upgradeItems = PartManager.getInstance().getUpgradeItems()
                .stream()
                .filter(upgradeItem -> itemIdList.contains(upgradeItem.getItem().getId()))
                .sorted(Comparator.comparingInt(item -> {
                    int index = itemIdList.indexOf(item.getItem().getId());
                    return index == -1 ? Integer.MAX_VALUE : index;
                }))
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

    public static void handle() {

    }
}
