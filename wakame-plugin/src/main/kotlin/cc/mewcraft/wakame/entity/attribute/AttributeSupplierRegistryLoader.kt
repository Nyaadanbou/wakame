package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.attribute.AttributeSupplier
import cc.mewcraft.wakame.attribute.AttributeSupplierSerializer
import cc.mewcraft.wakame.element.ElementTypeRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.buildYamlConfigLoader

// TODO 把 Attribute 迁移到该 package 下

@Init(
    stage = InitStage.PRE_WORLD,
    runAfter = [
        ElementTypeRegistryLoader::class, // deps: 反序列化时必须知道所有已知的元素类型
    ]
)
@Reload
internal object AttributeSupplierRegistryLoader : RegistryLoader {
    const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        KoishRegistries.ATTRIBUTE_SUPPLIER.resetRegistry()
        applyDataToRegistry(KoishRegistries.ATTRIBUTE_SUPPLIER::add)
        KoishRegistries.ATTRIBUTE_SUPPLIER.freeze()
    }

    @ReloadFun
    fun reload() {
        applyDataToRegistry(KoishRegistries.ATTRIBUTE_SUPPLIER::update)
    }

    private fun applyDataToRegistry(registryAction: (Identifier, AttributeSupplier) -> Unit) {
        val loader = buildYamlConfigLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText()).node("entity_attributes")
        val dataMap = AttributeSupplierSerializer.deserialize(rootNode)
        dataMap.forEach { (k, v) ->
            registryAction(k, v)
        }
    }
}