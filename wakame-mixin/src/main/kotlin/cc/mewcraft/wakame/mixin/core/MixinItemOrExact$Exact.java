package cc.mewcraft.wakame.mixin.core;

import io.papermc.paper.inventory.recipe.ItemOrExact;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ItemOrExact.Exact.class)
public abstract class MixinItemOrExact$Exact {

    // @Final
    // @Shadow
    // private ItemStack stack;
    //
    // /**
    //  * @author Nailm
    //  * @reason make Koish items invariant to their data components
    //  */
    // @Overwrite
    // public boolean matches(final ItemStack stack) {
    //     return CustomItemStack.equals(this.stack, stack);
    // }
    //
    // /**
    //  * @author Nailm
    //  * @reason make Koish items invariant to their data components
    //  */
    // @Overwrite
    // public boolean equals(final Object obj) {
    //     if (!(obj instanceof ItemOrExact.Exact(final ItemStack otherExact))) return false;
    //     return CustomItemStack.equals(this.stack, otherExact);
    // }
    //
    // /**
    //  * @author Nailm
    //  * @reason make Koish items invariant to their data components
    //  */
    // @Overwrite
    // public int hashCode() {
    //     return CustomItemStack.hashCode(this.stack);
    // }
}
