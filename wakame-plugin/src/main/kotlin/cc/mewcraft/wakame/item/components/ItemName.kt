package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


data class ItemName(
    /**
     * 原始字符串, 格式为 MiniMessage.
     *
     * 这部分是直接存在 NBT 里的原始字符串.
     */
    val raw: String,

    /**
     * 原始字符串 [raw] 经解析之后的产生的 [Component].
     *
     * 这部分是直接存在原版物品组件 `minecraft:item_name` 上的.
     * 因此, 如果之后有修改过该组件的值, 那么该文本可能与 [raw]
     * 一开始生成出来的富文本有所差别.
     */
    val rich: Component,
) : Examinable {

    /**
     * 用于直接设置 `minecraft:item_name`.
     */
    constructor(
        rich: Component,
    ) : this(
        miniMessage.serialize(rich), rich
    )

    companion object : ItemComponentBridge<ItemName>, KoinComponent {
        /**
         * 该组件的配置文件.
         */
        private val config: ItemComponentConfig = ItemComponentConfig.provide(ItemConstants.ITEM_NAME)

        override fun codec(id: String): ItemComponentType<ItemName> {
            return Codec(id)
        }

        private val miniMessage: MiniMessage by inject()
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<ItemName> {
        override fun read(holder: ItemComponentHolder): ItemName? {
            val tag = holder.getTag() ?: return null

            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就读取什么. 整体上做到简单, 一致, 无例外.

            // 获取 raw string
            val raw = tag.getString(TAG_VALUE)
            // 获取 rich string
            val rich = holder.item.itemMeta?.itemName() ?: Component.empty()

            return ItemName(raw = raw, rich = rich)
        }

        override fun write(holder: ItemComponentHolder, value: ItemName) {
            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就写入什么. 整体上做到简单, 一致, 无例外.

            // 将 raw 写入到 NBT
            if (value.raw.isNotBlank()) {
                // 只有当 raw 不为空字符串时才更新 NBT
                holder.editTag { tag ->
                    tag.putString(TAG_VALUE, value.raw)
                }
            }

            // 将 rich 写入到原版物品组件 `minecraft:item_name`
            val item = holder.item
            item.editMeta {
                it.itemName(value.rich)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
            holder.item.editMeta { it.itemName(null) }
        }

        private companion object {
            const val TAG_VALUE = "raw"
        }
    }
}