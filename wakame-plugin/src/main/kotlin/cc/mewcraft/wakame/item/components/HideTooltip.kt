package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
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
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: HideTooltip) {
            ItemDeprecations.usePaperOrNms()
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}