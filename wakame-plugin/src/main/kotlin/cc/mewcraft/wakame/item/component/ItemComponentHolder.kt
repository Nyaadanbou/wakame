package cc.mewcraft.wakame.item.component

import cc.mewcraft.nbt.CompoundTag
import org.bukkit.inventory.ItemStack

/**
 * 代表一个储存了物品组件信息的容器.
 *
 * 目前容器主要分为三类:
 *
 * - 有些物品组件是原版物品组件的代理, 这些组件就需要访问 [ItemStack].
 * - 有些物品组件是我们原创的, 这些组件就需要访问 NBT.
 * - 有些物品组件融合了以上两者, 这些组件就需要同时访问 [ItemStack] 和 NBT.
 */
sealed interface ItemComponentHolder {

    /**
     * 包含储存了单个组件信息的 NBT 结构.
     *
     * 注意这里的 NBT 是包含单个组件信息的 NBT 结构. 也就是说, 如果下面是我们的物品组件的整个 NBT:
     *
     * ```NBT
     * Compound('components'):
     *
     *     // 这一级是包含单个物品组件的最小 NBT 结构
     *     Compound('elements'):
     *
     *         // 这一级是组件内部的具体数据
     *         IntArray('value'): [1I, 2I, 3I]
     *
     *     Compound('kizamiz'):
     *
     *         ...
     *
     *     Compound('cells'):
     *
     *         ...
     * ```
     *
     * 那么这里的 NBT 只会是 `Compound('elements')` 或 `Compound('kizamiz')`,
     * 而不是最顶级的包含了所有组件信息的 `Compound('components')`.
     */
    data class NBT(val tag: CompoundTag) : ItemComponentHolder

    /**
     * 包含整个 [ItemStack] (本质是物品上的原版组件).
     */
    data class Item(val item: ItemStack) : ItemComponentHolder

    /**
     * 包含整个 [ItemStack], 以及储存了单个组件信息的 NBT 结构.
     */
    data class Complex(val item: ItemStack, val tag: CompoundTag) : ItemComponentHolder

}