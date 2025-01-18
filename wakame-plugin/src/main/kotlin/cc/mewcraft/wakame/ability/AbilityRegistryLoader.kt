package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.trigger.SequenceTrigger
import cc.mewcraft.wakame.ability.trigger.SingleTrigger
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryConfigStorage
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.buildYamlConfigLoader
import cc.mewcraft.wakame.util.krequire

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeBundleFacadeRegistryConfigStorage::class, // deps: 需要直接的数据
    ]
)
@Reload
object AbilityRegistryConfigStorage : RegistryConfigStorage {
    /**
     * 存放铭刻的文件夹 (相对于插件文件夹).
     */
    const val DIR_PATH: String = "ability/"

    // TODO 有这么几类东西需要分别放入不同的注册表:
    // AbilityArchetype
    // Ability
    // Trigger
    // PlayerAbility (仅在配置文件中, 不会被直接放进注册表?

    // TODO 由于已经在新分支上动了部分底层代码, 等这个 PR 合并后再写技能的

    @InitFun
    fun init() {
        KoishRegistries.ABILITY.resetRegistry()
        // 初始化静态变量
        SingleTrigger.RIGHT_CLICK
        SequenceTrigger.RRR

        applyAbilityDataToRegistry(KoishRegistries.ABILITY::add)
        KoishRegistries.ABILITY.freeze()
        KoishRegistries.TRIGGER.freeze()
    }

    @ReloadFun
    fun reload() {
        applyAbilityDataToRegistry(KoishRegistries.ABILITY::update)
    }

    private fun applyAbilityDataToRegistry(registryAction: (Identifier, Ability) -> Unit) {
        AbilityArchetypes // 初始化技能原型

        val dataDirectory = getFileInConfigDirectory(DIR_PATH)
        val namespaceDirs = dataDirectory.walk().maxDepth(1)
            .drop(1) // exclude the `dataDirectory` itself
            .filter { it.isDirectory }
            .toList()

        val loaderBuilder = buildYamlConfigLoader { withDefaults() }

        // then walk each file, i.e., each ability
        for (namespaceDir in namespaceDirs) {
            namespaceDir.walk().maxDepth(1)
                .drop(1) // exclude the `namespaceDir` itself
                .filter { it.isFile }
                .forEach { file ->
                    val value = file.nameWithoutExtension

                    val text = file.readText()
                    val node = loaderBuilder.buildAndLoadString(text)

                    val abilityId = Identifiers.of(value)
                    val archetype = node.node("type").krequire<AbilityArchetype>()
                    val ability = try {
                        archetype.create(abilityId, node)
                    } catch (t: Throwable) {
                        LOGGER.warn("Failed to load ability: '$abilityId', Path: '${file.path}'", t)
                        return@forEach
                    }

                    registryAction(abilityId, ability)
                }
        }
    }
}