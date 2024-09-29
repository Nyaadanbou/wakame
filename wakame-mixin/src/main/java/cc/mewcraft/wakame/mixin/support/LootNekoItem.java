package cc.mewcraft.wakame.mixin.support;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

public class LootNekoItem extends LootPoolSingletonContainer {
    public static final MapCodec<LootNekoItem> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("name").forGetter(entry -> entry.item))
                    .and(singletonFields(instance))
                    .apply(instance, LootNekoItem::new)
    );
    private final Holder<Item> item;

    private LootNekoItem(Holder<Item> item, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.item = item;
        //new DuckClass(); // TODO ClassNotFound
    }

    @Override
    public @NotNull LootPoolEntryType getType() {
        return LootPoolEntryInitializer.NEKO_ITEM;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> lootConsumer, @NotNull LootContext context) {
        lootConsumer.accept(new ItemStack(this.item));
    }

    public static LootPoolSingletonContainer.Builder<?> lootTableItem(ItemLike drop) {
        return simpleBuilder(
                (weight, quality, conditions, functions) -> new LootNekoItem(drop.asItem().builtInRegistryHolder(), weight, quality, conditions, functions)
        );
    }
}
