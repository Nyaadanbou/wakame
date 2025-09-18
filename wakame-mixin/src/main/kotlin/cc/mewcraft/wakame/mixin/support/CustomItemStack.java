package cc.mewcraft.wakame.mixin.support;

import cc.mewcraft.wakame.item2.KoishStackData;
import it.unimi.dsi.fastutil.Hash;
import net.kyori.adventure.key.Key;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * 包含了让 Koish 物品堆叠在配方中只考虑物品类型(ID)的哈希策略和比较方法.
 */
public class CustomItemStack {

    /**
     * 用于比较 {@link org.bukkit.inventory.RecipeChoice} 中的物品堆叠的哈希策略.
     * <p>
     * 策略特点:
     * 使得 Koish 物品堆叠在参与配方匹配时只考虑 Koish 物品类型(ID), 而忽略其他物品组件.
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
     * 如果物品堆叠是 Koish 物品, 则哈希值基于 Koish 物品类型(ID);
     * 如果物品堆叠不是 Koish 物品, 则哈希值基于 Minecraft 物品类型.
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
            // 目前的临时考虑: 对于 ExactChoice 中的非 Koish 物品, 只考虑 Minecraft 物品类型, 忽略其所有额外组件
            // 这实际上破坏了 Bukkit API 中关于 ExactChoice 的定义, 但实际情况是, 只要没有其他插件注册使用了 ExactChoice 的配方, 那么就没有问题
            // 我们需要考虑更好的实现, 即保证 ExactChoice 的语义不被破坏, 同时又能让 Koish 物品在配方中只考虑物品类型(ID)

            // 只考虑物品类型
            return 31 * h;

            // 考虑物品类型 + 组件
            //return 31 * h + stack.getComponents().hashCode();
        }
    }

    /**
     * 检查两个物品堆叠是否属于同一个 Koish 物品或具有相同的 Minecraft 物品类型.
     *
     * @param first  第一个物品堆叠
     * @param second 第二个物品堆叠
     * @return 如果两个物品堆叠属于同一个 Koish 物品类型则直接返回 true, 否则继续判断是否具有相同的 Minecraft 物品类型
     */
    public static boolean equals(@Nullable ItemStack first, @Nullable ItemStack second) {
        // FIXME #396

        // 只考虑物品类型
        return first == second || first != null && second != null && first.isEmpty() == second.isEmpty() && (isSameKoishItemType(first, second) || ItemStack.isSameItem(first, second));

        // 考虑物品类型 + 组件
        //return first == second || first != null && second != null && first.isEmpty() == second.isEmpty() && (isSameKoishItemType(first, second) || ItemStack.isSameItemSameComponents(first, second));
    }

    /**
     * 检查两个物品堆叠(Koish 物品类型) 是否一样.
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
