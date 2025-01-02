package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.ItemStackStrategy;
import io.papermc.paper.inventory.recipe.StackedContentsExtrasMap;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StackedContentsExtrasMap.class)
public abstract class MixinStackedContentsExtrasMap {

    /**
     * @author Nailm, Flandre, g2213swo
     * @reason 让原版无序合成完全支持萌芽物品
     */
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectOpenCustomHashSet;",
                    opcode = Opcodes.NEW
            )
    )
    public ObjectOpenCustomHashSet<ItemStack> redirect(final Hash.Strategy<ItemStack> strategy) {
        return new ObjectOpenCustomHashSet<>(ItemStackStrategy.CUSTOM_STRATEGY);
    }
}