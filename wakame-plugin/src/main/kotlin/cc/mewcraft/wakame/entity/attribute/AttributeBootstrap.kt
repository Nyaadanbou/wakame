package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.attribute.AttributeMapAccessImpl
import cc.mewcraft.wakame.attribute.AttributeMapPatches
import cc.mewcraft.wakame.attribute.Attributes
import cc.mewcraft.wakame.attribute.ImaginaryAttributeMaps
import cc.mewcraft.wakame.element.ElementRegistryLoader
import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun

/**
 * 负责初始化属性系统的状态.
 */
internal interface AttributeBootstrap2 {

    @Init(
        stage = InitStage.PRE_WORLD, runAfter = [
            ElementRegistryLoader::class, // 调用 Attributes.init() 之前, 所有元素必须已加载完毕
        ]
    )
    object Pre {

        @InitFun
        fun init() {
            Attributes.init()
            AttributeMapAccess.register(AttributeMapAccessImpl)
            AttributeProvider.register(Attributes)
        }

    }

    @Init(
        stage = InitStage.POST_WORLD, runAfter = [
            AttributeSupplierRegistryLoader::class,
        ]
    )
    @Reload
    object Post {

        @InitFun
        fun init() {
            AttributeMapPatches.init()
            ImaginaryAttributeMaps.init()
        }

        @ReloadFun
        fun reload() {
            ImaginaryAttributeMaps.reload()
        }

        @DisableFun
        fun close() {
            AttributeMapPatches.close()
        }

    }

}