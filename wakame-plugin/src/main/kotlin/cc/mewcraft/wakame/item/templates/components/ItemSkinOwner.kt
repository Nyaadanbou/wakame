package cc.mewcraft.wakame.item.templates.components

import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.item.components.ItemSkinOwner as ItemSkinOwnerData

// TODO 完成组件: ItemSkinOwner

interface ItemSkinOwner : ItemTemplate<ItemSkinOwnerData> {
    companion object : ItemTemplateBridge<ItemSkinOwner> {
        override fun codec(id: String): ItemTemplateType<ItemSkinOwner> {
            throw NotImplementedError("Not implemented")
        }
    }
}