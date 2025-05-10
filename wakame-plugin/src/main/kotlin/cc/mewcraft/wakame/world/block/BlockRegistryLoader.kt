package cc.mewcraft.wakame.world.block

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.yamlLoader
import cc.mewcraft.wakame.world.KoishBlock
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.File

@Init(stage = InitStage.PRE_WORLD)
internal object BlockRegistryLoader : RegistryLoader {

    private val rootDirectory: File = KoishDataPaths.CONFIGS.resolve("block/").toFile()
    private val globalConfigFile: File = rootDirectory.resolve("config.yml")
    private val entryDataDirectory: File = rootDirectory.resolve("entries/")

    @InitFun
    fun init() {
        consumeData(BuiltInRegistries.BLOCK::add)
        BuiltInRegistries.BLOCK.freeze()
    }

    private fun consumeData(action: (Identifier, KoishBlock) -> Unit) {
        val loader = yamlLoader {
            withDefaults()
        }

        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val entryId = Identifiers.of(f.relativeTo(entryDataDirectory).invariantSeparatorsPath.substringBeforeLast('.'))
                val entryVal = parseEntry(entryId, rootNode)
                action(entryId, entryVal)
            } catch (e: Exception) {
                LOGGER.error("Failed to load attack speed from file: ${f.toRelativeString(rootDirectory)}", e)
            }
        }
    }

    private fun parseEntry(id: Identifier, node: ConfigurationNode): KoishBlock {
        val test = node.node("test").get<String>("Empty!")
        return KoishBlock(test)
    }

}