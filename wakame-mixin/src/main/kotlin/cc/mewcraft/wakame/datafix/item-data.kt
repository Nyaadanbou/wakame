package cc.mewcraft.wakame.datafix

import cc.mewcraft.lazyconfig.configurate.require
import cc.mewcraft.wakame.SharedConstants
import cc.mewcraft.wakame.item.data.ItemDataType
import cc.mewcraft.wakame.item.data.ItemDataTypes
import cc.mewcraft.wakame.registry.BuiltInRegistries
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.transformation.ConfigurationTransformation

/**
 * 用于修复物品堆叠的数据格式.
 */
object ItemDataFixer {

    @JvmStatic
    val transforms: ArrayList<ItemDataFix> = arrayListOf(
        ItemDataFixV1(),
    )

    @JvmStatic
    private val versionKey: String by lazy {
        BuiltInRegistries.ITEM_DATA_TYPE.getId(ItemDataTypes.VERSION)!!.value()
    }

    @JvmStatic
    fun createFix(): ConfigurationTransformation {
        return ConfigurationTransformation.versionedBuilder()
            .versionKey(versionKey)
            .apply {
                transforms.forEach { trans ->
                    this.addVersion(trans.version, trans)
                }
            }.build()
    }

    @JvmStatic
    fun needFix(rootNode: ConfigurationNode): Boolean {
        return rootNode.node(versionKey).getInt(-1) != SharedConstants.ITEM_STACK_DATA_VERSION
    }

    @JvmStatic
    fun validate(rootNode: ConfigurationNode) {
        if (!SharedConstants.isRunningInIde && rootNode.node(versionKey).getInt(-1) != SharedConstants.ITEM_STACK_DATA_VERSION) {
            throw IllegalStateException("The data version of item $rootNode does not match the expected version ${SharedConstants.ITEM_STACK_DATA_VERSION}.")
        }
    }
}

abstract class ItemDataFix(
    val version: Int,
) : ConfigurationTransformation {

    /**
     * 返回 [dataType] 对应的配置节点.
     */
    protected fun ConfigurationNode.dataNode(dataType: ItemDataType<*>): ConfigurationNode {
        val nodeKey = BuiltInRegistries.ITEM_DATA_TYPE.getId(dataType)?.value()
            ?: error("ItemDataType $dataType is not registered in BuiltInRegistries")
        return this.node(nodeKey)
    }

    /**
     * 将此配置节点反序列化成 [dataType].
     */
    protected inline fun <reified T> ConfigurationNode.getData(dataType: ItemDataType<T>): T {
        return dataNode(dataType).require<T>()
    }
}

private class ItemDataFixV1 : ItemDataFix(1) {

    override fun apply(node: ConfigurationNode) {
    }
}
