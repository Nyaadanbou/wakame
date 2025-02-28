package cc.mewcraft.wakame.mixin.core;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LootPoolEntries.class)
public interface InvokerLootPoolEntries {

    @Invoker("register")
    static LootPoolEntryType register(String id, MapCodec<? extends LootPoolEntryContainer> codec) {
        throw new AssertionError();
    }

}
