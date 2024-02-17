package cc.mewcraft.wakame.util

import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.*

object ShadowNbtUtils {

    fun createDoubleList(vararg doubles: Double): ListShadowTag {
        val listTag = ListShadowTag.create()
        doubles.forEach { listTag.add(DoubleShadowTag.valueOf(it)) }
        return listTag
    }

    fun createFloatList(vararg floats: Float): ListShadowTag {
        val listTag = ListShadowTag.create()
        floats.forEach { listTag.add(FloatShadowTag.valueOf(it)) }
        return listTag
    }

    fun createStringList(strings: Iterable<String>): ListShadowTag {
        val listTag = ListShadowTag.create()
        strings.forEach { listTag.add(StringShadowTag.valueOf(it)) }
        return listTag
    }

    fun removeItemData(compoundTag: CompoundShadowTag): CompoundShadowTag {
        compoundTag.remove("Items")
        compoundTag.remove("HandItems")
        compoundTag.remove("ArmorItems")
        compoundTag.remove("SaddleItem")
        compoundTag.remove("Inventory")

        return compoundTag
    }

}

@Suppress("UNCHECKED_CAST")
fun <T : ShadowTag> CompoundShadowTag.getOrPut(key: String, defaultValue: () -> T): T {
    if (contains(key))
        return get(key) as T

    val value = defaultValue()
    put(key, value)

    return value
}

@Suppress("UNCHECKED_CAST")
fun <T : ShadowTag> CompoundShadowTag.getOrNull(key: String): T? {
    return if (contains(key)) {
        get(key) as? T
    } else null
}

fun CompoundShadowTag.getByteOrNull(key: String): Byte? {
    if (contains(key, ShadowTagType.BYTE))
        return (get(key) as? NumberShadowTag)?.byteValue()

    return null
}

fun CompoundShadowTag.getShortOrNull(key: String): Short? {
    if (contains(key, ShadowTagType.SHORT))
        return (get(key) as? NumberShadowTag)?.shortValue()

    return null
}

fun CompoundShadowTag.getIntOrNull(key: String): Int? {
    if (contains(key, ShadowTagType.INT))
        return (get(key) as? NumberShadowTag)?.intValue()

    return null
}

fun CompoundShadowTag.getLongOrNull(key: String): Long? {
    if (contains(key, ShadowTagType.LONG))
        return (get(key) as? NumberShadowTag)?.longValue()

    return null
}

fun CompoundShadowTag.getFloatOrNull(key: String): Float? {
    if (contains(key, ShadowTagType.FLOAT))
        return (get(key) as? NumberShadowTag)?.floatValue()

    return null
}

fun CompoundShadowTag.getDoubleOrNull(key: String): Double? {
    if (contains(key, ShadowTagType.DOUBLE))
        return (get(key) as? NumberShadowTag)?.doubleValue()

    return null
}

fun CompoundShadowTag.getStringOrNull(key: String): String? {
    if (contains(key, ShadowTagType.STRING))
        return (get(key) as? StringShadowTag)?.value()

    return null
}

fun CompoundShadowTag.getByteArrayOrNull(key: String): ByteArray? {
    if (contains(key, ShadowTagType.BYTE_ARRAY))
        return (get(key) as? ByteArrayShadowTag)?.value()

    return null
}

fun CompoundShadowTag.getIntArrayOrNull(key: String): IntArray? {
    if (contains(key, ShadowTagType.INT_ARRAY))
        return (get(key) as? IntArrayShadowTag)?.value()

    return null
}

fun CompoundShadowTag.getLongArrayOrNull(key: String): LongArray? {
    if (contains(key, ShadowTagType.LONG_ARRAY))
        return (get(key) as? LongArrayShadowTag)?.value()

    return null
}

fun CompoundShadowTag.getCompoundOrNull(key: String): CompoundShadowTag? {
    if (contains(key, ShadowTagType.COMPOUND))
        return get(key) as? CompoundShadowTag

    return null
}

fun CompoundShadowTag.getListOrNull(key: String): ListShadowTag? {
    if (contains(key, ShadowTagType.LIST))
        return get(key) as? ListShadowTag

    return null
}

fun CompoundShadowTag.getListOrNull(key: String, type: ShadowTagType): ListShadowTag? {
    if (contains(key, ShadowTagType.LIST)) {
        val listTag = get(key) as ListShadowTag
        if (listTag.elementType() == type) {
            return listTag
        }
    }

    return null
}

/* Kotlin-style builders */

fun compoundShadowTag(builder: CompoundShadowTag.() -> Unit): CompoundShadowTag =
    CompoundShadowTag.create().apply(builder)

fun listShadowTag(builder: ListShadowTag.() -> Unit): ListShadowTag =
    ListShadowTag.create().apply(builder)

fun listShadowTag(vararg tags: ShadowTag): ListShadowTag {
    val list = tags.asList()
    val type = list.first().type
    return ListShadowTag.create(list, type)
}
