package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.item2.KoishStackData;
import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.key.Key;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 能够正确识别自定义物品的 {@link net.minecraft.world.item.ItemStackLinkedSet}.
 * <p>
 * 当物品是 Koish 物品时, 产生的哈希值将基于 {@link net.minecraft.world.item.Item} 和 Koish 物品类型, 忽略其他所有信息.
 * 当物品不是 Koish 物品时, 产生的哈希值将基于游戏原本的哈希算法, 也就是基于物品类型和完整的物品组件.
 */
public class CustomItemStack {

    /**
     * 用于比较 exact 物品堆叠的哈希策略.
     */
    public static final Hash.Strategy<ItemStack> EXACT_MATCH_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(@Nullable ItemStack stack) {
            return CustomItemStack.hashCode(stack);
        }

        @Override
        public boolean equals(@Nullable ItemStack first, @Nullable ItemStack second) {
            return CustomItemStack.equals(first, second);
        }
    };

    /**
     * 计算物品堆叠的哈希值.
     * <p>
     * 如果物品是 Koish 物品, 则哈希值基于 Koish 物品类型;
     * 如果物品不是 Koish 物品, 则哈希值基于物品类型和完整的物品组件.
     *
     * @param stack 物品堆叠
     * @return 物品的哈希值
     */
    public static int hashCode(@Nullable ItemStack stack) {
        if (stack == null) return 0;
        int h = 31 + stack.getItem().hashCode();
        Key id = KoishStackData.getKoishTypeId(stack);
        if (id != null) {
            return 31 * h + id.hashCode();
        } else {
            // FIXME #396
            // 目前的临时考虑: 对于 ItemChoice.Exact 中的非 Koish 物品, 只考虑物品类型, 忽略所有组件
            // 这实际上破坏了 Bukkit API 中关于 ItemChoice.Exact 的定义, 但实际情况是, 只要没有其他插件注册 ItemChoice.Exact 的配方, 那么就没有问题
            // 我们需要考虑更好的实现, 即保证 ItemChoice.Exact 的语义不被破坏, 同时又能让 Koish 物品在配方中只考虑物品类型
            return 31 * h;
            //return 31 * h + stack.getComponents().hashCode();
        }
    }

    /**
     * 检查两个物品堆叠是否属于同一个 Koish 物品或具有相同的组件.
     *
     * @param first  第一个物品堆叠
     * @param second 第二个物品堆叠
     * @return 如果两个物品堆叠属于同一个 Koish 物品则直接返回 true, 否则继续判断是否有相同的物品组件
     */
    public static boolean equals(@Nullable ItemStack first, @Nullable ItemStack second) {
        // FIXME #396
        return first == second || first != null && second != null && first.isEmpty() == second.isEmpty() && (isSameKoishItemType(first, second) || ItemStack.isSameItem(first, second));
        //return first == second || first != null && second != null && first.isEmpty() == second.isEmpty() && (isSameKoishItemType(first, second) || ItemStack.isSameItemSameComponents(first, second));
    }

    /**
     * 检查两个物品堆叠是否属于同一个 Koish 物品类型.
     *
     * @param first  第一个物品堆叠
     * @param second 第二个物品堆叠
     * @return 如果两个物品堆叠属于同一个 Koish 物品类型, 则返回 true; 否则返回 false
     */
    public static boolean isSameKoishItemType(@NonNull ItemStack first, @NonNull ItemStack second) {
        Key id1 = KoishStackData.getKoishTypeId(first);
        Key id2 = KoishStackData.getKoishTypeId(second);
        return Objects.equals(id1, id2);
    }
}
