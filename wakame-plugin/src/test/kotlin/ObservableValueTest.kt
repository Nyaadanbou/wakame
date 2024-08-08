import cc.mewcraft.wakame.util.ObservableDelegates
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.assertTrue

class ObservableValueTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(testEnv())
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun testObservableReference() {
        var intRef by ObservableDelegates.reference(0)
        // 假设我们有一个方式来捕获日志输出，此处为示例
        intRef = 1
    }

    @Test
    fun testObservableCollection() {
        val intCollection: MutableCollection<Int> by ObservableDelegates.collection(mutableListOf())
        intCollection.add(1)
        assertEquals(1, intCollection.size, "The collection should contain one element.")

        // 这里添加更多asserts来验证其他方法，如remove(), clear()等
        intCollection.remove(1)
        assertEquals(0, intCollection.size, "The collection should be empty.")

        intCollection.add(1)
        intCollection.clear()
        assertTrue(intCollection.isEmpty(), "The collection should be empty.")
    }

    @Test
    fun testObservableList() {
        val intList: MutableList<Int> by ObservableDelegates.list(mutableListOf())
        intList.add(0, 1)
        assertEquals(1, intList[0], "The list should contain the element at index 0.")

        // 测试其他List特有方法
        intList.add(0, 2)
        assertEquals(2, intList[0], "The list should contain the element at index 0.")

        intList.removeAt(0)
        assertEquals(1, intList[0], "The list should contain the element at index 0.")
    }

    @Test
    fun testObservableSet() {
        val intSet: MutableSet<Int> by ObservableDelegates.set(mutableSetOf())
        intSet.add(1)
        assertTrue(intSet.contains(1), "The set should contain the element.")

        // 测试Set特有的行为，比如添加重复元素
        intSet.add(1)
        assertEquals(1, intSet.size, "The set should contain only one element.")
    }

    @Test
    fun testObservableMap() {
        val int2StringMap: MutableMap<Int, String> by ObservableDelegates.map(mutableMapOf())
        int2StringMap[1] = "One"
        assertEquals("One", int2StringMap[1], "The map should contain the value associated with key 1.")

        // 测试其他Map方法，如remove(), clear()
        int2StringMap.remove(1)
        assertTrue(int2StringMap.isEmpty(), "The map should be empty.")

        int2StringMap[1] = "One"
        int2StringMap.clear()
        assertTrue(int2StringMap.isEmpty(), "The map should be empty.")
    }

    @Test
    fun testObservableContainer() {
        val container = ObservableContainer()
        container.intRef = 1
        assertEquals(1, container.intRef, "The observable reference should be 1.")

        container.intSet.add(1)
        assertTrue(container.intSet.contains(1), "The number set should contain the element.")
    }
}

private class ObservableContainer {
    var intRef by ObservableDelegates.reference(0)
    val intSet: MutableSet<Int> by ObservableDelegates.set(mutableSetOf())
}