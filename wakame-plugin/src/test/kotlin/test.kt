import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.config.node
import cc.mewcraft.wakame.config.optionalEntry
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.orElse
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

private val CONFIG: Provider<CommentedConfigurationNode> = Configs["animation"]

class Test {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    testEnv(),
                )
            }
        }

        @JvmStatic
        @AfterAll
        fun shutdown() {
            stopKoin()
        }
    }

    @Test
    fun case1() {
        val animation = Animation(CONFIG.node("animation"))

        assertEquals(0f, animation.delay)

        assertEquals(0f, animation.noneCriticalStrike.startInterpolation)
        assertEquals(0f, animation.noneCriticalStrike.interpolationDuration)
        assertContentEquals(listOf(0f, 0f, 0f), animation.noneCriticalStrike.translation)
        assertContentEquals(listOf(1f, 1f, 1f), animation.noneCriticalStrike.scale)

        assertEquals(1f, animation.positiveCriticalStrike.startInterpolation)
        assertEquals(2f, animation.positiveCriticalStrike.interpolationDuration)
        assertContentEquals(listOf(0f, 0f, 0f), animation.positiveCriticalStrike.translation)
        assertContentEquals(listOf(1f, 1f, 1f), animation.positiveCriticalStrike.scale)

        assertEquals(0f, animation.negativeCriticalStrike.startInterpolation)
        assertEquals(0f, animation.negativeCriticalStrike.interpolationDuration)
        assertContentEquals(listOf(0f, 0f, 0f), animation.negativeCriticalStrike.translation)
        assertContentEquals(listOf(1f, 1f, 1f), animation.negativeCriticalStrike.scale)
    }
}

class Animation(source: Provider<ConfigurationNode>) {

    val delay: Float by source.entry<Float>("delay")
    val noneCriticalStrike: Transform = Transform(source.node("none_critical_strike"))
    val positiveCriticalStrike: Transform = Transform(source.node("positive_critical_strike"), source.node("none_critical_strike"))
    val negativeCriticalStrike: Transform = Transform(source.node("negative_critical_strike"), source.node("none_critical_strike"))

    class Transform(
        source: Provider<ConfigurationNode>,
        fallback: Provider<ConfigurationNode> = source,
    ) {
        val startInterpolation: Float by entry0(source, fallback, "start_interpolation")
        val interpolationDuration: Float by entry0(source, fallback, "interpolation_duration")
        val translation: List<Float> by entry0(source, fallback, "translation")
        val scale: List<Float> by entry0(source, fallback, "scale")

        private inline fun <reified T : Any> entry0(source: Provider<ConfigurationNode>, fallback: Provider<ConfigurationNode> = source, vararg path: String): Provider<T> =
            source.optionalEntry<T>(*path).orElse(fallback.entry<T>(*path))
    }

}
