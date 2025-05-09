package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomItemStackLinkedSet;
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
     * @author Nailm, Flandre
     * @reason 让 Koish 物品在 Shapeless 配方中只考虑 Koish 的唯一物品标识而忽略其他物品数据
     */
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "Lit/unimi/dsi/fastutil/objects/ObjectOpenCustomHashSet;",
                    opcode = Opcodes.NEW
            )
    )
    public ObjectOpenCustomHashSet<ItemStack> koish$ObjectOpenCustomHashSet(final Hash.Strategy<ItemStack> strategy) {
        return new ObjectOpenCustomHashSet<>(CustomItemStackLinkedSet.KOISH_STRATEGY);
    }
}