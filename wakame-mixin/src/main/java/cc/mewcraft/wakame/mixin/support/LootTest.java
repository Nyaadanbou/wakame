package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.DummyClass;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * 用于测试能不能新增加一个 {@link LootPoolEntryType}.
 */
public class LootTest extends LootPoolSingletonContainer {
    public static final MapCodec<LootTest> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("name").forGetter(entry -> entry.item))
                    .and(singletonFields(instance))
                    .apply(instance, LootTest::new)
    );
    private final Holder<Item> item;

    private LootTest(Holder<Item> item, int weight, int quality, List<LootItemCondition> conditions, List<LootItemFunction> functions) {
        super(weight, quality, conditions, functions);
        this.item = item;
    }

    @Override
    public @NotNull LootPoolEntryType getType() {
        return LootPoolEntryInitializer.TEST;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> lootConsumer, @NotNull LootContext context) {
        lootConsumer.accept(new ItemStack(this.item));
        System.out.println(DummyClass.DUMMY + " is called from System Classloader!");
    }
}
