package cc.mewcraft.wakame.util

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream
import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream
import net.kyori.adventure.nbt.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import java.io.*

/* Check if a key with given type exists */

fun CompoundBinaryTag.contains(key: String, type: BinaryTagType<*>): Boolean =
    get(key)?.let { type.test(it.type()) } == true

/* Nullable getters */

fun CompoundBinaryTag.getBooleanOrNull(key: String): Boolean? =
    if (this.contains(key, BinaryTagTypes.BYTE)) {
        val zero: Byte = 0
        getByte(key) != zero
    } else null

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
    if (this.contains(key, BinaryTagTypes.BYTE_ARRAY)) (get(key) as ByteArrayBinaryTag).value() else null

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
    if (this.contains(key, BinaryTagTypes.LONG_ARRAY)) (get(key) as LongArrayBinaryTag).value() else null

/* Throwable getters */

fun CompoundBinaryTag.getBooleanOrThrow(key: String): Boolean =
    getBooleanOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getByteOrThrow(key: String): Byte =
    getByteOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getShortOrThrow(key: String): Short =
    getShortOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getIntOrThrow(key: String): Int =
    getIntOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getLongOrThrow(key: String): Long =
    getLongOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getFloatOrThrow(key: String): Float =
    getFloatOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getDoubleOrThrow(key: String): Double =
    getDoubleOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getByteArrayOrThrow(key: String): ByteArray =
    getByteArrayOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getStringOrThrow(key: String): String =
    getStringOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getListOrThrow(key: String): ListBinaryTag =
    getListOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getListOrThrow(key: String, expectedType: BinaryTagType<out BinaryTag>): ListBinaryTag =
    getListOrNull(key, expectedType) ?: throw NullPointerException()

fun CompoundBinaryTag.getCompoundOrThrow(key: String): CompoundBinaryTag =
    getCompoundOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getIntArrayOrThrow(key: String): IntArray =
    getIntArrayOrNull(key) ?: throw NullPointerException()

fun CompoundBinaryTag.getLongArrayOrThrow(key: String): LongArray =
    getLongArrayOrNull(key) ?: throw NullPointerException()

/* Kotlin-style builders */

fun CompoundBinaryTag(base: CompoundBinaryTag, builder: CompoundBinaryTag.Builder.() -> Unit): CompoundBinaryTag =
    CompoundBinaryTag.builder().put(base).apply(builder).build()

fun CompoundBinaryTag(builder: CompoundBinaryTag.Builder.() -> Unit): CompoundBinaryTag =
    CompoundBinaryTag.builder().apply(builder).build()

fun ListBinaryTag(builder: ListBinaryTag.Builder<BinaryTag>.() -> Unit): ListBinaryTag =
    ListBinaryTag.builder().apply(builder).build()

fun ListBinaryTag(vararg tags: BinaryTag): ListBinaryTag =
    ListBinaryTag { tags.forEach(this::add) }

private val CompoundTag.toAdventure: CompoundBinaryTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        val dataOutputStream = DataOutputStream(arrayOutputStream)
        NbtIo.write(this, dataOutputStream)
        val dataInputStream: InputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return BinaryTagIO.reader().read(dataInputStream)
    }

private val CompoundBinaryTag.toMinecraft: CompoundTag
    get() {
        val arrayOutputStream = FastByteArrayOutputStream()
        BinaryTagIO.writer().write(this, arrayOutputStream)
        val dataInputStream = DataInputStream(FastByteArrayInputStream(arrayOutputStream.array))
        return NbtIo.read(dataInputStream)
    }