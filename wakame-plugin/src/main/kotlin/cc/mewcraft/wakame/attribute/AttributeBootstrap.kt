package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.initializer.Initializable

/**
 * 负责初始化伤害系统 API 的一些实例.
 */
internal object AttributeBootstrap : Initializable {
    override fun onPreWorld() {

        // 注册 AttributeMapAccess
        AttributeMapAccess.register(DefaultAttributeMapAccess)

        // 注册 AttributeProvider
        AttributeProvider.register(Attributes)

    }
}