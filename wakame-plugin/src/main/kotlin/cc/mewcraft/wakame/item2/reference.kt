@file:JvmName("ItemReferenceSupport")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage

/**
 * 管理 [ItemRef] 的对象池.
 */
@Init(stage = InitStage.POST_WORLD)
internal object ItemRefValidator {

    @InitFun
    fun init() {

    }

}