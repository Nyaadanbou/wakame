package cc.mewcraft.wakame.mixin.support;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class KoishInventoryChangeTrigger extends SimpleCriterionTrigger<KoishInventoryChangeTrigger.TriggerInstance> {

    @Override
    public Codec<KoishInventoryChangeTrigger.TriggerInstance> codec() {
        return KoishInventoryChangeTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, Inventory inventory, ItemStack stack) {
        int i = 0;
        int i1 = 0;
        int i2 = 0;

        for (int i3 = 0; i3 < inventory.getContainerSize(); i3++) {
            ItemStack item = inventory.getItem(i3);
            if (item.isEmpty()) {
                i1++;
            } else {
                i2++;
                if (item.getCount() >= item.getMaxStackSize()) {
                    i++;
                }
            }
        }

        this.trigger(player, inventory, stack, i, i1, i2);
    }

    private void trigger(ServerPlayer player, Inventory inventory, ItemStack stack, int full, int empty, int occupied) {
        this.trigger(player, instance -> instance.matches(inventory, stack, full, empty, occupied));
    }

    public record TriggerInstance(
            @Override Optional<ContextAwarePredicate> player,
            KoishInventoryChangeTrigger.TriggerInstance.Slots slots,
            List<KoishItemPredicate> items
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<KoishInventoryChangeTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KoishInventoryChangeTrigger.TriggerInstance::player),
                        KoishInventoryChangeTrigger.TriggerInstance.Slots.CODEC.optionalFieldOf("slots", KoishInventoryChangeTrigger.TriggerInstance.Slots.ANY).forGetter(KoishInventoryChangeTrigger.TriggerInstance::slots),
                        KoishItemPredicate.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(KoishInventoryChangeTrigger.TriggerInstance::items)
                ).apply(instance, KoishInventoryChangeTrigger.TriggerInstance::new)
        );

        public boolean matches(Inventory inventory, ItemStack stack, int full, int empty, int occupied) {
            if (!this.slots.matches(full, empty, occupied)) {
                return false;
            } else if (this.items.isEmpty()) {
                return true;
            } else if (this.items.size() != 1) {
                List<KoishItemPredicate> list = new ObjectArrayList<>(this.items);
                int containerSize = inventory.getContainerSize();

                for (int i = 0; i < containerSize; i++) {
                    if (list.isEmpty()) {
                        return true;
                    }

                    ItemStack item = inventory.getItem(i);
                    if (!item.isEmpty()) {
                        list.removeIf(item1 -> item1.test(item));
                    }
                }

                return list.isEmpty();
            } else {
                return !stack.isEmpty() && this.items.get(0).test(stack);
            }
        }

        public record Slots(MinMaxBounds.Ints occupied, MinMaxBounds.Ints full, MinMaxBounds.Ints empty) {
            public static final Codec<KoishInventoryChangeTrigger.TriggerInstance.Slots> CODEC = RecordCodecBuilder.create(
                    instance -> instance.group(
                            MinMaxBounds.Ints.CODEC.optionalFieldOf("occupied", MinMaxBounds.Ints.ANY).forGetter(KoishInventoryChangeTrigger.TriggerInstance.Slots::occupied),
                            MinMaxBounds.Ints.CODEC.optionalFieldOf("full", MinMaxBounds.Ints.ANY).forGetter(KoishInventoryChangeTrigger.TriggerInstance.Slots::full),
                            MinMaxBounds.Ints.CODEC.optionalFieldOf("empty", MinMaxBounds.Ints.ANY).forGetter(KoishInventoryChangeTrigger.TriggerInstance.Slots::empty)
                    ).apply(instance, KoishInventoryChangeTrigger.TriggerInstance.Slots::new)
            );
            public static final KoishInventoryChangeTrigger.TriggerInstance.Slots ANY = new KoishInventoryChangeTrigger.TriggerInstance.Slots(
                    MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY
            );

            public boolean matches(int full, int empty, int occupied) {
                return this.full.matches(full) && this.empty.matches(empty) && this.occupied.matches(occupied);
            }
        }
    }
}