package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.factory.AbilityFactories
import cc.mewcraft.wakame.ability.trigger.SequenceTrigger
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
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

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [AttributeRegistry::class, ElementRegistry::class]
)
@Reload(
    runAfter = [AttributeRegistry::class, ElementRegistry::class]
)
//@ReloadDependency(runBefore = [AttributeRegistry::class, ElementRegistry::class])
object AbilityRegistry : KoinComponent {
    /* Trigger Constants */

    /**
     * The key of the empty ability.
     */
    val EMPTY_KEY: Key = Key(Namespaces.ABILITY, "empty")

    /**
     * 技能类型. 包含了技能的唯一标识, 条件, 描述信息等.
     */
    val INSTANCES: Registry<Key, Ability> = SimpleRegistry()

    /**
     * 技能触发器.
     */
    val TRIGGERS: Registry<Key, Trigger> = SimpleRegistry()

    val PATHS: Set<String>
        get() = INSTANCES.mapTo(ObjectArraySet(1)) { it.key.value() }

    private val LOGGER: Logger by inject()

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

        val dataDirectory = get<File>(named(PLUGIN_DATA_DIR)).resolve(ABILITY_PROTO_CONFIG_DIR)
        val namespaceDirs = dataDirectory.walk().maxDepth(1)
            .drop(1) // exclude the `dataDirectory` itself
            .filter { it.isDirectory }
            .toList()

        val loaderBuilder = get<YamlConfigurationLoader.Builder>(named(ABILITY_PROTO_CONFIG_LOADER))

        // then walk each file, i.e., each ability
        for (namespaceDir in namespaceDirs) {
            namespaceDir.walk().maxDepth(1)
                .drop(1) // exclude the `namespaceDir` itself
                .filter { it.isFile }
                .forEach { file ->
                    val namespace = namespaceDir.name
                    val value = file.nameWithoutExtension

                    val text = file.readText()
                    val node = loaderBuilder.buildAndLoadString(text)

                    val abilityId = Key(Namespaces.ABILITY, "${namespace}/$value")
                    val type = node.node("type").krequire<String>()
                    val ability = try {
                        requireNotNull(AbilityFactories[type]).create(abilityId, node)
                    } catch (t: Throwable) {
                        LOGGER.warn("Failed to load ability: '$abilityId', Path: '${file.path}'", t)
                        return@forEach
                    }

                    INSTANCES.register(abilityId, ability)
                }
        }
    }

    @InitFun
    fun onPreWorld() {
        AbilityFactories.load()
        loadConfiguration()
        loadTriggers()
    }

    @ReloadFun
    private fun onReload() {
        loadConfiguration()
    }
}
