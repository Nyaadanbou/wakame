package cc.mewcraft.wakame.entity.player

import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.require
import cc.mewcraft.wakame.util.yamlLoader
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.StyleBuilderApplicable
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import java.io.File

@Init(stage = InitStage.PRE_WORLD)
internal object AttackSpeedRegistryLoader : RegistryLoader {

    private val rootDirectory: File = KoishDataPaths.CONFIGS.resolve("attack_speed/").toFile()
    private val globalConfigFile: File = rootDirectory.resolve("config.yml")
    private val entryDataDirectory: File = rootDirectory.resolve("entries/")

    @InitFun
    fun init() {
        BuiltInRegistries.ATTACK_SPEED.resetRegistry()
        consumeData(BuiltInRegistries.ATTACK_SPEED::add)
        BuiltInRegistries.ATTACK_SPEED.freeze()
    }

    private fun consumeData(action: (Identifier, AttackSpeed) -> Unit) {
        val loader = yamlLoader {
            withDefaults()
        }

        entryDataDirectory.walk().drop(1).filter { it.isFile && it.extension == "yml" }.forEach { f ->
            try {
                val rootNode = loader.buildAndLoadString(f.readText())
                val entryId = Identifiers.of(f.toRelativeString(entryDataDirectory).substringBeforeLast('.'))
                val entryVal = parseEntry(entryId, rootNode)
                action(entryId, entryVal)
            } catch (e: Exception) {
                LOGGER.error("Failed to load attack speed from file: ${f.toRelativeString(rootDirectory)}", e)
            }
        }
    }

    private fun parseEntry(id: Identifier, node: ConfigurationNode): AttackSpeed {
        val name = node.node("name").get<Component>(Component.text(id.asString()))
        val styles = node.node("styles").get<Array<StyleBuilderApplicable>>(emptyArray())
        val cooldown = node.node("cooldown").require<Int>()

        return AttackSpeed(name, styles, cooldown)
    }

}