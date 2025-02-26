package cc.mewcraft.wakame.util.data

import me.lucko.shadow.*
import me.lucko.shadow.bukkit.BukkitShadowFactory
import net.minecraft.nbt.*

object NbtUtils {

    const val TAG_END = 0
    const val TAG_BYTE = 1
    const val TAG_SHORT = 2
    const val TAG_INT = 3
    const val TAG_LONG = 4
    const val TAG_FLOAT = 5
    const val TAG_DOUBLE = 6
    const val TAG_BYTE_ARRAY = 7
    const val TAG_STRING = 8
    const val TAG_LIST = 9
    const val TAG_COMPOUND = 10
    const val TAG_INT_ARRAY = 11
    const val TAG_LONG_ARRAY = 12
    const val TAG_ANY_NUMERIC = 99

    fun doubleListTag(vararg doubles: Double): ListTag {
        val listTag = ListTag()

        doubles.forEach { listTag.add(DoubleTag.valueOf(it)) }
        return listTag
    }

    fun floatListTag(vararg floats: Float): ListTag {
        val listTag = ListTag()
        floats.forEach { listTag.add(FloatTag.valueOf(it)) }
        return listTag
    }

    fun stringListTag(strings: Iterable<String>): ListTag {
        val listTag = ListTag()
        strings.forEach { listTag.add(StringTag.valueOf(it)) }
        return listTag
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Tag> CompoundTag.getOrPut(key: String, defaultValue: () -> T): T {
    if (contains(key))
        return get(key) as T

    val value = defaultValue()
    put(key, value)

    return value
}

@Suppress("UNCHECKED_CAST")
fun <T : Tag> CompoundTag.getOrNull(key: String): T? {
    return if (contains(key)) {
        get(key) as? T
    } else null
}

fun CompoundTag.getByteOrNull(key: String): Byte? {
    if (contains(key, NbtUtils.TAG_BYTE))
        return (get(key) as? NumericTag)?.asByte

    return null
}

fun CompoundTag.getShortOrNull(key: String): Short? {
    if (contains(key, NbtUtils.TAG_SHORT))
        return (get(key) as? NumericTag)?.asShort

    return null
}

fun CompoundTag.getIntOrNull(key: String): Int? {
    if (contains(key, NbtUtils.TAG_INT))
        return (get(key) as? NumericTag)?.asInt

    return null
}

fun CompoundTag.getLongOrNull(key: String): Long? {
    if (contains(key, NbtUtils.TAG_LONG))
        return (get(key) as? NumericTag)?.asLong

    return null
}

fun CompoundTag.getFloatOrNull(key: String): Float? {
    if (contains(key, NbtUtils.TAG_FLOAT))
        return (get(key) as? NumericTag)?.asFloat

    return null
}

fun CompoundTag.getDoubleOrNull(key: String): Double? {
    if (contains(key, NbtUtils.TAG_DOUBLE))
        return (get(key) as? NumericTag)?.asDouble

    return null
}

fun CompoundTag.getStringOrNull(key: String): String? {
    if (contains(key, NbtUtils.TAG_STRING))
        return (get(key) as? StringTag)?.asString

    return null
}

fun CompoundTag.getByteArrayOrNull(key: String): ByteArray? {
    if (contains(key, NbtUtils.TAG_BYTE_ARRAY))
        return (get(key) as? ByteArrayTag)?.asByteArray

    return null
}

fun CompoundTag.getIntArrayOrNull(key: String): IntArray? {
    if (contains(key, NbtUtils.TAG_INT_ARRAY))
        return (get(key) as? IntArrayTag)?.asIntArray

    return null
}

fun CompoundTag.getLongArrayOrNull(key: String): LongArray? {
    if (contains(key, NbtUtils.TAG_LONG_ARRAY))
        return (get(key) as? LongArrayTag)?.asLongArray

    return null
}

fun CompoundTag.getCompoundOrNull(key: String): CompoundTag? {
    if (contains(key, NbtUtils.TAG_COMPOUND))
        return get(key) as? CompoundTag

    return null
}

fun CompoundTag.getListOrNull(key: String): ListTag? {
    if (contains(key, NbtUtils.TAG_LIST))
        return get(key) as? ListTag

    return null
}

fun CompoundTag.getListOrNull(key: String, type: Int): ListTag? {
    if (contains(key, NbtUtils.TAG_LIST)) {
        val listTag = get(key) as ListTag
        if (listTag.elementType.compareTo(type) == 0) {
            return listTag
        }
    }

    return null
}

fun CompoundTag.keySet(): Set<String> = allKeys

fun CompoundTag.removeAll() {
    BukkitShadowFactory.global().shadow<ShadowCompoundTag>(this).removeAll()
}

/* Kotlin-style builders */

fun ListTag(block: ListTag.() -> Unit): ListTag =
    ListTag().apply(block)

fun CompoundTag(block: CompoundTag.() -> Unit): CompoundTag =
    CompoundTag().apply(block)


/* Private */


@ClassTarget(CompoundTag::class)
private interface ShadowCompoundTag : Shadow {

    @Field
    @Target("tags")
    @ShadowingStrategy(wrapper = ForTypelessMaps::class)
    fun tags(): MutableMap<Any?, Any?>

    fun removeAll() = tags().clear()

}

/**
 * A wrapper/unwrapper for a Map of Tag objects.
 *
 * The shadow Map is a view of its shadow target - any changes on the shadow Map
 * will reflect on its shadow target.
 *
 * This shadowing strategy is only intended to expose the typeless methods of the shadow map,
 * such as [MutableMap.clear] and [MutableMap.isEmpty].
 */
enum class ForTypelessMaps : ShadowingStrategy.Wrapper, ShadowingStrategy.Unwrapper {
    INSTANCE;

    override fun wrap(unwrapped: Any?, expectedType: Class<*>, shadowFactory: ShadowFactory): Map<*, *>? = unwrapped as? Map<*, *>
    override fun unwrap(wrapped: Any?, expectedType: Class<*>, shadowFactory: ShadowFactory): Any? = throw UnsupportedOperationException()
    override fun unwrap(wrappedClass: Class<*>?, shadowFactory: ShadowFactory): Class<*> = throw UnsupportedOperationException()
}