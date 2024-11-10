package cc.mewcraft.wakame.mixin.support;

import it.unimi.dsi.fastutil.Hash;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * ItemStack 的自定义哈希算法.
 * <p>
 * 当物品是萌芽物品时, 产生的哈希值将完全基于 {@link net.minecraft.world.item.Item} 和萌芽物品的标识, 而忽略所有其他的信息.
 * 当物品不是萌芽物品时, 产生的哈希值将基于游戏原本的哈希算法, 也就是基于物品类型和完整的物品组件.
 */
public class ItemStackStrategy {
    public static final Hash.Strategy<? super ItemStack> CUSTOM_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(@Nullable ItemStack itemStack) {
            if (itemStack == null) {
                return 0;
            }

            // 始终计算物品类型的哈希值
            int h = 31 + itemStack.getItem().hashCode();

            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null && customData.contains("wakame")) {
                // 萌芽物品, 只计算萌芽 id 的哈希
                CompoundTag nekooTag = customData.getUnsafe().getCompound("wakame");
                h = 31 * h + Objects.hashCode(nekooTag.get("id"));
                return h;
            } else {
                // 非萌芽物品, 计算所有物品组件的哈希
                return 31 * h + itemStack.getComponents().hashCode();
            }
        }

        @Override
        public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
            return itemStack == itemStack2 ||
                   itemStack != null && itemStack2 != null &&
                   itemStack.isEmpty() == itemStack2.isEmpty() &&
                   (
                           isSameNyaItem(itemStack, itemStack2) ||
                           ItemStack.isSameItemSameComponents(itemStack, itemStack2)
                   );
        }
    };

    private static boolean isSameNyaItem(@NotNull ItemStack itemStack1, @NotNull ItemStack itemStack2) {
        CustomData customData1 = itemStack1.get(DataComponents.CUSTOM_DATA);
        CustomData customData2 = itemStack2.get(DataComponents.CUSTOM_DATA);
        if (customData1 == null || customData2 == null) {
            return false;
        }

        if (customData1.contains("wakame") && customData2.contains("wakame")) {
            CompoundTag nekooTag1 = customData1.getUnsafe().getCompound("wakame");
            CompoundTag nekooTag2 = customData2.getUnsafe().getCompound("wakame");
            return Objects.equals(nekooTag1.get("id"), nekooTag2.get("id"));
        }

        return false;
    }
}
