package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.*
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.item.components.cells.CoreFactory
import cc.mewcraft.wakame.util.getCompoundOrNull
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable

data class PortableCore(
    /**
     * 本便携式核心所包含的核心.
     */
    override val wrapped: Core,
    /**
     * 本便携式核心当前的惩罚值.
     *
     * 该值的具体作用由实现决定, 这里仅提供一个通用的字段.
     */
    val penalty: Int,
) : PortableObject<Core>, Examinable {
    /**
     * 方便函数.
     *
     * @see Core.displayName
     */
    val displayName: Component
        get() = wrapped.displayName

    /**
     * 方便函数.
     *
     * @see Core.description
     */
    val description: List<Component>
        get() = wrapped.description

    companion object : ItemComponentBridge<PortableCore> {
        /**
         * 该组件的配置文件.
         */
        private val config = ItemComponentConfig.provide(ItemConstants.PORTABLE_CORE)

        override fun codec(id: String): ItemComponentType<PortableCore> {
            return Codec(id)
        }
    }

    private data class Codec(override val id: String) : ItemComponentType<PortableCore> {
        override fun read(holder: ItemComponentHolder): PortableCore? {
            val tag = holder.getTag() ?: return null
            val core = tag.getCompoundOrNull(TAG_CORE)?.let { CoreFactory.deserialize(it) } ?: return null
            val mergeCount = tag.getInt(TAG_PENALTY)
            return PortableCore(core, mergeCount)
        }

        override fun write(holder: ItemComponentHolder, value: PortableCore) {
            holder.editTag { tag ->
                tag.put(TAG_CORE, value.wrapped.serializeAsTag())
                if (value.penalty > 0) {
                    tag.putInt(TAG_PENALTY, value.penalty)
                }
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        companion object {
            private const val TAG_CORE = "core"
            private const val TAG_PENALTY = "penalty"
        }
    }
}