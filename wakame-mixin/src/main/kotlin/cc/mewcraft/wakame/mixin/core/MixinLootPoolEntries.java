package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ExtraLootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootPoolEntries.class)
public abstract class MixinLootPoolEntries {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onInit(CallbackInfo ci) {
        // 注册一个新的 LootPoolEntryType, 以便直接在战利品表中写入萌芽物品.
        // 对应数据包中的 “singleton entry” 概念, 具体参考 https://minecraft.wiki/w/Loot_table#Singleton_entry
        ExtraLootPoolEntries.bootstrap();
    }

}
