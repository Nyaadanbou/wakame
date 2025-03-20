package util

import cc.mewcraft.wakame.util.RecursionGuard
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class RecursionGuardTest {

    @BeforeEach
    fun setup() {
        // 确保每个测试前重置状态
        RecursionGuard.exit("test1")
        RecursionGuard.exit("test2")
        RecursionGuard.exit("test3")
        RecursionGuard.exit("A")
        RecursionGuard.exit("B")
        RecursionGuard.exit("multi-thread")
        RecursionGuard.exit("non-existent")
    }

    // 场景 1: 正常调用无递归
    @Test
    fun `normal execution should work`() {
        fun normalCall(): Int? = RecursionGuard.withValue("test1", silence = false) { 42 }
        val result = normalCall()

        assertEquals(42, result)
    }

    // 场景 2: 直接递归调用应被拦截
    @Test
    fun `direct recursion should be blocked`() {
        var count = 0
        fun recursiveCall(): Unit = RecursionGuard.with("test2", silenceLogs = false) {
            count++
            if (count < 3) {
                recursiveCall() // 触发递归
            }
        }
        recursiveCall()

        assertEquals(1, count) // 只有第一次调用被执行
    }

    // 场景 3: 间接递归调用应被拦截
    private var countA = 0
    private var countB = 0

    private fun methodA(): Unit = RecursionGuard.with("A", silenceLogs = false) {
        countA++
        methodB()
    }

    private fun methodB(): Unit = RecursionGuard.with("B", silenceLogs = false) {
        countB++
        methodA() // 间接递归
    }

    @Test
    fun `indirect recursion should be blocked`() {
        methodA()

        assertEquals(1, countA)
        assertEquals(1, countB)
    }

    // 场景 4: 多线程环境下互不影响
    @Test
    fun `should work independently in different threads`() {
        val threadPool = Executors.newFixedThreadPool(2)
        val latch = CountDownLatch(2)
        var results = mutableListOf<Int>()
        fun multiThread(i: Int): Int? = RecursionGuard.withValue("multi-thread", silence = false) {
            Thread.sleep(10)
            100 + i
        }

        repeat(2) { i ->
            threadPool.submit {
                val result = multiThread(i)
                results.add(result ?: -1)
                latch.countDown()
            }
        }

        latch.await()

        assertTrue(results.containsAll(listOf(100, 101)))
    }

    // 场景 5: 多次正常调用后状态应正确
    @Test
    fun `should reset counter after multiple normal calls`() {
        fun repeatCall(): Unit = RecursionGuard.with("test3", silenceLogs = false) { /* 空操作 */ }
        repeat(3) { repeatCall() }

        // 最终深度应为 0
        assertNull(RecursionGuard.callDepthMap.get()["test3"])
    }

    // 边界条件: 处理不存在的方法名
    @Test
    fun `exit non-existent method should do nothing`() {
        assertDoesNotThrow {
            RecursionGuard.exit("non-existent")
        }
    }

}
