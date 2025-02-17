package cc.mewcraft.wakame.item.components.cells

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.util.CompoundTag

/**
 * 核孔.
 *
 * @property id 核孔的 id
 * @property core 核孔的核心
 */
data class Cell(
    val id: String,
    val core: Core = Core.empty(),
) {

    companion object {

        /**
         * 构建一个 [Cell].
         *
         * NBT 标签的结构要求可以参考本项目的 `README`.
         */
        fun fromNbt(id: String, nbt: CompoundTag): Cell {
            return Cell(id = id, Core.fromNbt(nbt.getCompound("core")))
        }

    }

    fun saveNbt(): CompoundTag = CompoundTag {
        put("core", core.saveNbt())
    }

}
