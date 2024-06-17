package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.schema.NekoItem

interface AttributeProvider : ItemBehavior {
    companion object Factory : ItemBehaviorFactory<AttributeProvider> {
        override fun create(item: NekoItem, config: ConfigProvider): AttributeProvider {
            return Default()
        }
    }

    private class Default : AttributeProvider
}