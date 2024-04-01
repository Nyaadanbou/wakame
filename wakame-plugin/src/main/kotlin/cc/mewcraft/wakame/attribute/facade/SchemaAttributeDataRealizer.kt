package cc.mewcraft.wakame.attribute.facade

/**
 * Used to convert a [SchemaAttributeData] into [BinaryAttributeData].
 */
fun interface SchemaAttributeDataRealizer {
    fun realize(schema: SchemaAttributeData, factor: Number): BinaryAttributeData
}