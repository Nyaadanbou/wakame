package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.ShownInTooltip
import cc.mewcraft.wakame.item.component.*
import net.kyori.examination.Examinable
import org.bukkit.inventory.ItemFlag

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
            val itemMeta = holder.item.itemMeta ?: return null
            if (!itemMeta.isUnbreakable) {
                return null
            }
            return Value(!itemMeta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE))
        }

        override fun write(holder: ItemComponentHolder, value: Unbreakable) {
            holder.item.editMeta {
                it.isUnbreakable = true
                if (value.showInTooltip) {
                    it.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                } else {
                    it.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.item.editMeta { it.isUnbreakable = false }
        }
    }
}