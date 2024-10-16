package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.components.ItemSkin as ItemSkinData

// TODO 完成组件: ItemSkin

interface ItemSkin : ItemTemplate<ItemSkinData> {
    companion object : ItemTemplateBridge<ItemSkin> {
        override fun codec(id: String): ItemTemplateType<ItemSkin> {
            throw NotImplementedError("Not implemented")
        }
    }
}