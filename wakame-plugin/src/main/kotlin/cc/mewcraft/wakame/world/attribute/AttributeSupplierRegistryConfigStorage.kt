package cc.mewcraft.wakame.world.attribute

import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.AttributeSupplierDeserializer
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
    runAfter = [AttributeRegistry::class]
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
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText())
        val dataMap = AttributeSupplierDeserializer(rootNode.node("entity_attributes")).deserialize()
        dataMap.forEach { (k, v) ->
            registryAction.invoke(k, v)
        }
    }
}