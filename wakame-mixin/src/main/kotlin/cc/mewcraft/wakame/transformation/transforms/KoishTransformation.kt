package cc.mewcraft.wakame.transformation.transforms

import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.transformation.ConfigurationTransformation

abstract class KoishTransformation(
    val version: Int
) : ConfigurationTransformation {
    protected fun ConfigurationNode.dataNode(dataType: ItemDataType<*>): ConfigurationNode {
        val nodeKey = BuiltInRegistries.ITEM_DATA_TYPE.getId(dataType)?.value()
            ?: error("ItemDataType $dataType is not registered in BuiltInRegistries")
        return this.node(nodeKey)
    }

    protected inline fun <reified T> ConfigurationNode.data(dataType: ItemDataType<T>): T {
        return dataNode(dataType).require<T>()
    }
}