package cc.mewcraft.wakame.transformation

import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.transformation.transforms.KoishTransformation
import cc.mewcraft.wakame.transformation.transforms.Transform1
import cc.mewcraft.wakame.util.test.TestOnly
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.transformation.ConfigurationTransformation

object Transformations {
    private val transforms: MutableSet<KoishTransformation> = mutableSetOf(
        Transform1,
    )

    private val versionPath: String
        get() {
            val nodeKey = BuiltInRegistries.ITEM_DATA_TYPE.getId(ItemDataTypes.VERSION)?.value()
                ?: error("ItemDataTypes.VERSION is not registered in BuiltInRegistries")
            return nodeKey
        }

    fun createItemTransformation(): ConfigurationTransformation {
        return ConfigurationTransformation.versionedBuilder()
            .versionKey(versionPath)
            .addVersions()
            .build()
    }

    fun versionNode(rootNode: ConfigurationNode): ConfigurationNode {
        return rootNode.node(versionPath)
    }

    @TestOnly
    fun addTransform(transform: KoishTransformation) {
        transforms.add(transform)
    }

    private fun ConfigurationTransformation.VersionedBuilder.addVersions(): ConfigurationTransformation.VersionedBuilder {
        transforms.forEach { transform -> this.addVersion(transform.version, transform) }
        return this
    }
}