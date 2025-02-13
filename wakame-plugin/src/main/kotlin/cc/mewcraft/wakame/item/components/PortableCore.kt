package cc.mewcraft.wakame.item.components

import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.item.ItemConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.components.cells.Core
import net.kyori.adventure.text.Component
import net.kyori.examination.Examinable

data class PortableCore(
    /**
     * 本便携式核心所包含的核心.
     */
    override val wrapped: Core,
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

            // fix data
            if (tag.contains(TAG_CORE, TagType.COMPOUND)) {
                val old = tag.getCompound(TAG_CORE)
                tag.remove(TAG_CORE)
                tag.merge(old)
            }

            val core = Core.fromNbt(tag)
            return PortableCore(core)
        }

        override fun write(holder: ItemComponentHolder, value: PortableCore) {
            holder.editTag { tag ->
                tag.merge(value.wrapped.saveNbt())
            }
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }

        companion object {
            private const val TAG_CORE = "core"
        }
    }
}