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

class RendererConfigurationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    module {
                        single<File>(named(PLUGIN_DATA_DIR)) { Path.of("src/main/resources").toFile().absoluteFile.also { println(it) } }
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
        val loader = get<RendererConfiguration>().also { it.onReload() }

        val allLoreIndexes = loader.loreMetaLookup
        val loreLineIndexes = loader.fullIndexLookup
        val fixedLoreLines = loader.fixedLoreLines

        val nameFormat = loader.nameFormat
        val loreFormat = loader.loreFormat
        val levelFormat = loader.levelFormat
        val rarityFormat = loader.rarityFormat
        val elementFormat = loader.elementFormat
        val kizamiFormat = loader.kizamiFormat
        val skinFormat = loader.skinFormat
        val skinOwnerFormat = loader.skinOwnerFormat
        val operationFormat = loader.operationFormat
        val attributeFormats = loader.attributeFormats
        val attackSpeedFormat = loader.attackSpeedFormat

        println(allLoreIndexes)
        println(loreLineIndexes)
        println(fixedLoreLines)

        println("=====================================")

        println(nameFormat)
        println(loreFormat)
        println(levelFormat)
        println(rarityFormat)
        println(elementFormat)
        println(kizamiFormat)
        println(skinFormat)
        println(skinOwnerFormat)
        println(operationFormat)
        println(attributeFormats)
        println(attackSpeedFormat)
    }
}