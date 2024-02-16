import cc.mewcraft.wakame.util.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.test.assertTrue

class WatchedValueTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            startKoin {
                modules(
                    module {
                        single<Logger> {
                            LoggerFactory.getLogger("MutableCollectionSelectionContextWatcherTest")
                        }
                    }
                )
            }
        }
    }

    @Test
    fun testWatchedPrimitive() {
        val initialValue = 0
        var watchedPrimitive by WatchedPrimitive(initialValue)
        // 假设我们有一个方式来捕获日志输出，此处为示例
        watchedPrimitive = 1
    }

    @Test
    fun testWatchedCollection() {
        val numberSets: MutableCollection<Int> by WatchedCollection(mutableListOf())
        numberSets.add(1)
        assertEquals(1, numberSets.size, "The collection should contain one element.")

        // 这里添加更多asserts来验证其他方法，如remove(), clear()等
        numberSets.remove(1)
        assertEquals(0, numberSets.size, "The collection should be empty.")

        numberSets.add(1)
        numberSets.clear()
        assertTrue(numberSets.isEmpty(), "The collection should be empty.")
    }

    @Test
    fun testWatchedList() {
        val numberList: MutableList<Int> by WatchedList(mutableListOf())
        numberList.add(0, 1)
        assertEquals(1, numberList[0], "The list should contain the element at index 0.")

        // 测试其他List特有方法
        numberList.add(0, 2)
        assertEquals(2, numberList[0], "The list should contain the element at index 0.")

        numberList.removeAt(0)
        assertEquals(1, numberList[0], "The list should contain the element at index 0.")
    }

    @Test
    fun testWatchedSet() {
        val numberSet: MutableSet<Int> by WatchedSet(mutableSetOf())
        numberSet.add(1)
        assertTrue(numberSet.contains(1), "The set should contain the element.")

        // 测试Set特有的行为，比如添加重复元素
        numberSet.add(1)
        assertEquals(1, numberSet.size, "The set should contain only one element.")
    }

    @Test
    fun testWatchedMap() {
        val watchedMap: MutableMap<Int, String> by WatchedMap(mutableMapOf())
        watchedMap[1] = "One"
        assertEquals("One", watchedMap[1], "The map should contain the value associated with key 1.")

        // 测试其他Map方法，如remove(), clear()
        watchedMap.remove(1)
        assertTrue(watchedMap.isEmpty(), "The map should be empty.")

        watchedMap[1] = "One"
        watchedMap.clear()
        assertTrue(watchedMap.isEmpty(), "The map should be empty.")
    }
}