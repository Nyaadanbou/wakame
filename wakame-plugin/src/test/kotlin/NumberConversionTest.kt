import cc.mewcraft.wakame.util.toStableByte
import cc.mewcraft.wakame.util.toStableDouble
import cc.mewcraft.wakame.util.toStableFloat
import cc.mewcraft.wakame.util.toStableInt
import cc.mewcraft.wakame.util.toStableShort
import cc.mewcraft.wakame.util.toStableLong
import org.junit.Test
import kotlin.test.assertEquals

class NumberExtensionsTest {

    private val floatTolerance = 0.0001f
    private val doubleTolerance = 0.0001

    @Test
    fun testByteConversions() {
        val testByte: Byte = 100
        assertEquals(testByte.toShort(), testByte.toStableShort())
        assertEquals(testByte.toInt(), testByte.toStableInt())
        assertEquals(testByte.toLong(), testByte.toStableLong())
        assertEquals(testByte.toFloat(), testByte.toStableFloat(), floatTolerance)
        assertEquals(testByte.toDouble(), testByte.toStableDouble(), doubleTolerance)
    }

    @Test
    fun testShortConversions() {
        val testShort: Short = 10000
        assertEquals(Byte.MAX_VALUE, testShort.toStableByte())
        assertEquals(testShort.toInt(), testShort.toStableInt())
        assertEquals(testShort.toLong(), testShort.toStableLong())
        assertEquals(testShort.toFloat(), testShort.toStableFloat(), floatTolerance)
        assertEquals(testShort.toDouble(), testShort.toStableDouble(), doubleTolerance)
    }

    @Test
    fun testIntConversions() {
        val testInt = 100000
        assertEquals(Byte.MAX_VALUE, testInt.toStableByte())
        assertEquals(Short.MAX_VALUE, testInt.toStableShort())
        assertEquals(testInt.toLong(), testInt.toStableLong())
        assertEquals(testInt.toFloat(), testInt.toStableFloat(), floatTolerance)
        assertEquals(testInt.toDouble(), testInt.toStableDouble(), doubleTolerance)
    }

    @Test
    fun testLongConversions() {
        val testLong = 100000L
        assertEquals(Byte.MAX_VALUE, testLong.toStableByte())
        assertEquals(Short.MAX_VALUE, testLong.toStableShort())
        assertEquals(testLong.toInt(), testLong.toStableInt())
        assertEquals(testLong.toFloat(), testLong.toStableFloat(), floatTolerance)
        assertEquals(testLong.toDouble(), testLong.toStableDouble(), doubleTolerance)
    }

    @Test
    fun testFloatConversions() {
        val testFloat = 100000f
        assertEquals(Byte.MAX_VALUE, testFloat.toStableByte())
        assertEquals(Short.MAX_VALUE, testFloat.toStableShort())
        assertEquals(testFloat.toInt(), testFloat.toStableInt())
        assertEquals(testFloat.toLong(), testFloat.toStableLong())
        assertEquals(testFloat.toDouble(), testFloat.toStableDouble(), doubleTolerance)
    }

    @Test
    fun testDoubleConversions() {
        val testDouble = 100000.0
        assertEquals(Byte.MAX_VALUE, testDouble.toStableByte())
        assertEquals(Short.MAX_VALUE, testDouble.toStableShort())
        assertEquals(testDouble.toInt(), testDouble.toStableInt())
        assertEquals(testDouble.toLong(), testDouble.toStableLong())
        assertEquals(testDouble.toFloat(), testDouble.toStableFloat(), floatTolerance)
    }
}