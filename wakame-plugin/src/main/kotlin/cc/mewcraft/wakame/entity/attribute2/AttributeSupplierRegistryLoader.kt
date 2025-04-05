package cc.mewcraft.wakame.entity.attribute2

import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.entity.attribute.AttributeSupplier
import cc.mewcraft.wakame.entity.attribute.AttributeSupplierSerializer
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.RegistryLoader
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.yamlLoader

@Init(
    stage = InitStage.PRE_WORLD, runAfter = [
        ElementRegistryLoader::class, // deps: 反序列化时必须知道所有已知的元素类型
    ]
)
@Reload
internal object AttributeSupplierRegistryLoader : RegistryLoader {
    const val FILE_PATH = "entities.yml"

    @InitFun
    fun init() {
        KoishRegistries.ATTRIBUTE_SUPPLIER.resetRegistry()
        consumeData(KoishRegistries.ATTRIBUTE_SUPPLIER::add)
        KoishRegistries.ATTRIBUTE_SUPPLIER.freeze()
    }

    @ReloadFun
    fun reload() {
        consumeData(KoishRegistries.ATTRIBUTE_SUPPLIER::update)
    }

    private fun consumeData(registryAction: (Identifier, AttributeSupplier) -> Unit) {
        val loader = yamlLoader { withDefaults() }
        val rootNode = loader.buildAndLoadString(getFileInConfigDirectory(FILE_PATH).readText()).node("entity_attributes")
        val dataMap = AttributeSupplierSerializer.deserialize(rootNode)
        dataMap.forEach { (k, v) ->
            registryAction(k, v)
        }
    }
}