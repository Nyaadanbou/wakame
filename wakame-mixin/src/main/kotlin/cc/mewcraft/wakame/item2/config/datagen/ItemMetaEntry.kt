package cc.mewcraft.wakame.item2.config.datagen

import cc.mewcraft.wakame.item2.data.ItemDataContainer
import cc.mewcraft.wakame.item2.data.ItemDataType
import cc.mewcraft.wakame.mixin.support.DataComponentsPatch
import cc.mewcraft.wakame.util.MojangStack

/**
 * 代表一个 "Item Data" 可持久化物品数据的配置项. 该接口的实例将存在于 [ItemMetaContainer] 中.
 *
 * @param V 对应的数据类型
 */
interface ItemMetaEntry<V> {

    /**
     * 根据上下文生成数据 [V].
     */
    fun make(context: Context): ItemMetaResult<V>

    /**
     * 向物品堆叠写入数据 [V].
     *
     * ### 实现注意事项
     * 如果要写入的数据是自定义数据类型, 而不是 Minecraft 自带的数据类型,
     * 应该使用函数 [cc.mewcraft.wakame.util.MojangStack.ensureSetData] 来确保数据可以写入成功.
     */
    fun write(value: V, itemstack: MojangStack)

    /**
     * 向该物品堆叠写入数据 [T], *无论该物品堆叠是否为合法的自定义物品*.
     *
     * @return 原有的值, 如果没有则返回 `null`
     */
    fun <T> MojangStack.ensureSetData(type: ItemDataType<in T>, value: T): T? {
        val container = getOrDefault(DataComponentsPatch.DATA_CONTAINER, ItemDataContainer.Companion.EMPTY)
        val builder = container.toBuilder()
        val oldVal = builder.set(type, value)
        set(DataComponentsPatch.DATA_CONTAINER, builder.build())
        return oldVal
    }

    /**
     * 向该物品堆叠写入数据 [T], *无论该物品堆叠是否为合法的自定义物品*.
     *
     * @return 原有的值, 如果没有则返回 `null`
     */
    fun <T> MojangStack.ensureRemoveData(type: ItemDataType<out T>): T? {
        val container = getOrDefault(DataComponentsPatch.DATA_CONTAINER, ItemDataContainer.Companion.EMPTY)
        val builder = container.toBuilder()
        val oldVal = builder.remove(type)
        set(DataComponentsPatch.DATA_CONTAINER, builder.build())
        return oldVal
    }

}