import cc.mewcraft.nbt.TagType
import cc.mewcraft.wakame.util.toMethodHandle
import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.toStableDouble
import cc.mewcraft.wakame.util.toStableFloat
import cc.mewcraft.wakame.util.toStableInt
import cc.mewcraft.wakame.util.toStableLong
import cc.mewcraft.wakame.util.toStableShort
import java.lang.invoke.MethodHandle
import java.util.EnumMap
import kotlin.test.Test
import kotlin.test.assertEquals

// Map Value's KFunction Type: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<TagType, MethodHandle> = buildMap {
    this[TagType.BYTE] = Number::toStableByte
    this[TagType.SHORT] = Number::toStableShort
    this[TagType.INT] = Number::toStableInt
    this[TagType.LONG] = Number::toStableLong
    this[TagType.FLOAT] = Number::toStableFloat
    this[TagType.DOUBLE] = Number::toStableDouble
}.mapValues { it.value.toMethodHandle() }.let { EnumMap(it) }

class ReflectionTest {
    @Test
    fun `convert number from shadow tag types`() {
        listOf(
            TagType.BYTE,
            TagType.SHORT,
            TagType.INT,
            TagType.LONG,
            TagType.FLOAT,
            TagType.DOUBLE
        ).forEach {
            val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getValue(it).invoke(3.0)
            val expected = converted::class.simpleName?.lowercase()
            val actual = it.name.lowercase()
            assertEquals(expected, actual)
            println("Expected: $expected, Actual: $actual")
        }
    }
}