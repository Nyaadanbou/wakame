package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.damage.DamageManagerApi;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.EntityDamageEvent;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Shadow
    public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    /**
     * 让此处if表达式的条件恒为false.
     * 实现即使伤害被无懈可击时间完全免疫, 也触发伤害事件.
     */
    @Redirect(
            method = "hurtServer",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/world/entity/LivingEntity;lastHurt:F",
                    ordinal = 0,
                    opcode = Opcodes.GETFIELD
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
        return DamageManagerApi.Impl.injectDamageLogic(event, instance.lastHurt, true);
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
        return DamageManagerApi.Impl.injectDamageLogic(event, instance.lastHurt, false);
    }

    /**
     * 移除原版“下落方块对头盔造成大量耐久度消耗”的机制.
     * 头盔对下落方块的减伤机制也被移除了, 见拓展函数 [EntityDamageEvent.removeUnusedDamageModifiers]
     */
    @Redirect(
            method = "actuallyHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 0
            )
    )
    private boolean redirectIsDamagesHelmet(DamageSource instance, TagKey<DamageType> damageTypeKey) {
        return false;
    }

    /**
     * 移除原版盔甲损失耐久度的机制.
     */
    @Redirect(
            method = "actuallyHurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 1
            )
    )
    private boolean redirectIsBypassesArmor(DamageSource instance, TagKey<DamageType> damageTypeKey) {
        return true;
    }
}

