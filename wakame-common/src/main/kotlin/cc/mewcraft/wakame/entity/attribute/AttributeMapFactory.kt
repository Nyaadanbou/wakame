package cc.mewcraft.wakame.entity.attribute

import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.jetbrains.annotations.ApiStatus

/**
 * 用于为各种实体创建对应的 [AttributeMap].
 *
 * 只有需要明确的 *创建* [AttributeMap] 对象的代码需要使用到此接口, 例如当玩家登录服务器时.
 * 对于大部分代码应该只需要 *访问* 实体的 [AttributeMap] - 直接使用 [AttributeMapAccess].
 *
 * @see AttributeMapAccess
 */
interface AttributeMapFactory {

    companion object {

        @get:JvmStatic
        @get:JvmName("getInstance")
        lateinit var INSTANCE: AttributeMapFactory private set

        @ApiStatus.Internal
        fun register(instance: AttributeMapFactory) {
            this.INSTANCE = instance
        }

    }

    /**
     * 基于玩家 [player] 创建一个新的 [AttributeMap] 对象.
     *
     * 该函数本身会对 [player] 产生副作用, 详见实现.
     *
     * 在 [player] 对象的整个生命周期内 (从进入服务器开始, 直到退出服务器之前),
     * 必须保证仅创建一个 [AttributeMap] 并且必须在特定地方与 [player] 绑定.
     *
     * 在 [player] 对象的生命周期里如果需要再次访问其 [AttributeMap],
     * 应该使用 [AttributeMapAccess] 而不是用该函数创建一个新的对象.
     *
     * @see AttributeMapAccess
     */
    fun create(player: Player): AttributeMap

    /**
     * 基于实体 [entity] 创建一个新的 [AttributeMap] 对象.
     *
     * 该函数本身会对 [entity] 产生副作用, 详见实现.
     *
     * @see AttributeMapAccess
     */
    fun create(entity: LivingEntity): AttributeMap

}