package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.item2.KoishStackData;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.kyori.adventure.key.Key;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

/**
 * 能够正确识别自定义物品的 {@link net.minecraft.world.item.ItemStackLinkedSet}.
 * <p>
 * 当物品是 Koish 物品时, 产生的哈希值将基于 {@link net.minecraft.world.item.Item} 和 Koish 物品类型, 忽略其他所有信息.
 * 当物品不是 Koish 物品时, 产生的哈希值将基于游戏原本的哈希算法, 也就是基于物品类型和完整的物品组件.
 */
public class CustomItemStackLinkedSet {

    public static final Hash.Strategy<? super ItemStack> KOISH_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(@Nullable ItemStack stack) {
            if (stack == null) return 0;
            int h = 31 + stack.getItem().hashCode();
            Key id = KoishStackData.getKoishTypeId(stack);
            if (id != null) {
                return 31 * h + id.hashCode();
            } else {
                return 31 * h + stack.getComponents().hashCode();
            }
        }

        @Override
        public boolean equals(@Nullable ItemStack first, @Nullable ItemStack second) {
            return first == second || first != null && second != null && first.isEmpty() == second.isEmpty() && (isSameKoishItem(first, second) || ItemStack.isSameItemSameComponents(first, second));
        }

        private static boolean isSameKoishItem(ItemStack first, ItemStack second) {
            Key id1 = KoishStackData.getKoishTypeId(first);
            Key id2 = KoishStackData.getKoishTypeId(second);
            return Objects.equals(id1, id2);
        }
    };

    public static Set<ItemStack> createTypeAndComponentsSet() {
        return new ObjectLinkedOpenCustomHashSet<>(KOISH_STRATEGY);
    }
}
