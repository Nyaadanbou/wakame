package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.LootPoolEntriesPatch;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootPoolEntries.class)
public interface MixinLootPoolEntries {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        // 注册一个新的 LootPoolEntryType, 以便直接在战利品表中写入萌芽物品.
        // 对应数据包中的 “singleton entry” 概念, 具体参考 https://minecraft.wiki/w/Loot_table#Singleton_entry
        LootPoolEntriesPatch.bootstrap();
    }

    @Invoker("register")
    static LootPoolEntryType register(String id, MapCodec<? extends LootPoolEntryContainer> codec) {
        throw new AssertionError();
    }

}
