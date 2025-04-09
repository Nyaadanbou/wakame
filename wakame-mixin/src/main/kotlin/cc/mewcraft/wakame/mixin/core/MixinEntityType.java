package cc.mewcraft.wakame.mixin.core;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityType.class)
public abstract class MixinEntityType {

    /**
     * 让 {@link cc.mewcraft.wakame.mixin.support.EntityTypeWrapper} 可以正常实例化.
     *
     * @author g2213swo
     */
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/DefaultedRegistry;createIntrusiveHolder(Ljava/lang/Object;)Lnet/minecraft/core/Holder$Reference;"
            )
    )
    private <T> Holder.Reference<T> redirectCreateIntrusiveHolder(DefaultedRegistry<T> registry, T object, @Local(argsOnly = true) EntityType.EntityFactory<?> factory) {
        if (factory == null) {
            return null; // factory == null 说明此时正在构建 EntityTypeWrapper
        }
        return registry.createIntrusiveHolder(object);
    }
}
