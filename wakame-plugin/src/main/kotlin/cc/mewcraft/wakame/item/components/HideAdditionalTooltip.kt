package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag


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
            val im = holder.item.itemMeta ?: return null
            if (im.hasItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)) {
                return Value
            }
            return null
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta {
                it.removeItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
        }

        override fun write(holder: ItemComponentHolder, value: HideAdditionalTooltip) {
            holder.item.editMeta {
                it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
        }
    }
}