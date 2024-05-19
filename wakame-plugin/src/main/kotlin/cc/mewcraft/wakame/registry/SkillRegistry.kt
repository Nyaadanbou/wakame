package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.condition.DurabilityCondition
import cc.mewcraft.wakame.skill.condition.MoLangCondition
import cc.mewcraft.wakame.skill.condition.SkillConditionFactory
import cc.mewcraft.wakame.skill.type.RemovePotionEffect
import cc.mewcraft.wakame.skill.type.SkillType
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

object SkillRegistry : Initializable, KoinComponent {
    private val logger: Logger by inject(mode = LazyThreadSafetyMode.NONE)

    /**
     * The key of the empty skill.
     */
    val EMPTY_KEY: Key = Key(Namespaces.SKILL, "empty")

    val INSTANCE: Registry<Key, ConfiguredSkill> = SimpleRegistry() // TODO rename it to INSTANCES
    val SKILL_TYPES: Registry<String, SkillType<*>> = SimpleRegistry() // TODO rename it to TYPES
    val CONDITIONS: Registry<String, SkillConditionFactory<*>> = SimpleRegistry()

    private fun loadCondition(){
        CONDITIONS += "durability" to DurabilityCondition
        CONDITIONS += "molang" to MoLangCondition
    }

    private fun loadType() {
        SKILL_TYPES += "remove_potion_effect" to RemovePotionEffect
    }

    private fun loadConfiguration() {
        INSTANCE.clear()

        val dataDirectory = get<File>(named(PLUGIN_DATA_DIR)).resolve(SKILL_PROTO_CONFIG_DIR)
        val namespaceDirs = mutableListOf<File>()

        // first walk each directory, i.e., each namespace
        dataDirectory.walk().maxDepth(1)
            .drop(1) // exclude the `dataDirectory` itself
            .filter { it.isDirectory }
            .forEach {
                namespaceDirs += it.also {
                    logger.info("Loading skill namespace: {}", it.name)
                }
            }

        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(SKILL_PROTO_CONFIG_LOADER))

        // then walk each file, i.e., each skill
        namespaceDirs.forEach { namespaceDir ->
            namespaceDir.walk().maxDepth(1)
                .drop(1) // exclude the `namespaceDir` itself
                .filter { it.isFile }
                .forEach { skillFile ->
                    val namespace = namespaceDir.name
                    val value = skillFile.nameWithoutExtension
                    val skillKey = Key(Namespaces.SKILL, "${namespace}/$value")

                    val text = skillFile.bufferedReader().use { it.readText() }
                    val node = loaderBuilder.buildAndLoadString(text)
                    val skill = ConfiguredSkill(node, skillKey, skillFile.path)

                    INSTANCE.register(skillKey, skill)
                    logger.info("Loaded skill: {}", skillKey)
                }
        }
    }

    override fun onPreWorld() {
        loadType()
        loadCondition()
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}
