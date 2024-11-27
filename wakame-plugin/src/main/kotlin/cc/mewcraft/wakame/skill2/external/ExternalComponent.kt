package cc.mewcraft.wakame.skill2.external

import com.github.quillraven.fleks.Component

/**
 * 用于与外部交互的组件包装类.
 *
 * @param T 组件的类型.
 */
interface ExternalComponent<T : Component<*>> {
    /**
     * 获取内部组件.
     */
    fun internal(): T
}