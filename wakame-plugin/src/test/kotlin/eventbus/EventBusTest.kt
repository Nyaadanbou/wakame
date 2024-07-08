package eventbus

import cc.mewcraft.wakame.eventbus.EventBus
import cc.mewcraft.wakame.eventbus.subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.Executors
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

data class TestEvent(val x: String)

class EventBusTest {

    private lateinit var eventBus: EventBus
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setUp() {
        // 实际上 EventBus 是跑在单线程上的, 因此这里要特别指明单线程的上下文
        testScope = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher() + SupervisorJob())
        eventBus = EventBus(testScope)
    }

    @AfterTest
    fun tearDown() {
        eventBus.close()
        testScope.cancel()
    }

    @Test
    fun `test event bus`(): Unit = runBlocking {
        val eventReceived = Channel<TestEvent>()

        eventBus.subscribe<TestEvent> {
            eventReceived.send(it)
        }

        val testEvent = TestEvent("TestUser")
        eventBus.post(testEvent)

        val receivedEvent = withTimeoutOrNull(1000) {
            eventReceived.receive()
        }
        assertNotNull(receivedEvent)
        assertEquals(testEvent, receivedEvent)
    }

    @Test
    fun `test event handler exception`(): Unit = runBlocking {
        val receivedEvents = Channel<TestEvent>(capacity = Channel.UNLIMITED)

        eventBus.subscribe<TestEvent> { event ->
            receivedEvents.send(event)
        }

        eventBus.subscribe<TestEvent> { event ->
            receivedEvents.send(event)
        }

        val testEvent1 = TestEvent("User1")
        val testEvent2 = TestEvent("User2")
        eventBus.post(testEvent1)
        eventBus.post(testEvent2)

        val receivedEvent1 = withTimeoutOrNull(1000) { receivedEvents.receive() }
        val receivedEvent2 = withTimeoutOrNull(1000) { receivedEvents.receive() }
        val receivedEvent3 = withTimeoutOrNull(1000) { receivedEvents.receive() }
        val receivedEvent4 = withTimeoutOrNull(2000) { receivedEvents.receive() }

        // 检查所有事件都已接收
        assertNotNull(receivedEvent1)
        assertNotNull(receivedEvent2)
        assertNotNull(receivedEvent3)
        assertNotNull(receivedEvent4)

        val receivedEventList = listOf(receivedEvent1, receivedEvent2, receivedEvent3, receivedEvent4)

        // 检查事件集合中是否包含预期的事件
        assertEquals(2, receivedEventList.count { it == testEvent1 })
        assertEquals(2, receivedEventList.count { it == testEvent2 })
    }

    @Test
    fun `test multiple event subscriptions`(): Unit = runBlocking {
        val eventReceived1 = Channel<TestEvent>(capacity = Channel.UNLIMITED)
        val eventReceived2 = Channel<TestEvent>(capacity = Channel.UNLIMITED)

        eventBus.subscribe<TestEvent> { event ->
            eventReceived1.send(event)
        }

        eventBus.subscribe<TestEvent> { event ->
            eventReceived2.send(event)
        }

        val testEvent = TestEvent("TestUser")
        eventBus.post(testEvent)
        eventBus.post(testEvent)

        val receivedEvent1 = withTimeoutOrNull(1000) { eventReceived1.receive() }
        val receivedEvent2 = withTimeoutOrNull(1000) { eventReceived1.receive() }
        val receivedEvent3 = withTimeoutOrNull(1000) { eventReceived2.receive() }
        val receivedEvent4 = withTimeoutOrNull(1000) { eventReceived2.receive() }

        assertNotNull(receivedEvent1)
        assertNotNull(receivedEvent2)
        assertNotNull(receivedEvent3)
        assertNotNull(receivedEvent4)

        val receivedEvents = listOf(receivedEvent1, receivedEvent2, receivedEvent3, receivedEvent4)

        // 检查事件集合中是否包含预期的事件
        assertEquals(4, receivedEvents.count { it == testEvent })
    }

    @Test
    fun `test terminable subscriptions`(): Unit = runBlocking {
        var flag = true

        // 订阅
        val job = eventBus.subscribe<TestEvent> { _ ->
            flag = false
        }

        // 然后立马取消订阅,
        // 意味着 flag 依然为 true
        job.cancel()

        val testEvent = TestEvent("TestUser")
        // 发布事件, 但应该没有人监听
        eventBus.post(testEvent)

        // 检查 flag 是不是依然为 true
        assertTrue(flag)
    }
}
