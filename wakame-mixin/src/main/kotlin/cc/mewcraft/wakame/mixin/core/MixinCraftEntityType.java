package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.EntityTypeWrapper;
import net.minecraft.world.entity.EntityType;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftEntityType.class)
public abstract class MixinCraftEntityType {

    /**
     * 如果是 {@link EntityTypeWrapper} 则返回其委托.
     *
     * @author g2213swo
     */
    @ModifyVariable(
            method = "minecraftToBukkit",
            at = @At(value = "HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private static EntityType<?> modifyMinecraftVariable(EntityType<?> original) {
        if (original instanceof EntityTypeWrapper<?> wrapper) {
            return wrapper.getDelegate();
        }
        return original;
    }
}
