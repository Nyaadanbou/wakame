import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap
import kotlin.test.Test
import kotlin.test.assertEquals

class MapPerformanceTest {

    companion object {
        private const val ITERATIONS = 1_000_000
    }

    @Test
    fun testHashMapPerformance() {
        val hashMap = HashMap<String, String>()
        val startTime = System.nanoTime()

        for (i in 0 until ITERATIONS) {
            hashMap["key1"] = "value1"
            hashMap["key2"] = "value2"
            hashMap["key3"] = "value3"
            assertEquals("value1", hashMap["key1"])
            assertEquals("value2", hashMap["key2"])
            assertEquals("value3", hashMap["key3"])
        }

        val endTime = System.nanoTime()
        val duration = endTime - startTime
        println("HashMap time: $duration ns")
    }

    @Test
    fun testObject2ObjectArrayMapPerformance() {
        val arrayMap = Object2ReferenceArrayMap<String, String>()
        val startTime = System.nanoTime()

        for (i in 0 until ITERATIONS) {
            arrayMap["key1"] = "value1"
            arrayMap["key2"] = "value2"
            arrayMap["key3"] = "value3"
            assertEquals("value1", arrayMap["key1"])
            assertEquals("value2", arrayMap["key2"])
            assertEquals("value3", arrayMap["key3"])
        }

        val endTime = System.nanoTime()
        val duration = endTime - startTime
        println("Object2ObjectArrayMap time: $duration ns")
    }
}
