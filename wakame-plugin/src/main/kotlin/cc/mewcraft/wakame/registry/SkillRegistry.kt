package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.skill2.Skill
import cc.mewcraft.wakame.skill2.condition.ManaCondition
import cc.mewcraft.wakame.skill2.condition.MoLangExpression
import cc.mewcraft.wakame.skill2.condition.NekoDurability
import cc.mewcraft.wakame.skill2.condition.SkillConditionFactory
import cc.mewcraft.wakame.skill2.factory.SkillFactories
import cc.mewcraft.wakame.skill2.trigger.SequenceTrigger
import cc.mewcraft.wakame.skill2.trigger.SingleTrigger
import cc.mewcraft.wakame.skill2.trigger.Trigger
import cc.mewcraft.wakame.util.Key
import cc.mewcraft.wakame.util.krequire
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

@PreWorldDependency(runBefore = [AttributeRegistry::class, ElementRegistry::class])
@ReloadDependency(runBefore = [AttributeRegistry::class, ElementRegistry::class])
object SkillRegistry : Initializable, KoinComponent {
    /* Trigger Constants */

    /**
     * The key of the empty skill.
     */
    val EMPTY_KEY: Key = Key(Namespaces.SKILL, "empty")

    /**
     * 技能类型. 包含了技能的唯一标识, 条件, 描述信息等.
     */
    val INSTANCES: Registry<Key, Skill> = SimpleRegistry()

    /**
     * 技能条件.
     */
    val CONDITIONS: Registry<String, SkillConditionFactory<*>> = SimpleRegistry()

    /**
     * 技能触发器.
     */
    val TRIGGERS: Registry<Key, Trigger> = SimpleRegistry()

    val PATHS: Set<String>
        get() = INSTANCES.mapTo(ObjectArraySet(1)) { it.key.value() }

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
        INSTANCES.clear()

        val dataDirectory = get<File>(named(PLUGIN_DATA_DIR)).resolve(SKILL_PROTO_CONFIG_DIR)
        val namespaceDirs = dataDirectory.walk().maxDepth(1)
            .drop(1) // exclude the `dataDirectory` itself
            .filter { it.isDirectory }
            .toList()

        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(SKILL_PROTO_CONFIG_LOADER))

        // then walk each file, i.e., each skill
        for (namespaceDir in namespaceDirs) {
            namespaceDir.walk().maxDepth(1)
                .drop(1) // exclude the `namespaceDir` itself
                .filter { it.isFile }
                .forEach { file ->
                    val namespace = namespaceDir.name
                    val value = file.nameWithoutExtension

                    val text = file.readText()
                    val node = loaderBuilder.buildAndLoadString(text)

                    val skillId = Key(Namespaces.SKILL, "${namespace}/$value")
                    val type = node.node("type").krequire<String>()
                    val skill = try {
                        requireNotNull(SkillFactories[type]).create(skillId, node)
                    } catch (t: Throwable) {
                        LOGGER.warn("Failed to load skill: '$skillId', Path: '${file.path}'", t)
                        return@forEach
                    }

                    INSTANCES.register(skillId, skill)
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
