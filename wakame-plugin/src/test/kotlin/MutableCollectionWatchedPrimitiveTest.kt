import cc.mewcraft.wakame.random.WatchedCollection
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MutableCollectionWatchedPrimitiveTest {
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

    private val numbers: MutableCollection<Int> by WatchedCollection(HashSet(8))

    @Test
    fun add() {
        numbers.add(1)
        numbers.add(2)
        numbers.add(2)
        numbers.add(3)

        assertEquals(3, numbers.size)
        assertEquals(setOf(1, 2, 3), numbers)
    }
}