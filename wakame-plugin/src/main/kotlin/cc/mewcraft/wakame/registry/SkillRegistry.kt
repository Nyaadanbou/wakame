package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.NodeConfigProvider
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.SkillFactories
import cc.mewcraft.wakame.skill.condition.ManaCondition
import cc.mewcraft.wakame.skill.condition.MoLangExpression
import cc.mewcraft.wakame.skill.condition.NekoDurability
import cc.mewcraft.wakame.skill.condition.SkillConditionFactory
import cc.mewcraft.wakame.skill.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.skill.trigger.Trigger
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

object SkillRegistry : Initializable, KoinComponent {
    /* Trigger Constants */

    /**
     * The key of the empty skill.
     */
    val EMPTY_KEY: Key = Key(Namespaces.SKILL, "empty")

    /**
     * 技能类型. 包含了技能的唯一标识, 条件, 描述信息等.
     */
    val TYPES: Registry<Key, Skill> = SimpleRegistry()

    /**
     * 技能条件.
     */
    val CONDITIONS: Registry<String, SkillConditionFactory<*>> = SimpleRegistry()

    /**
     * 技能触发器.
     */
    val TRIGGERS: Registry<Key, Trigger> = SimpleRegistry()

    private val LOGGER: Logger by inject()

    private fun loadSkillConditions() {
        CONDITIONS.register("durability", NekoDurability)
        CONDITIONS.register("molang", MoLangExpression)
        CONDITIONS.register("mana", ManaCondition)
    }

    private fun loadTriggers() {
        val GENERIC_TRIGGERS: List<SingleTrigger> = listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.ATTACK, SingleTrigger.JUMP)
        val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> = listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)

        // Register Static Triggers
        GENERIC_TRIGGERS.forEach { TRIGGERS.register(it.key, it) }
        // Register Combo Triggers
        val combos = SequenceTrigger.generate(SEQUENCE_GENERATION_TRIGGERS, 3)
        combos.forEach { TRIGGERS.register(it.key, it) }
    }

    private fun loadConfiguration() {
        TYPES.clear()

        val dataDirectory = get<File>(named(PLUGIN_DATA_DIR)).resolve(SKILL_PROTO_CONFIG_DIR)
        val namespaceDirs = mutableListOf<File>()

        // first walk each directory, i.e., each namespace
        dataDirectory.walk().maxDepth(1)
            .drop(1) // exclude the `dataDirectory` itself
            .filter { it.isDirectory }
            .forEach {
                namespaceDirs += it.also {
                    LOGGER.info("Loading skill namespace: {}", it.name)
                }
            }

        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(SKILL_PROTO_CONFIG_LOADER))

        // then walk each file, i.e., each skill
        for (namespaceDir in namespaceDirs) {
            namespaceDir.walk().maxDepth(1)
                .drop(1) // exclude the `namespaceDir` itself
                .filter { it.isFile }
                .forEach { skillFile ->
                    val namespace = namespaceDir.name
                    val value = skillFile.nameWithoutExtension
                    val skillKey = Key(Namespaces.SKILL, "${namespace}/$value")

                    val text = skillFile.bufferedReader().use { it.readText() }
                    val node = loaderBuilder.buildAndLoadString(text)

                    val type = node.node("type").krequire<String>()
                    val provider = NodeConfigProvider(node, skillFile.path)
                    val skill = SkillFactories[type]?.create(skillKey, provider)
                    if (skill == null) {
                        LOGGER.error("Failed to load skill: {}", skillKey)
                        return@forEach
                    }

                    TYPES.register(skillKey, skill)
                    LOGGER.info("Loaded configured skill: {}", skillKey)
                }
        }
    }

    override fun onPreWorld() {
        SkillFactories.load()
        loadSkillConditions()
        loadConfiguration()
        loadTriggers()
    }

    override fun onReload() {
        loadConfiguration()
    }
}
