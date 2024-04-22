package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SToolMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import kotlin.reflect.KClass

/**
 * 可以作为工具的物品。
 */
interface Tool : ItemBehavior {


    override val requiredItemMeta: Array<KClass<out SchemaItemMeta<*>>>
        get() = arrayOf(SToolMeta::class)

    companion object Factory : ItemBehaviorFactory<Tool> {
        override fun create(item: NekoItem, config: ConfigProvider): Tool {
            return Default()
        }
    }

    private class Default(
    ) : Tool {
    }
}