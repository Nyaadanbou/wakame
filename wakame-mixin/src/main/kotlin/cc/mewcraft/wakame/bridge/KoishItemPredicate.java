package cc.mewcraft.wakame.bridge;

import cc.mewcraft.wakame.bridge.codec.AdventureCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.kyori.adventure.key.Key;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

public record KoishItemPredicate(Key item, MinMaxBounds.Ints count) implements Predicate<ItemStack> {
    public static final Codec<KoishItemPredicate> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            AdventureCodecs.KEY_WITH_MINECRAFT_NAMESPACE.fieldOf("item").forGetter(KoishItemPredicate::item),
                            MinMaxBounds.Ints.CODEC.optionalFieldOf("count", MinMaxBounds.Ints.ANY).forGetter(KoishItemPredicate::count)
                    )
                    .apply(instance, KoishItemPredicate::new)
    );

    @Override
    public boolean test(ItemStack stack) {
        Key typeId = KoishItemBridge.Impl.getTypeId(stack);
        return Objects.equals(typeId, this.item) && this.count.matches(stack.getCount());
    }

    public static class Builder {
        private Key item;
        private MinMaxBounds.Ints count = MinMaxBounds.Ints.ANY;

        public static ItemPredicate.Builder item() {
            return new ItemPredicate.Builder();
        }

        public KoishItemPredicate.Builder of(Key item) {
            this.item = item;
            return this;
        }

        public KoishItemPredicate.Builder withCount(MinMaxBounds.Ints count) {
            this.count = count;
            return this;
        }

        public KoishItemPredicate build() {
            return new KoishItemPredicate(this.item, this.count);
        }
    }
}
