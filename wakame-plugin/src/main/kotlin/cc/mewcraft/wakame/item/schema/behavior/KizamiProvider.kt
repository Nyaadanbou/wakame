package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import kotlin.reflect.KClass

interface KizamiProvider : ItemBehavior {
    companion object Factory : ItemBehaviorFactory<KizamiProvider> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): KizamiProvider {
            return Default()
        }
    }

    private class Default : KizamiProvider {
        override val requiredItemMeta: Array<KClass<out SchemaItemMeta<*>>> = emptyArray()
    }
}