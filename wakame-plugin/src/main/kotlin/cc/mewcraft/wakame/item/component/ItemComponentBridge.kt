package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.template.ItemTemplateType

/**
 * 用来统一物品组件的具体实现.
 */
interface ItemComponentBridge<T> {

    /**
     * 物品组件的 [ItemComponentType].
     */
    fun codec(id: String): ItemComponentType<T>

    /**
     * 物品组件的模板的 [ItemTemplateType].
     */
    fun templateType(id: String): ItemTemplateType<*>

}