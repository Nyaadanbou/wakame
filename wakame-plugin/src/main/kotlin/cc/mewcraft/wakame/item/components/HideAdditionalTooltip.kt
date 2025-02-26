package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable


interface HideAdditionalTooltip : Examinable {
    companion object : ItemComponentBridge<HideAdditionalTooltip> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.HIDE_ADDITIONAL_TOOLTIP)

        /**
         * 返回 [HideAdditionalTooltip] 的实例.
         */
        fun instance(): HideAdditionalTooltip {
            return Value
        }

        override fun codec(id: String): ItemComponentType<HideAdditionalTooltip> {
            return Codec(id)
        }
    }

    private data object Value : HideAdditionalTooltip

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<HideAdditionalTooltip> {
        override fun read(holder: ItemComponentHolder): HideAdditionalTooltip? {
            ItemDeprecations.usePaperOrNms()
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: HideAdditionalTooltip) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}