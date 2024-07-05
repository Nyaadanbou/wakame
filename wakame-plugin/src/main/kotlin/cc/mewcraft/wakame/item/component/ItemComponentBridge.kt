package cc.mewcraft.wakame.item.component

import cc.mewcraft.wakame.item.template.ItemTemplateType

interface ItemComponentBridge<T /* : ItemComponent */> {

    fun codec(id: String): ItemComponentType<T>

    fun templateType(): ItemTemplateType<T>

}