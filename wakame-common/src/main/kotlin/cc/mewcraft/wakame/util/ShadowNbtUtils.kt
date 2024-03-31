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

fun <T : ShadowTag> CompoundShadowTag.getOrPut(key: String, defaultValue: () -> T): T {
    if (contains(key)) {
        return (@Suppress("UNCHECKED_CAST") (get(key) as T))
    } else {
        val value = defaultValue()
        put(key, value)
        return value
    }
}

fun <T : ShadowTag> CompoundShadowTag.getOrNull(key: String): T? {
    return if (contains(key)) {
        @Suppress("UNCHECKED_CAST") (get(key) as? T)
    } else null
}

fun CompoundShadowTag.getByteOrNull(key: String): Byte? {
    return if (contains(key, ShadowTagType.BYTE)) {
        (get(key) as NumberShadowTag).byteValue()
    } else null
}

fun CompoundShadowTag.getShortOrNull(key: String): Short? {
    return if (contains(key, ShadowTagType.SHORT)) {
        (get(key) as NumberShadowTag).shortValue()
    } else null
}

fun CompoundShadowTag.getIntOrNull(key: String): Int? {
    return if (contains(key, ShadowTagType.INT)) {
        (get(key) as NumberShadowTag).intValue()
    } else null
}

fun CompoundShadowTag.getLongOrNull(key: String): Long? {
    return if (contains(key, ShadowTagType.LONG)) {
        (get(key) as NumberShadowTag).longValue()
    } else null
}

fun CompoundShadowTag.getFloatOrNull(key: String): Float? {
    return if (contains(key, ShadowTagType.FLOAT)) {
        (get(key) as NumberShadowTag).floatValue()
    } else null
}

fun CompoundShadowTag.getDoubleOrNull(key: String): Double? {
    return if (contains(key, ShadowTagType.DOUBLE)) {
        (get(key) as NumberShadowTag).doubleValue()
    } else null
}

fun CompoundShadowTag.getStringOrNull(key: String): String? {
    return if (contains(key, ShadowTagType.STRING)) {
        (get(key) as StringShadowTag).value()
    } else null
}

fun CompoundShadowTag.getByteArrayOrNull(key: String): ByteArray? {
    return if (contains(key, ShadowTagType.BYTE_ARRAY)) {
        (get(key) as ByteArrayShadowTag).value()
    } else null
}

fun CompoundShadowTag.getIntArrayOrNull(key: String): IntArray? {
    return if (contains(key, ShadowTagType.INT_ARRAY)) {
        (get(key) as IntArrayShadowTag).value()
    } else null
}

fun CompoundShadowTag.getLongArrayOrNull(key: String): LongArray? {
    return if (contains(key, ShadowTagType.LONG_ARRAY)) {
        (get(key) as LongArrayShadowTag).value()
    } else null
}

fun CompoundShadowTag.getCompoundOrNull(key: String): CompoundShadowTag? {
    return if (contains(key, ShadowTagType.COMPOUND)) {
        get(key) as CompoundShadowTag
    } else null
}

fun CompoundShadowTag.getListOrNull(key: String): ListShadowTag? {
    return if (contains(key, ShadowTagType.LIST)) {
        get(key) as ListShadowTag
    } else null
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

fun CompoundShadowTag(builder: CompoundShadowTag.() -> Unit): CompoundShadowTag =
    CompoundShadowTag.create().apply(builder)

fun ListShadowTag(builder: ListShadowTag.() -> Unit): ListShadowTag =
    ListShadowTag.create().apply(builder)

fun ListShadowTag(vararg tags: ShadowTag): ListShadowTag {
    val list = tags.asList()
    val type = list.first().type
    return ListShadowTag.create(list, type)
}
