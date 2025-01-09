package cc.mewcraft.wakame.world.attribute

import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.AttributeSupplierSerializer
import cc.mewcraft.wakame.core.RegistryConfigStorage
import cc.mewcraft.wakame.core.ResourceLocation
import cc.mewcraft.wakame.core.registries.KoishRegistries
import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.buildYamlConfigLoader

// TODO 把 Attribute 迁移到该 package 下

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        AttributeRegistry::class // 实际上也依赖 Element, 但因为 Attribute 已经依赖 Element 所以这里不需要显式声明
    ]
)
@Reload
object AttributeSupplierRegistryConfigStorage : RegistryConfigStorage {
    const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        KoishRegistries.ATTRIBUTE_SUPPLIER.resetRegistry()
        applyDataToRegistry(KoishRegistries.ATTRIBUTE_SUPPLIER::register)
        KoishRegistries.ATTRIBUTE_SUPPLIER.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.ATTRIBUTE_SUPPLIER::update)
    }

    private fun applyDataToRegistry(registryAction: (ResourceLocation, AttributeSupplier) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText()).node("entity_attributes")
        val dataMap = AttributeSupplierSerializer.deserialize(rootNode)
        dataMap.forEach { (k, v) ->
            registryAction.invoke(k, v)
        }
    }
}