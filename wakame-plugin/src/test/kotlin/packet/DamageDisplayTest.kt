package packet

import cc.mewcraft.wakame.network.RadialPointCycle
import io.mockk.mockk
import org.bukkit.entity.Entity
import kotlin.test.Test
import kotlin.test.assertEquals

class DamageDisplayTest {
    @Test
    fun `radial point cycle with 4 slices`() {
        val expected = listOf(
            Pair(-1f, 0f),
            Pair(1f, 0f),
            Pair(0f, -1f),
            Pair(0f, 1f)
        )
        testRadialCycle(4, 1f, expected)
    }

    @Test
    fun `radial point cycle with 8 slices`() {
        val expected = listOf(
            Pair(-1f, 0f),
            Pair(1f, 0f),
            Pair(-0.7071f, -0.7071f),
            Pair(0.7071f, 0.7071f),
            Pair(0f, -1f),
            Pair(0f, 1f),
            Pair(0.7071f, -0.7071f),
            Pair(-0.7071f, 0.7071f)
        )
        testRadialCycle(8, 1f, expected)
    }

    private fun testRadialCycle(
        slices: Int,
        radius: Float,
        expected: List<Pair<Float, Float>>,
    ) {
        val entity = mockk<Entity>(relaxed = true)
        val cycle = RadialPointCycle(slices, radius)
        val actual = (0 until slices).map { cycle.next(entity) }

        assertEquals(expected.size, actual.size)

        for (i in expected.indices) {
            val (ex, ey) = expected[i]
            val (ax, ay) = actual[i]
            assertEquals(ex, ax, 1e-4f)
            assertEquals(ey, ay, 1e-4f)
        }
    }
}