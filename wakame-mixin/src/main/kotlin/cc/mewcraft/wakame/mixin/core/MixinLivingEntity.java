package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.damage.DamageManagerApi;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    /**
     * 让此处if表达式的条件恒为false.
     * 实现即使伤害被无懈可击时间完全免疫, 也触发伤害事件.
     */
    @Redirect(
            method = "hurtServer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;lastHurt:F",
                    ordinal = 0
            )
    )
    private float disableAmountCheckBeforeCallEvent(LivingEntity instance) {
        return -Float.MAX_VALUE;
    }

    /**
     * 插入无懈可击期间自定义的伤害逻辑.
     */
    @Redirect(
            method = "hurtServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;computeAmountFromEntityDamageEvent(Lorg/bukkit/event/entity/EntityDamageEvent;)F",
                    ordinal = 0
            )
    )
    private float redirectComputeAmountDuringInvulnerable(LivingEntity instance, EntityDamageEvent event) {
        return DamageManagerApi.Companion.getInstance().injectDamageLogic(event, true);
    }

    /**
     * 插入非无懈可击期间自定义的伤害逻辑.
     */
    @Redirect(
            method = "hurtServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;computeAmountFromEntityDamageEvent(Lorg/bukkit/event/entity/EntityDamageEvent;)F",
                    ordinal = 1
            )
    )
    private float redirectComputeAmountDuringNotInvulnerable(LivingEntity instance, EntityDamageEvent event) {
        return DamageManagerApi.Companion.getInstance().injectDamageLogic(event, false);
    }

}

