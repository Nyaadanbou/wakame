package eventbus

import cc.mewcraft.wakame.eventbus.EventBus
import cc.mewcraft.wakame.eventbus.Subscribers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

data class UserRegisteredEvent(val username: String)

class EventBusTest {

    private lateinit var eventBus: EventBus
    private lateinit var testScope: CoroutineScope

    @BeforeTest
    fun setUp() {
        testScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        eventBus = EventBus(testScope)
    }

    @AfterTest
    fun tearDown() {
        eventBus.close()
        testScope.cancel()
    }

    @Test
    fun `test event bus`(): Unit = runBlocking {
        val eventReceived = Channel<UserRegisteredEvent>()

        Subscribers.register<UserRegisteredEvent>(eventBus) { event ->
            eventReceived.send(event)
        }

        val testEvent = UserRegisteredEvent("TestUser")
        eventBus.post(testEvent)

        val receivedEvent = withTimeoutOrNull(1000) {
            eventReceived.receive()
        }
        assertNotNull(receivedEvent)
        assertEquals(testEvent, receivedEvent)
    }

    @Test
    fun `test event handler exception`(): Unit = runBlocking {
        val firstEventReceived = Channel<UserRegisteredEvent>()
        val secondEventReceived = Channel<UserRegisteredEvent>()

        Subscribers.register<UserRegisteredEvent>(eventBus) { event ->
            firstEventReceived.send(event)
            throw RuntimeException("Handler exception")
        }

        Subscribers.register<UserRegisteredEvent>(eventBus) { event ->
            secondEventReceived.send(event)
        }

        val testEvent1 = UserRegisteredEvent("User1")
        val testEvent2 = UserRegisteredEvent("User2")
        eventBus.post(testEvent1)
        eventBus.post(testEvent2)

        val receivedFirstEvent = withTimeoutOrNull(1000) { firstEventReceived.receive() }
        assertNotNull(receivedFirstEvent)
        assertEquals(testEvent1, receivedFirstEvent)

        val receivedSecondEvent = withTimeoutOrNull(1000) { secondEventReceived.receive() }
        assertNotNull(receivedSecondEvent)
        assertEquals(testEvent2, receivedSecondEvent)
    }

    @Test
    fun `test multiple event subscriptions`(): Unit = runBlocking {
        val eventsReceived = mutableListOf<UserRegisteredEvent>()
        val eventReceived1 = Channel<UserRegisteredEvent>()
        val eventReceived2 = Channel<UserRegisteredEvent>()

        Subscribers.register<UserRegisteredEvent>(eventBus) { event ->
            eventsReceived.add(event)
            eventReceived1.send(event)
        }

        Subscribers.register<UserRegisteredEvent>(eventBus) { event ->
            eventsReceived.add(event)
            eventReceived2.send(event)
        }

        val testEvent = UserRegisteredEvent("TestUser")
        eventBus.post(testEvent)
        eventBus.post(testEvent)

        val receivedEvent1 = withTimeoutOrNull(1000) { eventReceived1.receive() }
        assertNotNull(receivedEvent1)
        assertEquals(testEvent, receivedEvent1)

        val receivedEvent2 = withTimeoutOrNull(1000) { eventReceived2.receive() }
        assertNotNull(receivedEvent2)
        assertEquals(testEvent, receivedEvent2)

        assertEquals(2, eventsReceived.size)
    }

    @Test
    fun `test terminable subscriptions`(): Unit = runBlocking {
        var cancelled = true

        val terminable = Subscribers.register<UserRegisteredEvent>(eventBus) { _ ->
            cancelled = false
        }

        // 取消订阅
        terminable.close()

        val testEvent = UserRegisteredEvent("TestUser")
        eventBus.post(testEvent)

        assertTrue(cancelled)
    }
}
