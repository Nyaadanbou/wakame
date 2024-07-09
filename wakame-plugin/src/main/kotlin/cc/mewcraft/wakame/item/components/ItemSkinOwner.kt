package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.template.ItemTemplateType
import net.kyori.examination.Examinable

// TODO 完成组件: ItemSkinOwner

interface ItemSkinOwner : Examinable {
    companion object : ItemComponentBridge<ItemSkinOwner> {
        override fun codec(id: String): ItemComponentType<ItemSkinOwner> {
            throw NotImplementedError("Not implemented")
        }

        override fun templateType(id: String): ItemTemplateType<*> {
            throw NotImplementedError("Not implemented")
        }
    }
}