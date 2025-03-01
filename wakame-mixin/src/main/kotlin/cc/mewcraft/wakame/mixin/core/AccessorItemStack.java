package cc.mewcraft.wakame.mixin.core;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStack.class)
public interface AccessorItemStack {
    @Accessor("components")
    PatchedDataComponentMap getComponents();
}