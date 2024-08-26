package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ItemStackStrategy;
import io.papermc.paper.inventory.recipe.StackedContentsExtraMap;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StackedContentsExtraMap.class)
public abstract class MixinStackedContentsExtraMap {

    @Redirect(
            method = "<init>", // 构造函数
            at = @At(
                    value = "NEW",
                    target = "Lit/unimi/dsi/fastutil/objects/Object2IntOpenCustomHashMap;",
                    opcode = Opcodes.NEW
            )
    )
    public Object2IntOpenCustomHashMap<ItemStack> redirect(final Hash.Strategy<ItemStack> strategy) {
        return new Object2IntOpenCustomHashMap<>(ItemStackStrategy.TYPE_AND_TAG);
    }
}