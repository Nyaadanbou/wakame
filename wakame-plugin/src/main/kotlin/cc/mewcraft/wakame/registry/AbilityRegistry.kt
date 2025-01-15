package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.KoishDataPaths
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.trigger.SequenceTrigger
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.ability.trigger.Trigger
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.util.require
import it.unimi.dsi.fastutil.objects.ObjectArraySet
import net.kyori.adventure.key.Key
import org.koin.core.qualifier.named
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeBundleFacadeRegistryLoader::class, // deps: 需要直接的数据
    ]
)
@Reload
object AbilityRegistry {
    /**
     * 技能类型. 包含了技能的唯一标识, 条件, 描述信息等.
     */
    val INSTANCES: Registry<Key, Ability> = SimpleRegistry()

    /**
     * 技能触发器.
     */
    val TRIGGERS: Registry<Key, Trigger> = SimpleRegistry()

    /**
     * 所有技能的 ID.
     */
    val PATHS: Set<String>
        get() = INSTANCES.mapTo(ObjectArraySet(1)) { it.key.value() }

    @InitFun
    fun init() {
        AbilityArchetypes.load()
        loadConfiguration()
        loadTriggers()
    }

    @ReloadFun
    fun reload() {
        loadConfiguration()
    }

    private fun loadTriggers() {
        val GENERIC_TRIGGERS: List<SingleTrigger> = listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK, SingleTrigger.NOOP)
        val SEQUENCE_GENERATION_TRIGGERS: List<SingleTrigger> = listOf(SingleTrigger.LEFT_CLICK, SingleTrigger.RIGHT_CLICK)

        // Register Static Triggers
        GENERIC_TRIGGERS.forEach { TRIGGERS.register(it.key, it) }
        // Register Combo Triggers
        val combos = SequenceTrigger.generate(SEQUENCE_GENERATION_TRIGGERS, 3)
        combos.forEach { TRIGGERS.register(it.key, it) }
    }

    private fun loadConfiguration() {
        INSTANCES.clear()

        val dataDirectory = KoishDataPaths.CONFIGS.resolve(ABILITY_PROTO_CONFIG_DIR).toFile()
        val namespaceDirs = dataDirectory.walk().maxDepth(1)
            .drop(1) // exclude the `dataDirectory` itself
            .filter { it.isDirectory }
            .toList()

        val loaderBuilder = Injector.get<YamlConfigurationLoader.Builder>(named(ABILITY_PROTO_CONFIG_LOADER))

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

                    val abilityId = Key.key(Namespaces.ABILITY, "${namespace}/$value")
                    val type = node.node("type").require<String>()
                    val ability = try {
                        requireNotNull(AbilityArchetypes[type]).create(abilityId, node)
                    } catch (t: Throwable) {
                        LOGGER.warn("Failed to load ability: '$abilityId', Path: '${file.path}'", t)
                        return@forEach
                    }

                    INSTANCES.register(abilityId, ability)
                }
        }
    }
}
