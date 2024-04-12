package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.item.schema.meta.SFoodMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import kotlin.reflect.KClass

/**
 * 可以作为工具的物品。
 */
interface Tool : ItemBehavior {

    override val requiredItemMeta: Array<KClass<out SchemaItemMeta<*>>>
        get() = arrayOf(SFoodMeta::class)
}