package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentConfig
import cc.mewcraft.wakame.item.component.ItemComponentHolder
import cc.mewcraft.wakame.item.component.ItemComponentMeta
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.adventure.key.Key
import net.kyori.examination.Examinable

interface SystemUse : Examinable {

    companion object : ItemComponentBridge<Unit>, ItemComponentMeta {
        override fun codec(id: String): ItemComponentType<Unit> {
            return Codec(id)
        }

        override fun templateType(): ItemTemplateType<Nothing> {
            throw UnsupportedOperationException()
        }

        override val configPath: String = ItemComponentConstants.SYSTEM_USE
        override val tooltipKey: Key = ItemComponentConstants.createKey { SYSTEM_USE }

        private val config: ItemComponentConfig = ItemComponentConfig.provide(this)
    }

    // 开发日记 2024/6/27
    // SystemUse 组件只用于内部代码,
    // 因此只有一个 Codec.
    // 它既不需要一个特定的 Value, 因为它只有存在与否;
    // 它也不需要一个特定的 Template, 因为配置文件暂时没有用处.

    private data class Codec(
        override val id: String,
    ) : ItemComponentType<Unit> {
        override fun read(holder: ItemComponentHolder): Unit? {
            return if (holder.hasTag()) Unit else null
        }

        override fun write(holder: ItemComponentHolder, value: Unit) {
            holder.putTag()
        }

        override fun remove(holder: ItemComponentHolder) {
            holder.removeTag()
        }
    }
}