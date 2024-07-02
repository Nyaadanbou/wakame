package cc.mewcraft.wakame.util

import cc.mewcraft.nbt.ByteArrayTag
import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.nbt.DoubleTag
import cc.mewcraft.nbt.FloatTag
import cc.mewcraft.nbt.IntArrayTag
import cc.mewcraft.nbt.ListTag
import cc.mewcraft.nbt.LongArrayTag
import cc.mewcraft.nbt.NumberTag
import cc.mewcraft.nbt.StringTag
import cc.mewcraft.nbt.Tag
import cc.mewcraft.nbt.TagType

object ShadowNbtUtils {

    fun createDoubleList(vararg doubles: Double): ListTag {
        val listTag = ListTag.create()
        doubles.forEach { listTag.add(DoubleTag.valueOf(it)) }
        return listTag
    }

    fun createFloatList(vararg floats: Float): ListTag {
        val listTag = ListTag.create()
        floats.forEach { listTag.add(FloatTag.valueOf(it)) }
        return listTag
    }

    fun createStringList(strings: Iterable<String>): ListTag {
        val listTag = ListTag.create()
        strings.forEach { listTag.add(StringTag.valueOf(it)) }
        return listTag
    }

    fun removeItemData(compoundTag: CompoundTag): CompoundTag {
        compoundTag.remove("Items")
        compoundTag.remove("HandItems")
        compoundTag.remove("ArmorItems")
        compoundTag.remove("SaddleItem")
        compoundTag.remove("Inventory")

        return compoundTag
    }

}

// TODO 2024/7/2 优化: 直接 get, 然后判断类型?

fun <T : Tag> CompoundTag.getOrPut(key: String, defaultValue: () -> T): T {
    if (contains(key)) {
        return (@Suppress("UNCHECKED_CAST") (get(key) as T))
    } else {
        val value = defaultValue()
        put(key, value)
        return value
    }
}

fun <T : Tag> CompoundTag.getOrNull(key: String): T? {
    return if (contains(key)) {
        @Suppress("UNCHECKED_CAST") (get(key) as? T)
    } else null
}

fun CompoundTag.getByteOrNull(key: String): Byte? {
    return if (contains(key, TagType.BYTE)) {
        (get(key) as NumberTag).byteValue()
    } else null
}

fun CompoundTag.getShortOrNull(key: String): Short? {
    return if (contains(key, TagType.SHORT)) {
        (get(key) as NumberTag).shortValue()
    } else null
}

fun CompoundTag.getIntOrNull(key: String): Int? {
    return if (contains(key, TagType.INT)) {
        (get(key) as NumberTag).intValue()
    } else null
}

fun CompoundTag.getLongOrNull(key: String): Long? {
    return if (contains(key, TagType.LONG)) {
        (get(key) as NumberTag).longValue()
    } else null
}

fun CompoundTag.getFloatOrNull(key: String): Float? {
    return if (contains(key, TagType.FLOAT)) {
        (get(key) as NumberTag).floatValue()
    } else null
}

fun CompoundTag.getDoubleOrNull(key: String): Double? {
    return if (contains(key, TagType.DOUBLE)) {
        (get(key) as NumberTag).doubleValue()
    } else null
}

fun CompoundTag.getStringOrNull(key: String): String? {
    return if (contains(key, TagType.STRING)) {
        (get(key) as StringTag).value()
    } else null
}

fun CompoundTag.getByteArrayOrNull(key: String): ByteArray? {
    return if (contains(key, TagType.BYTE_ARRAY)) {
        (get(key) as ByteArrayTag).value()
    } else null
}

fun CompoundTag.getIntArrayOrNull(key: String): IntArray? {
    return if (contains(key, TagType.INT_ARRAY)) {
        (get(key) as IntArrayTag).value()
    } else null
}

fun CompoundTag.getLongArrayOrNull(key: String): LongArray? {
    return if (contains(key, TagType.LONG_ARRAY)) {
        (get(key) as LongArrayTag).value()
    } else null
}

fun CompoundTag.getCompoundOrNull(key: String): CompoundTag? {
    return if (contains(key, TagType.COMPOUND)) {
        get(key) as CompoundTag
    } else null
}

fun CompoundTag.getListOrNull(key: String): ListTag? {
    return if (contains(key, TagType.LIST)) {
        get(key) as ListTag
    } else null
}

fun CompoundTag.getListOrNull(key: String, type: TagType): ListTag? {
    if (contains(key, TagType.LIST)) {
        val listTag = get(key) as ListTag
        if (listTag.elementType() == type) {
            return listTag
        }
    }

    return null
}

/* Kotlin-style builders */

fun CompoundTag(builder: CompoundTag.() -> Unit): CompoundTag =
    CompoundTag.create().apply(builder)

fun ListTag(builder: ListTag.() -> Unit): ListTag =
    ListTag.create().apply(builder)

fun ListTag(vararg tags: Tag): ListTag {
    val list = tags.asList()
    val type = list.first().type
    return ListTag.create(list, type)
}
