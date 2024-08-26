package cc.mewcraft.wakame.mixin.support;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Set;

public class ItemStackStrategy {
    public static final Hash.Strategy<? super ItemStack> TYPE_AND_TAG = new Hash.Strategy<ItemStack>() {
        @Override
        public int hashCode(@Nullable ItemStack itemStack) {
            return ItemStack.hashItemAndComponents(itemStack);
        }

        @Override
        public boolean equals(@Nullable ItemStack itemStack, @Nullable ItemStack itemStack2) {
            return itemStack == itemStack2
                    || itemStack != null
                    && itemStack2 != null
                    && itemStack.isEmpty() == itemStack2.isEmpty()
                    && ItemStack.isSameItemSameComponents(itemStack, itemStack2);
        }
    };

    public static Set<ItemStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet<>(TYPE_AND_TAG);
    }
}
