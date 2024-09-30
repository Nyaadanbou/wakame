package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.NekooProvider;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 新增的战利品条目类型, 用于直接生成萌芽物品.
 * <p>
 * 本实现相当于新增了一个 {@code Singleton Entry}. 具体请参考 <a href="https://minecraft.wiki/w/Loot_table#Singleton_entry">Loot table</a>.
 */
public class LootNekoItem extends LootPoolSingletonContainer {
    public static final MapCodec<LootNekoItem> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(ResourceLocation.CODEC.fieldOf("id").forGetter(entry -> entry.id))
                    .and(singletonFields(instance))
                    .apply(instance, LootNekoItem::new)
    );
    private final ResourceLocation id;

    private LootNekoItem(ResourceLocation id, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.id = id;
    }

    @Override
    public @NotNull LootPoolEntryType getType() {
        return LootPoolEntryInitializer.NEKO_ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> lootConsumer, @NotNull LootContext context) {
        var nekoo = NekooProvider.get();

        var namespace = id.getNamespace();
        var path = id.getPath();
        var player = getLootingPlayer(context);

        var bukkitItemStack = nekoo.createItemStack(namespace, path, player.getBukkitEntity());
        var nmsItemStack = CraftItemStack.unwrap(bukkitItemStack);

        lootConsumer.accept(nmsItemStack);
    }

    /**
     * 从 {@link LootContext} 中提取出应该得到该战利品的玩家.
     *
     * @param context 战利品的上下文
     * @return 应该得到战利品的玩家
     */
    private @Nullable ServerPlayer getLootingPlayer(LootContext context) {
        var thisEntity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (thisEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        var lastDamagePlayer = context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        if (lastDamagePlayer != null) {
            return (ServerPlayer) lastDamagePlayer;
        }

        var attackingEntity = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        if (attackingEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        return null;
    }
}