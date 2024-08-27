package cc.mewcraft.wakame.mixin.support;

import it.unimi.dsi.fastutil.Hash;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.NotNull;

public class ItemStackStrategy {
    public static final Hash.Strategy<? super ItemStack> TYPE_AND_TAG = new Hash.Strategy<>() {
        @Override
        public int hashCode(@Nullable ItemStack itemStack) {
            if (itemStack == null) {
                return 0;
            }

            int h = 31 + itemStack.getItem().hashCode();

            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                if (customData.contains("wakame")) {
                    CompoundTag nyaTag = customData.getUnsafe().getCompound("wakame");
                    String namespace = nyaTag.getString("namespace");
                    String path = nyaTag.getString("path");
                    h = 31 * h + namespace.hashCode();
                    h = 31 * h + path.hashCode();
                    return h;
                }
            }

            for (final TypedDataComponent<?> component : itemStack.getComponents()) {
                h = 31 * h + component.hashCode();
            }

            return h;
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

    private static boolean isSameNyaItem(@NotNull ItemStack itemStack, @NotNull ItemStack itemStack2) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        CustomData customData2 = itemStack2.get(DataComponents.CUSTOM_DATA);
        if (customData == null || customData2 == null) {
            return false;
        }

        if (customData.contains("wakame") && customData2.contains("wakame")) {
            CompoundTag nyaTag = customData.getUnsafe().getCompound("wakame");
            CompoundTag nyaTag2 = customData2.getUnsafe().getCompound("wakame");
            return nyaTag.getString("namespace").equals(nyaTag2.getString("namespace"))
                   && nyaTag.getString("path").equals(nyaTag2.getString("path"));
        }

        return false;
    }
}
