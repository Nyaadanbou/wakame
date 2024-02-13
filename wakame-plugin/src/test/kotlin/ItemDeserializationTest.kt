import cc.mewcraft.wakame.MINIMESSAGE_FULL
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.attribute.facade.AttributeFacadeRegistry
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reference.referenceModule
import cc.mewcraft.wakame.registry.*
import cc.mewcraft.wakame.skin.skinModule
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertNotNull

class ItemDeserializationTest : KoinTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            startKoin {
                modules(
                    module {
                        single<File>(named(PLUGIN_DATA_DIR)) {
                            Path.of("src/main/resources").toFile().absoluteFile.also {
                                println(it)
                            }
                        }

                        single<Logger> {
                            LoggerFactory.getLogger("Deserialization")
                        }

                        single<MiniMessage>(named(MINIMESSAGE_FULL)) { MiniMessage.miniMessage() }
                        single<GsonComponentSerializer> { GsonComponentSerializer.gson() }
                    },
                )
                modules(
                    elementModule(),
                    itemModule(),
                    kizamiModule(),
                    referenceModule(),
                    registryModule(),
                    rarityModule(),
                    skinModule()
                )
            }

            // initialize attribute facades
            AttributeFacadeRegistry.onPreWorld()

            // initialize necessary registry
            ElementRegistry.onPreWorld()
            KizamiRegistry.onPreWorld()
            RarityRegistry.onPreWorld()
            RarityMappingRegistry.onPreWorld()
            NekoItemRegistry.onPreWorld()
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            stopKoin()
        }
    }

    @Test
    fun `item deserialization test`() {
        val demo = NekoItemRegistry.get("short_sword:demo")
        assertNotNull(demo)
    }

    // we can't do the test as it requires server runtime environment
    fun `item generation test`() {
        val demo = NekoItemRegistry.get("short_sword:demo")
        assertNotNull(demo)
        demo.createItemStack(null)
    }
}