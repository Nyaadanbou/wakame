import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import com.github.quillraven.fleks.configureWorld
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals

class Test {
    @Test
    fun add() {
        val world = configureWorld {
            systems {
                add(TestSystem())
            }
        }

        val time = measureTimeMillis {
            for (i in 1..100) {
                world.update(i.toFloat())
            }
        }

        assertEquals(100f, world.deltaTime)

        println("Done in $time ms")
    }
}

class TestSystem : IntervalSystem(
    interval = Fixed(100f)
) {
    override fun onTick() {
        println("Hello, world")
    }
}