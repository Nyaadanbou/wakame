package cc.mewcraft.wakame.mixin.support;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * 这是一个封装了 MythicMobs 怪物类型的 {@link EntityType}.
 *
 * @param <T> Minecraft 内置实体的类型
 */
@NullMarked
public class EntityTypeWrapper<T extends Entity> extends EntityType<T> {

    public static final Codec<EntityType<?>> CODEC = Identifier.CODEC.comapFlatMap(EntityTypeWrapper::encode, EntityTypeWrapper::decode);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String NAMESPACE = "mythicmobs";

    private static <T extends Entity> DataResult<EntityType<T>> parse(Identifier id) {
        if (id.getNamespace().equals(NAMESPACE)) {
            return DataResult.success(new EntityTypeWrapper<>(id));
        } else {
            return DataResult.error(() -> "Unsupported entity type: " + id);
        }
    }

    private static DataResult<? extends EntityType<?>> encode(Identifier id) {
        if (id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return DataResult.success(BuiltInRegistries.ENTITY_TYPE.getValue(id));
        } else {
            return parse(id);
        }
    }

    private static Identifier decode(EntityType<?> entityType) {
        if (entityType instanceof EntityTypeWrapper<?> wrapper) {
            return wrapper.id;
        } else {
            return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        }
    }

    //

    private final Identifier id;
    @Nullable
    private EntityType<T> delegate;

    private EntityTypeWrapper(Identifier id) {
        super(
                null,
                null,
                false,
                false,
                false,
                false,
                ImmutableSet.of(),
                EntityDimensions.scalable(0f, 0f),
                0f,
                0,
                0,
                "",
                Optional.empty(),
                FeatureFlagSet.of(),
                false
        );
        this.id = id;

        // 注册当前实例到全局 EntityTypeWrapperObjects, 用于之后热更新 this.delegate
        EntityTypeWrapperObjects.INSTANCE.register(this);
    }

    public Identifier getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    public EntityType<T> getDelegate() {
        if (this.delegate == null) {
            // 直接从 MythicBootstrapBridge 获取 NMS EntityType, 避免在 Bootstrap 阶段调用 CraftBukkit 还未初始化的代码
            // 如果 MythicMobs 生物配置文件写错了 EntityType, 这里会抛出 RuntimeException 来终止服务端启动, 这是符合预期的
            EntityType<T> entityType = (EntityType<T>) MythicBootstrapBridge.INSTANCE.getEntityType(id);
            if (entityType != null) {
                this.delegate = entityType;
            } else {
                LOGGER.error("Could not find MythicMobs entity type {}. Fix you datapack(s) or MythicMobs configs as soon as possible. Fallback to BAT to avoid crash.", id);
                this.delegate = (EntityType<T>) EntityType.BAT;
            }
        }
        return this.delegate;
    }

    @SuppressWarnings("unchecked")
    public void setDelegate(EntityType<?> delegate) {
        this.delegate = (EntityType<T>) delegate;
    }

    @Override
    public @Nullable T spawn(ServerLevel world, BlockPos pos, EntitySpawnReason reason) {
        return getDelegate().spawn(world, pos, reason);
    }

    @Override
    public @Nullable T spawn(ServerLevel worldserver, BlockPos blockposition, EntitySpawnReason entityspawnreason, CreatureSpawnEvent.SpawnReason spawnReason) {
        return getDelegate().spawn(worldserver, blockposition, entityspawnreason, spawnReason);
    }

    @Override
    public @Nullable T spawn(ServerLevel world, @Nullable Consumer<T> afterConsumer, BlockPos pos, EntitySpawnReason reason, boolean alignPosition, boolean invertY) {
        return getDelegate().spawn(world, afterConsumer, pos, reason, alignPosition, invertY);
    }

    @Override
    public @Nullable T spawn(ServerLevel worldserver, @Nullable Consumer<T> consumer, BlockPos blockposition, EntitySpawnReason entityspawnreason, boolean flag, boolean flag1, CreatureSpawnEvent.SpawnReason spawnReason) {
        return getDelegate().spawn(worldserver, consumer, blockposition, entityspawnreason, flag, flag1, spawnReason);
    }

    @Override
    public @Nullable T create(ServerLevel world, @Nullable Consumer<T> afterConsumer, BlockPos pos, EntitySpawnReason reason, boolean alignPosition, boolean invertY) {
        return getDelegate().create(world, afterConsumer, pos, reason, alignPosition, invertY);
    }

    @Override
    public boolean canSerialize() {
        return getDelegate().canSerialize();
    }

    @Override
    public boolean canSummon() {
        return getDelegate().canSummon();
    }

    @Override
    public boolean fireImmune() {
        return getDelegate().fireImmune();
    }

    @Override
    public boolean canSpawnFarFromPlayer() {
        return getDelegate().canSpawnFarFromPlayer();
    }

    @Override
    public MobCategory getCategory() {
        return getDelegate().getCategory();
    }

    @Override
    public String getDescriptionId() {
        return getDelegate().getDescriptionId();
    }

    @Override
    public Component getDescription() {
        return getDelegate().getDescription();
    }

    @Override
    public String toString() {
        return getDelegate().toString();
    }

    @Override
    public String toShortString() {
        return getDelegate().toShortString();
    }

    @Override
    public Optional<ResourceKey<LootTable>> getDefaultLootTable() {
        return getDelegate().getDefaultLootTable();
    }

    @Override
    public float getWidth() {
        return getDelegate().getWidth();
    }

    @Override
    public float getHeight() {
        return getDelegate().getHeight();
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return getDelegate().requiredFeatures();
    }

    @Override
    public boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return getDelegate().isEnabled(enabledFeatures);
    }

    @Override
    public @Nullable T create(Level world, EntitySpawnReason reason) {
        T entity = getDelegate().create(world, reason);
        if (entity == null)
            return null;

        CraftEntity bukkitEntity = entity.getBukkitEntity();

        // MythicMobs 5.8.2:
        // 在实体被创建时, MythicMobs 会识别这里写入的数据, 将实体变成对应的 MythicMobs 实体
        MythicPluginBridge.Impl.writeIdMark(bukkitEntity, PaperAdventure.asAdventure(id));

        return entity;
    }

    @Override
    public AABB getSpawnAABB(double x, double y, double z) {
        return getDelegate().getSpawnAABB(x, y, z);
    }

    @Override
    public boolean isBlockDangerous(BlockState state) {
        return getDelegate().isBlockDangerous(state);
    }

    @Override
    public EntityDimensions getDimensions() {
        return getDelegate().getDimensions();
    }

    @Override
    public int clientTrackingRange() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int updateInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean trackDeltas() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean is(TagKey<EntityType<?>> tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean is(HolderSet<EntityType<?>> entityTypeEntryList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable T tryCast(Entity obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends Entity> getBaseClass() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("deprecation")
    @Override
    public Holder.Reference<EntityType<?>> builtInRegistryHolder() {
        throw new UnsupportedOperationException();
    }
}
