package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable
import net.minecraft.core.component.DataComponents


interface ItemMaxDamage : Examinable {
    companion object : ItemComponentBridge<Int> {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.MAX_DAMAGE)

        override fun codec(id: String): ItemComponentType<Int> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Int> {
        override fun read(holder: ItemComponentHolder): Int {
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: Int) {
            holder.mojangStack.set(DataComponents.MAX_DAMAGE, value)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}