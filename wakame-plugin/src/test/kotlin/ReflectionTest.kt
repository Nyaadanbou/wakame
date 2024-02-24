import cc.mewcraft.wakame.util.*
import me.lucko.helper.nbt.ShadowTagType
import java.lang.invoke.MethodHandle
import java.util.EnumMap
import kotlin.test.Test
import kotlin.test.assertEquals

// Map Value's KFunction Type: (Number) -> Number
private val TAG_TYPE_2_NUMBER_CONVERTER_MAP: Map<ShadowTagType, MethodHandle> = buildMap {
    this[ShadowTagType.BYTE] = Number::toStableByte
    this[ShadowTagType.SHORT] = Number::toStableShort
    this[ShadowTagType.INT] = Number::toStableInt
    this[ShadowTagType.LONG] = Number::toStableLong
    this[ShadowTagType.FLOAT] = Number::toStableFloat
    this[ShadowTagType.DOUBLE] = Number::toStableDouble
}.mapValues { it.value.toMethodHandle() }.let { EnumMap(it) }

class ReflectionTest {
    @Test
    fun `convert number from shadow tag types`() {
        listOf(
            ShadowTagType.BYTE,
            ShadowTagType.SHORT,
            ShadowTagType.INT,
            ShadowTagType.LONG,
            ShadowTagType.FLOAT,
            ShadowTagType.DOUBLE
        ).forEach {
            val converted = TAG_TYPE_2_NUMBER_CONVERTER_MAP.getOrThrow(it).invoke(3.0)
            val expected = converted::class.simpleName?.lowercase()
            val actual = it.name.lowercase()
            assertEquals(expected, actual)
            println("Expected: $expected, Actual: $actual")
        }
    }
}