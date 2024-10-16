package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.*
import net.kyori.examination.Examinable


interface HideTooltip : Examinable {

    companion object : ItemComponentBridge<HideTooltip> {
        /**
         * 返回 [HideTooltip] 的实例.
         */
        fun instance(): HideTooltip {
            return Value
        }

        override fun codec(id: String): ItemComponentType<HideTooltip> {
            return Codec(id)
        }
    }

    private data object Value : HideTooltip

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<HideTooltip> {
        override fun read(holder: ItemComponentHolder): HideTooltip? {
            val im = holder.item.itemMeta ?: return null
            if (im.isHideTooltip) {
                return Value
            }
            return null
        }

        override fun write(holder: ItemComponentHolder, value: HideTooltip) {
            holder.item.editMeta {
                it.isHideTooltip = true
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.isHideTooltip = false
            }
        }
    }
}