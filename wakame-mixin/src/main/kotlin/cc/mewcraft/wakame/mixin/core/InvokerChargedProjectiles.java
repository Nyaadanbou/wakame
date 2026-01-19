package cc.mewcraft.wakame.mixin.core;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChargedProjectiles.class)
public interface InvokerChargedProjectiles {

    @Accessor("items")
    List<ItemStack> items();
}
