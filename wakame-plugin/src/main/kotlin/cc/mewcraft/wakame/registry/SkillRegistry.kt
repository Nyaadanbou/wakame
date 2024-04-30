package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skill.ConfiguredSkill
import cc.mewcraft.wakame.skill.SkillTrigger
import cc.mewcraft.wakame.skill.SkillTrigger.*
import cc.mewcraft.wakame.skill.condition.DurabilityCondition
import cc.mewcraft.wakame.skill.condition.MoLangCondition
import cc.mewcraft.wakame.skill.condition.SkillConditionFactory
import cc.mewcraft.wakame.skill.type.*
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.SkillTriggerUtil.generateCombinations
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

    /* Trigger Constants */
    val GENERIC_TRIGGERS: List<SkillTrigger> = listOf(LeftClick, RightClick, Attack, Jump)
    val COMBO_TRIGGERS: List<SkillTrigger> = listOf(LeftClick, RightClick)

    /**
     * The key of the empty skill.
     */
    val EMPTY_KEY: Key = Key(Namespaces.SKILL, "empty")

    val INSTANCE: Registry<Key, ConfiguredSkill> = SimpleRegistry()
    val CONDITIONS: Registry<String, SkillConditionFactory<*>> = SimpleRegistry()
    val SKILL_TYPES: Registry<String, SkillFactory<*>> = SimpleRegistry()
    val TRIGGER_INSTANCES: Registry<Key, SkillTrigger> = SimpleRegistry()

    private fun loadCondition() {
        operator fun Pair<String, SkillConditionFactory<*>>.unaryPlus() = CONDITIONS.register(first, second)

        +("durability" to DurabilityCondition)
        +("molang" to MoLangCondition)
    }

    private fun loadType() {
        operator fun Pair<String, SkillFactory<*>>.unaryPlus() = SKILL_TYPES.register(first, second)

        +("command_execute" to CommandExecute)
        +("kill_entity" to KillEntity)
        +("remove_potion_effect" to RemovePotionEffect)
        +("teleport" to Teleport)
    }

    private fun loadTriggers() {
        // Register Static Triggers
        GENERIC_TRIGGERS.forEach { TRIGGER_INSTANCES.register(it.key, it) }
        // Register Combo Triggers
        val combos = COMBO_TRIGGERS.generateCombinations(3)
        combos.forEach { TRIGGER_INSTANCES.register(it.key, it) }
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
                    val skill = ConfiguredSkill(node, skillFile.path)

                    INSTANCE.register(skillKey, skill)
                    logger.info("Loaded configured skill: {}", skillKey)
                }
        }
    }

    override fun onPreWorld() {
        loadType()
        loadTriggers()
        loadCondition()
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}
