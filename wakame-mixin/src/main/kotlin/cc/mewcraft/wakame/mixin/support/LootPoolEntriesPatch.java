package cc.mewcraft.wakame.mixin.support;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;

/**
 * 负责注册新的 {@link LootPoolEntryType}.
 */
public class LootPoolEntriesPatch {

    public static final LootPoolEntryType KOISH_ITEM = register(
            "koish:item", LootKoishItem.CODEC
    );

    public static void bootstrap() {
        // 调用该方法以触发 static initializer
    }

    private static LootPoolEntryType register(String id, MapCodec<? extends LootPoolEntryContainer> codec) {
        return Registry.register(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE, ResourceLocation.parse(id), new LootPoolEntryType(codec));
    }

}
