package cc.mewcraft.wakame.mixin.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BundleContents.class)
public interface InvokerBundleContents {

    @Accessor("items")
    List<ItemStack> items();
}
