package cc.mewcraft.wakame.mixin.support;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

@NullMarked
public class EntityTypeWrapper<T extends Entity> extends EntityType<T> {
    private static final String MYTHICMOBS_NAMESPACE = "mythicmobs";
    private static final NamespacedKey PDC_KEY = new NamespacedKey("koish", "mob_id");

    public static final Codec<EntityType<?>> CODEC = ResourceLocation.CODEC.comapFlatMap(
            resourceLocation -> {
                if (resourceLocation.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                    return DataResult.success(BuiltInRegistries.ENTITY_TYPE.getValue(resourceLocation));
                } else {
                    return parse(resourceLocation);
                }
            },
            entityType -> {
                if (entityType instanceof EntityTypeWrapper<?> wrapper) {
                    return wrapper.id;
                }
                return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            }
    );

    private final ResourceLocation id;

    @Nullable
    private EntityType<T> delegate = null;

    private static <T extends Entity> DataResult<EntityType<T>> parse(ResourceLocation id) {
        if (id.getNamespace().equals(MYTHICMOBS_NAMESPACE)) {
            return DataResult.success(new EntityTypeWrapper<>(id));
        } else {
            return DataResult.error(() -> "Unsupported entity type: " + id);
        }
    }

    private EntityTypeWrapper(ResourceLocation id) {
        super(null, null, false, false, false, false, ImmutableSet.of(), EntityDimensions.scalable(0f, 0f), 0f, 0, 0, "", Optional.empty(), FeatureFlagSet.of());
        this.id = id;
    }

    public EntityType<T> getDelegate() {
        if (delegate == null) {
            delegate = (EntityType<T>) CraftEntityType.bukkitToMinecraft(MythicMobsBridgeProvider.get().getEntityType(PaperAdventure.asAdventure(id)));
        }
        return delegate;
    }

    @Override
    public @Nullable T spawn(ServerLevel world, @Nullable ItemStack stack, @Nullable Player player, BlockPos pos, EntitySpawnReason spawnReason, boolean alignPosition, boolean invertY) {
        return getDelegate().spawn(world, stack, player, pos, spawnReason, alignPosition, invertY);
    }

    @Override
    public @Nullable T spawn(ServerLevel worldserver, @Nullable ItemStack itemstack, @Nullable Player entityhuman, BlockPos blockposition, EntitySpawnReason entityspawnreason, boolean flag, boolean flag1, CreatureSpawnEvent.SpawnReason spawnReason) {
        return getDelegate().spawn(worldserver, itemstack, entityhuman, blockposition, entityspawnreason, flag, flag1, spawnReason);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSummon() {
        return getDelegate().canSummon();
    }

    @Override
    public boolean fireImmune() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canSpawnFarFromPlayer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MobCategory getCategory() {
        return MobCategory.MONSTER;
    }

    @Override
    public String getDescriptionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component getDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toShortString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ResourceKey<LootTable>> getDefaultLootTable() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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
        MythicMobsBridgeProvider.get().writeMobId(bukkitEntity, PaperAdventure.asAdventure(id));
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

    @Override
    public Holder.Reference<EntityType<?>> builtInRegistryHolder() {
        throw new UnsupportedOperationException();
    }
}
