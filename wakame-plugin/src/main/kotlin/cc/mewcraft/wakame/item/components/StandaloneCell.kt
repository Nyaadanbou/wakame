package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.reforge.ReforgeHistory

/**
 * 代表一个独立的核孔 [Core], 用于任何只需要呈现单个核孔的场景.
 * 目前主要用于实现重铸系统的某些功能.
 */
data class StandaloneCell(
    val cell: Cell,
) {
    companion object : ItemComponentBridge<StandaloneCell> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.STANDALONE_CELL)

        override fun codec(id: String): ItemComponentType<StandaloneCell> {
            return Codec(id)
        }
    }

    /**
     * 方便函数.
     */
    val id: String
        get() = cell.getId()

    /**
     * 方便函数.
     */
    val core: Core
        get() = cell.getCore()

    /**
     * 方便函数.
     */
    val reforgeHistory: ReforgeHistory
        get() = cell.getReforgeHistory()

    private data class Codec(override val id: String) : ItemComponentType<StandaloneCell> {
        override fun read(holder: ItemComponentHolder): StandaloneCell? {
            val tag = holder.getTag() ?: return null
            val id = tag.getString(TAG_ID)
            val cell = Cell.of(id, tag.getCompound(TAG_CELL))
            return StandaloneCell(cell)
        }

        override fun write(holder: ItemComponentHolder, value: StandaloneCell) {
            holder.editTag { tag ->
                tag.putString(TAG_ID, value.id)
                tag.put(TAG_CELL, value.cell.serializeAsTag())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        companion object {
            private const val TAG_ID = "id"
            private const val TAG_CELL = "cell"
        }
    }
}