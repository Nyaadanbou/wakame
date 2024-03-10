package cc.mewcraft.wakame.attribute.facade

/**
 * Used to convert a [SchemaAttributeData] into [PlainAttributeData].
 */
fun interface SchemaAttributeDataBaker {
    fun bake(schema: SchemaAttributeData, factor: Number): PlainAttributeData
}