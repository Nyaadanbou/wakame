package eventbus

import cc.mewcraft.wakame.util.eventbus.FlowEventBus
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlowEventBusTest {

    data class TestEvent(val message: String)

    @Test
    fun `test basic functionality`() = runTest {
        val receivedEvents = mutableListOf<TestEvent>()
        val job = FlowEventBus.subscribe<TestEvent> {
            receivedEvents.add(it)
        }

        FlowEventBus.post(TestEvent("Hello, World!"))
        delay(100) // 等待事件处理

        assertEquals(1, receivedEvents.size)
        assertEquals("Hello, World!", receivedEvents[0].message)

        job.cancel()
    }

    @Test
    fun `test concurrent event posting`() = runTest {
        val receivedEvents = mutableListOf<TestEvent>()
        val job = FlowEventBus.subscribe<TestEvent> {
            receivedEvents.add(it)
        }

        val eventsToPost = 100
        coroutineScope {
            repeat(eventsToPost) { index ->
                launch {
                    FlowEventBus.post(TestEvent("Event #$index"))
                }
            }
        }
        delay(1000) // 等待所有事件处理

        assertEquals(eventsToPost, receivedEvents.size)
        val messages = receivedEvents.map { it.message }
        repeat(eventsToPost) { index ->
            assertTrue(messages.contains("Event #$index"))
        }

        job.cancel()
    }

    @Test
    fun `test exception handling in handler`() = runTest {
        val receivedEvents = mutableListOf<TestEvent>()
        val job = FlowEventBus.subscribe<TestEvent> {
            if (it.message == "Throw") {
                throw RuntimeException("Test Exception")
            } else {
                receivedEvents.add(it)
            }
        }

        FlowEventBus.post(TestEvent("Throw"))
        FlowEventBus.post(TestEvent("No Exception"))
        delay(100) // 等待事件处理

        assertEquals(1, receivedEvents.size)
        assertEquals("No Exception", receivedEvents[0].message)

        job.cancel()
    }
}
