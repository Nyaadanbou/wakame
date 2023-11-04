package cc.mewcraft.wakame.util

import net.kyori.adventure.nbt.*

fun CompoundBinaryTag.contains(key: String, type: BinaryTagType<*>): Boolean =
    get(key)?.let { type.test(it.type()) } == true

fun CompoundBinaryTag.getBooleanOrNull(key: String): Boolean? =
    if (this.contains(key, BinaryTagTypes.BYTE)) getByte(key) != 0.toByte() else null

fun CompoundBinaryTag.getByteOrNull(key: String): Byte? =
    if (this.contains(key, BinaryTagTypes.BYTE)) (get(key) as NumberBinaryTag).byteValue() else null

fun CompoundBinaryTag.getShortOrNull(key: String): Short? =
    if (this.contains(key, BinaryTagTypes.SHORT)) (get(key) as NumberBinaryTag).shortValue() else null

fun CompoundBinaryTag.getIntOrNull(key: String): Int? =
    if (this.contains(key, BinaryTagTypes.INT)) (get(key) as NumberBinaryTag).intValue() else null

fun CompoundBinaryTag.getLongOrNull(key: String): Long? =
    if (this.contains(key, BinaryTagTypes.LONG)) (get(key) as NumberBinaryTag).longValue() else null

fun CompoundBinaryTag.getFloatOrNull(key: String): Float? =
    if (this.contains(key, BinaryTagTypes.FLOAT)) (get(key) as NumberBinaryTag).floatValue() else null

fun CompoundBinaryTag.getDoubleOrNull(key: String): Double? =
    if (this.contains(key, BinaryTagTypes.DOUBLE)) (get(key) as NumberBinaryTag).doubleValue() else null

fun CompoundBinaryTag.getByteArrayOrNull(key: String): ByteArray? =
    if (this.contains(key, BinaryTagTypes.BYTE_ARRAY)) (this.get(key) as ByteArrayBinaryTag).value() else null

fun CompoundBinaryTag.getStringOrNull(key: String): String? =
    if (this.contains(key, BinaryTagTypes.STRING)) (get(key) as StringBinaryTag).value() else null

fun CompoundBinaryTag.getListOrNull(key: String): ListBinaryTag? =
    if (this.contains(key, BinaryTagTypes.LIST)) get(key) as ListBinaryTag else null

fun CompoundBinaryTag.getListOrNull(key: String, expectedType: BinaryTagType<out BinaryTag>): ListBinaryTag? {
    if (this.contains(key, BinaryTagTypes.LIST)) {
        val tag = get(key) as ListBinaryTag
        if (expectedType.test(tag.elementType())) {
            return tag
        }
    }
    return null
}

fun CompoundBinaryTag.getCompoundOrNull(key: String): CompoundBinaryTag? =
    if (this.contains(key, BinaryTagTypes.COMPOUND)) get(key) as CompoundBinaryTag else null

fun CompoundBinaryTag.getIntArrayOrNull(key: String): IntArray? =
    if (this.contains(key, BinaryTagTypes.INT_ARRAY)) (get(key) as IntArrayBinaryTag).value() else null

fun CompoundBinaryTag.getLongArrayOrNull(key: String): LongArray? =
    if (this.contains(key, BinaryTagTypes.LONG_ARRAY)) (get(key) as LongArrayBinaryTag).value() else LongArray(0)

fun compoundBinaryTag(builder: CompoundBinaryTag.Builder.() -> Unit): CompoundBinaryTag =
    CompoundBinaryTag.builder().apply(builder).build()

fun listBinaryTag(builder: ListBinaryTag.Builder<BinaryTag>.() -> Unit): ListBinaryTag =
    ListBinaryTag.builder().apply(builder).build()

fun listBinaryTag(vararg tags: BinaryTag): ListBinaryTag =
    listBinaryTag { tags.forEach(this::add) }