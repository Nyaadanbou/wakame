package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.ability.archetype.AbilityArchetype
import cc.mewcraft.wakame.ability.archetype.AbilityArchetypes
import cc.mewcraft.wakame.ability.trigger.TriggerRegistryLoader
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.entity.attribute.AttributeBundleFacadeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryConfigStorage
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import cc.mewcraft.wakame.util.NamespacedFileTreeWalker
import cc.mewcraft.wakame.util.require

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeBundleFacadeRegistryLoader::class, // deps: 需要直接的数据
        TriggerRegistryLoader::class,
    ]
)
@Reload
internal object AbilityRegistryLoader : RegistryConfigStorage {
    /**
     * 存放铭刻的文件夹 (相对于插件文件夹).
     */
    const val DIR_PATH: String = "ability/"

    @InitFun
    private fun init() {
        KoishRegistries.ABILITY.resetRegistry()

        applyAbilityDataToRegistry(KoishRegistries.ABILITY::add)
        KoishRegistries.ABILITY.freeze()
    }

    @ReloadFun
    private fun reload() {
        applyAbilityDataToRegistry(KoishRegistries.ABILITY::update)
    }

    private fun applyAbilityDataToRegistry(registryAction: (Identifier, Ability) -> Unit) {
        AbilityArchetypes // 初始化技能原型
        val loader = Configs.createBuilder("ability")

        for ((file, _, path) in NamespacedFileTreeWalker(getFileInConfigDirectory(DIR_PATH), fileExtension = "yml", includeFullPath = true, includeNamespacePath = true)) {
            val rootNode = loader.buildAndLoadString(file.readText())

            val abilityId = Identifiers.of(path)
            val archetype = rootNode.node("type").require<AbilityArchetype>()
            try {
                val ability = archetype.create(abilityId, rootNode)
                registryAction(abilityId, ability)
            } catch (t: Throwable) {
                LOGGER.warn("Failed to load ability: '$abilityId', Path: '${file.path}'", t)
                continue
            }

            LOGGER.info("Loaded ability: '$abilityId'")
        }
    }
}