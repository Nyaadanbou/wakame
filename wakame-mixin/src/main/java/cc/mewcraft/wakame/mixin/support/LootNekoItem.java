package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.Nekoo;
import cc.mewcraft.wakame.NekooProvider;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

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
        Nekoo nekoo = NekooProvider.get();

        String namespace = id.getNamespace();
        String path = id.getPath();
        Player player = getLootingPlayer(context);

        var bukkitItemStack = nekoo.createItemStack(namespace, path, player);
        var nmsItemStack = CraftItemStack.unwrap(bukkitItemStack);

        lootConsumer.accept(nmsItemStack);
    }

    /**
     * 从 {@link LootContext} 中提取出应该得到该战利品的玩家.
     *
     * @param context 战利品的上下文
     * @return 应该得到战利品的玩家
     */
    private @Nullable Player getLootingPlayer(LootContext context) {
        // TODO 分析所有可能的情况, 从 loot context 中提取出应该得到该战利品的 player
        return null;
    }
}