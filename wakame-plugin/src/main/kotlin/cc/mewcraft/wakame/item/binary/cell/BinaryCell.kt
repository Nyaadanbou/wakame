package cc.mewcraft.wakame.item.binary.cell

import cc.mewcraft.wakame.item.Cell
import cc.mewcraft.wakame.item.ShadowTagLike
import cc.mewcraft.wakame.item.binary.cell.core.BinaryCore
import cc.mewcraft.wakame.item.binary.cell.core.isEmpty
import cc.mewcraft.wakame.item.binary.cell.core.isNoop
import cc.mewcraft.wakame.item.binary.cell.curse.BinaryCurse
import cc.mewcraft.wakame.item.binary.cell.reforge.ReforgeDataHolder
import cc.mewcraft.wakame.item.schema.cell.SchemaCell
import net.kyori.examination.Examinable

//
// BinaryCell 接口是相对简单的，但实现上有很大不同。
// 出于性能和易用性考虑，我们特别的把实现分为了两类：
// 1. NBT 对象的封装类（本身只持有一个 NBT 对象，其他均为函数）
// 2. 纯粹的数据存储类（本身直接持有了所有的数据）
//
// 每种 BinaryCell 实现下，其成员变量也应该与 BinaryCell 的实现类型一致。
// 也就是说，如果 BinaryCell 是封装类，那其成员变量的实现也应该是封装类。

/**
 * Represents a certain state from a [SchemaCell].
 */
interface BinaryCell : Cell, ShadowTagLike, Examinable {
    /**
     * The core stored in this cell.
     */
    val core: BinaryCore

    /**
     * The curse stored in this cell.
     */
    val curse: BinaryCurse

    /**
     * The reforge data stored in this cell.
     */
    val reforge: ReforgeDataHolder
}

/**
 * Checks if the cell is noop.
 *
 * **A noop cell should never be written to the item NBT!**
 *
 * **By design, if the core is noop, then the whole cell is considered noop.**
 */
val BinaryCell.isNoop: Boolean get() = (this.core.isNoop)

/**
 * Checks if the cell is empty.
 *
 * **By design, if the core is empty, then the whole cell is considered empty.**
 */
val BinaryCell.isEmpty: Boolean get() = (this.core.isEmpty)
