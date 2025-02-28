package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ItemDeprecations
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.examination.Examinable
import io.papermc.paper.datacomponent.item.Unbreakable as PaperUnbreakable

// 开发日记 2024/6/27
// 之所以写这个组件是因为想验证一下
// 在配置文件中以 1:1 的形式配置原版物品组件
// 是否可行.

interface Unbreakable : Examinable, ShownInTooltip {

    companion object : ItemComponentBridge<Unbreakable> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.UNBREAKABLE)

        /**
         * 构建一个 [Unbreakable] 的实例.
         */
        fun of(showInTooltip: Boolean): Unbreakable {
            return Value(showInTooltip)
        }

        override fun codec(id: String): ItemComponentType<Unbreakable> {
            return Codec(id)
        }
    }

    private data class Value(
        override val showInTooltip: Boolean,
    ) : Unbreakable {
        private companion object
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unbreakable> {
        override fun read(holder: ItemComponentHolder): Unbreakable? {
            ItemDeprecations.usePaperOrNms()
        }

        override fun write(holder: ItemComponentHolder, value: Unbreakable) {
            val showInTooltip = value.showInTooltip
            val paperUnbreakable = PaperUnbreakable.unbreakable(showInTooltip)
            holder.bukkitStack.setData(DataComponentTypes.UNBREAKABLE, paperUnbreakable)
        }

        override fun remove(holder: ItemComponentHolder) {
            ItemDeprecations.usePaperOrNms()
        }
    }
}