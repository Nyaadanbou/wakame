import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.display.RendererConfiguration
import cc.mewcraft.wakame.display.displayModule
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path

class RendererConfigurationTest : KoinTest {
    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            startKoin {
                modules(
                    module {
                        single<File>(named(PLUGIN_DATA_DIR)) {
                            Path.of("src/main/resources").toFile().absoluteFile.also {
                                println(it)
                            }
                        }

                        single<Logger> {
                            LoggerFactory.getLogger("RendererConfigurationTest")
                        }
                    },
                    displayModule()
                )
            }
        }
    }

    @Test
    fun `renderer config loader test`() {
        val loader: RendererConfiguration by inject()
        loader.onReload()

        val loreLineIndexes = loader.loreLineIndexes
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

        println(loreLineIndexes)
        println(fixedLoreLines)

        println(nameFormat)
        println(loreFormat)
        println(levelFormat)
        println(rarityFormat)
        println(elementFormat)
        println(kizamiFormat)
        println(skinFormat)
        println(skinOwnerFormat)
        println(operationFormat)
    }
}