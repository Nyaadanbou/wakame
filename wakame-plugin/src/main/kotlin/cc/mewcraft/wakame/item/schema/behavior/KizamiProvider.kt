package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.schema.NekoItem

interface KizamiProvider : ItemBehavior {
    companion object Factory : ItemBehaviorFactory<KizamiProvider> {
        override fun create(item: NekoItem, config: ConfigProvider): KizamiProvider {
            return Default()
        }
    }

    private class Default : KizamiProvider
}