package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.schema.NekoItem

/**
 * 可以作为工具的物品。
 */
interface Tool : ItemBehavior {

    companion object Factory : ItemBehaviorFactory<Tool> {
        override fun create(item: NekoItem, config: ConfigProvider): Tool {
            return Default()
        }
    }

    private class Default(
    ) : Tool {
    }
}