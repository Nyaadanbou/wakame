package cc.mewcraft.wakame.item.schema.cell

import org.spongepowered.configurate.ConfigurationNode

object SchemaCellFactory {
    /**
     * Creates an empty cell.
     */
    fun empty(): SchemaCell = EmptySchemaCell

    /**
     * Creates a [SchemaCell] from given configuration nodes.
     */
    @Deprecated("实现已转移到专门的 TypeSerializer")
    fun schemaOf(
        id: String,
        cellNode: ConfigurationNode,
        coreNode: ConfigurationNode?,
        curseNode: ConfigurationNode?,
    ): SchemaCell {
        throw UnsupportedOperationException()
    }
}