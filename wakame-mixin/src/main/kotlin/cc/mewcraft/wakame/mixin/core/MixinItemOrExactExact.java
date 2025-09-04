package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.CustomItemStack;
import io.papermc.paper.inventory.recipe.ItemOrExact;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemOrExact.Exact.class)
public abstract class MixinItemOrExactExact {

    @Final
    @Shadow
    private ItemStack stack;

    /**
     * @author Nailm
     * @reason make custom items invariant to their data components
     */
    @Overwrite
    public boolean matches(final ItemStack stack) {
        return CustomItemStack.equals(this.stack, stack);
    }

    /**
     * @author Nailm
     * @reason make custom items invariant to their data components
     */
    @Overwrite
    public boolean equals(final Object obj) {
        if (!(obj instanceof ItemOrExact.Exact(final ItemStack otherExact))) return false;
        return CustomItemStack.equals(this.stack, otherExact);
    }

    /**
     * @author Nailm
     * @reason make custom items invariant to their data components
     */
    @Overwrite
    public int hashCode() {
        return CustomItemStack.hashCode(this.stack);
    }
}
