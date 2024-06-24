package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.component.ItemComponentType
import org.koin.core.component.KoinComponent

object ItemComponentRegistry : KoinComponent, Initializable {

    internal val CONFIG by lazy { Configs.YAML[ITEM_CONFIG_FILE].derive("components") }

    val TYPES: Registry<String, ItemComponentType<*, *>> = SimpleRegistry()

    override fun onPreWorld() {

    }
}