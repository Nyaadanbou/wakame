package cc.mewcraft.wakame.item.schema.cell

import org.spongepowered.configurate.ConfigurationNode

object SchemaCellFactory {
    /**
     * Creates an empty cell.
     */
    @Deprecated("我们不需要一个类来表示“空词条栏”，因为拥有“空核心”的词条栏就是“空词条栏”")
    fun empty(): SchemaCell {
        throw UnsupportedOperationException()
    }

    /**
     * Creates a [SchemaCell] from given configuration nodes.
     */
    @Deprecated("不要使用这个函数，完整的实现已转移到专门的 TypeSerializer")
    fun schemaOf(
        id: String,
        cellNode: ConfigurationNode,
        coreNode: ConfigurationNode?,
        curseNode: ConfigurationNode?,
    ): SchemaCell {
        throw UnsupportedOperationException()
    }
}