package cc.mewcraft.wakame.mixin.core;

import cc.mewcraft.wakame.mixin.support.EntityTypeWrapper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 将 {@link EntityTypeWrapper} 解包为其委托的原版 {@link EntityType},
 * 使得 {@link SpawnPlacements} 的静态 Map 查找能够正确匹配到注册的生成规则.
 *
 * <p>如果不做这个处理, {@link EntityTypeWrapper} 实例不在 {@code DATA_BY_TYPE} 中,
 * 导致 {@code getPlacementType} 返回 {@code NO_RESTRICTIONS},
 * {@code checkSpawnRules} 无条件返回 {@code true},
 * 从而使怪物在任意位置 (包括半空中、方块内) 生成.</p>
 *
 * @author Copilot
 */
@Mixin(SpawnPlacements.class)
public abstract class MixinSpawnPlacements {

    @ModifyVariable(
            method = "getPlacementType",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private static EntityType<?> koish$unwrapGetPlacementType(EntityType<?> entityType) {
        if (entityType instanceof EntityTypeWrapper<?> wrapper) {
            return wrapper.getDelegate();
        }
        return entityType;
    }

    @ModifyVariable(
            method = "isSpawnPositionOk",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private static EntityType<?> koish$unwrapIsSpawnPositionOk(EntityType<?> entityType) {
        if (entityType instanceof EntityTypeWrapper<?> wrapper) {
            return wrapper.getDelegate();
        }
        return entityType;
    }

    @ModifyVariable(
            method = "getHeightmapType",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private static EntityType<?> koish$unwrapGetHeightmapType(EntityType<?> entityType) {
        if (entityType instanceof EntityTypeWrapper<?> wrapper) {
            return wrapper.getDelegate();
        }
        return entityType;
    }

    @ModifyVariable(
            method = "checkSpawnRules",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true
    )
    private static EntityType<?> koish$unwrapCheckSpawnRules(EntityType<?> entityType) {
        if (entityType instanceof EntityTypeWrapper<?> wrapper) {
            return wrapper.getDelegate();
        }
        return entityType;
    }
}

