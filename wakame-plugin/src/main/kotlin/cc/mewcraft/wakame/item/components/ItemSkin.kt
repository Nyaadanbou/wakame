package cc.mewcraft.wakame.item.components

import cc.mewcraft.wakame.item.component.ItemComponentBridge
import cc.mewcraft.wakame.item.component.ItemComponentType
import net.kyori.examination.Examinable

// TODO 完成组件: ItemSkin

interface ItemSkin : Examinable {
    companion object : ItemComponentBridge<ItemSkin> {
        override fun codec(id: String): ItemComponentType<ItemSkin> {
            throw NotImplementedError("Not implemented")
        }
    }
}
