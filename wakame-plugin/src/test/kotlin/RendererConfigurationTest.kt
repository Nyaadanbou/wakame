import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display.RendererConfiguration
import cc.mewcraft.wakame.display.displayModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.skinModule
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

class RendererConfigurationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    module {
                        single<File>(named(PLUGIN_DATA_DIR)) { Path.of("src/main/resources").toFile().absoluteFile }
                        single<Logger> { LoggerFactory.getLogger("RendererConfigurationTest") }
                        single<MiniMessage>(named(MINIMESSAGE_FULL)) { MiniMessage.miniMessage() }
                        single<GsonComponentSerializer> { GsonComponentSerializer.gson() }
                    },
                )

                // this module
                modules(displayModule())

                // dependencies
                modules(
                    elementModule(),
                    kizamiModule(),
                    registryModule(),
                    rarityModule(),
                    skinModule()
                )
            }

            // initialize attribute facades
            AttributeRegistry.onPreWorld()

            // initialize necessary registry
            ElementRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopKoin()
        }
    }

    @Test
    fun `renderer config loader test`() {
        val logger = get<Logger>()
        val loader = get<RendererConfiguration>().also { it.onReload() }

        // some properties are lazy, we need to call them to trigger initialization
        logger.debug("Calling all getters through reflection")
        loader::class.declaredMemberProperties
            .onEach { it.isAccessible = true }
            .filter { it.visibility == KVisibility.PUBLIC }
            .forEach { it.getter.call(loader) }
        logger.debug("Done!")
    }
}