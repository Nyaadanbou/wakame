package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.examination.Examinable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


// 开发日记 2024/6/28
// CustomName 有几个需要思考的问题:
// 1. 在物品上存什么;
// 2. 从物品上获取时返回什么;
// 3. 在后台模板上存什么;
data class CustomName(
    /**
     * 这部分是直接存在 NBT 里的原始字符串.
     */
    val raw: String,
    /**
     * 原始字符串 [raw] 经解析之后的产生的 [Component].
     *
     * 这部分是直接存在原版物品组件 `minecraft:custom_name` 上的.
     * 因此, 如果之后有修改过该组件的值, 那么该文本可能与 [raw]
     * 一开始生成出来的富文本有所差别.
     */
    val rich: Component,
) : Examinable {

    /**
     * 用于直接设置 `minecraft:custom_name`.
     */
    constructor(
        rich: Component,
    ) : this(
        miniMessage.serialize(rich), rich
    )

    companion object : ItemComponentBridge<CustomName>, KoinComponent {
        override fun codec(id: String): ItemComponentType<CustomName> {
            return Codec(id)
        }

        private val miniMessage by inject<MiniMessage>()
    }

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<CustomName> {

        private companion object {
            const val TAG_VALUE = "raw"
        }

        override fun read(holder: ItemComponentHolder): CustomName? {
            val tag = holder.getTag() ?: return null

            // 2024/6/29
            // 设计上 custom_name 和 item_name 都不经过发包系统处理,
            // 因此这里有什么就读取什么. 整体上做到简单, 一致, 无例外.

            // 获取 raw string
            val raw = tag.getString(TAG_VALUE)
            // 获取 rich string
            val rich = holder.item.itemMeta?.displayName() ?: Component.empty()

            return CustomName(raw = raw, rich = rich)
        }

        override fun write(holder: ItemComponentHolder, value: CustomName) {
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

            // 将 rich 写入到原版物品组件 `minecraft:custom_name`
            holder.item.editMeta {
                it.displayName(value.rich)
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
            holder.item.editMeta {
                it.displayName(null)
            }
        }
    }
}
