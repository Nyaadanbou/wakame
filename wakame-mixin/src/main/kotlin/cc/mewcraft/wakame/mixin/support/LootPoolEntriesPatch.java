package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.mixin.core.InvokerLootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;

/**
 * 负责注册新的 {@link LootPoolEntryType}.
 */
public class LootPoolEntriesPatch {

    public static final LootPoolEntryType KOISH_ITEM = InvokerLootPoolEntries.register("neko_item", LootKoishItem.CODEC);

    public static void bootstrap() {
        // 调用该方法以触发 static initializer
    }

}
