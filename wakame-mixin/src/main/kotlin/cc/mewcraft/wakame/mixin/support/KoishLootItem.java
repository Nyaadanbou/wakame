package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.api.Koish;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyori.adventure.key.Key;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 新增的战利品条目类型, 用于直接生成萌芽物品.
 * <p>
 * 本实现相当于新增了一个 {@code Singleton Entry}. 具体请参考 <a href="https://minecraft.wiki/w/Loot_table#Singleton_entry">Loot table</a>.
 */
@NullMarked
public class KoishLootItem extends LootPoolSingletonContainer {
    public static final MapCodec<KoishLootItem> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(entry -> entry.id))
                    .and(singletonFields(instance))
                    .apply(instance, KoishLootItem::new)
    );
    private final ResourceLocation id;

    private KoishLootItem(ResourceLocation id, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.id = id;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public LootPoolEntryType getType() {
        return ExtraLootPoolEntries.KOISH_ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> lootConsumer, LootContext context) {
        var namespace = id.getNamespace();
        var path = id.getPath();
        var player = getLootingPlayer(context);

        var nekoItem = Koish.get().getItemRegistry().getOrNull(Key.key(namespace, path));
        if (nekoItem == null) {
            LogUtils.getClassLogger().error("No item type with id: {}", id);
            return;
        }

        var bukkitItemStack = nekoItem.createItemStack(player);
        var mojangItemStack = CraftItemStack.unwrap(bukkitItemStack);

        lootConsumer.accept(mojangItemStack);
    }

    /**
     * 从 {@link LootContext} 中提取出应该得到该战利品的玩家.
     *
     * @param context 战利品的上下文
     * @return 应该得到战利品的玩家
     */
    private @Nullable CraftPlayer getLootingPlayer(LootContext context) {
        var thisEntity = context.getParameter(LootContextParams.THIS_ENTITY);
        if (thisEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.getBukkitEntity();
        }

        var lastDamagePlayer = context.getParameter(LootContextParams.LAST_DAMAGE_PLAYER);
        if (lastDamagePlayer instanceof ServerPlayer serverPlayer) {
            return serverPlayer.getBukkitEntity();
        }

        var attackingEntity = context.getParameter(LootContextParams.ATTACKING_ENTITY);
        if (attackingEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.getBukkitEntity();
        }

        return null;
    }

}