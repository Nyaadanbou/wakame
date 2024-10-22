package cc.mewcraft.wakame.display2

import cc.mewcraft.wakame.display2.NekoItemHolder.ObjectPool.get
import cc.mewcraft.wakame.item.*
import cc.mewcraft.wakame.registry.ItemRegistry
import net.kyori.adventure.key.Key
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.commons.provider.Provider
import xyz.xenondevs.commons.provider.immutable.map
import xyz.xenondevs.commons.provider.immutable.provider
import java.util.concurrent.ConcurrentHashMap

// TODO 临时实现, 到时候重构 Registry
//  会有泛用性更强的 Holder 框架

/**
 * 快速构建一个持有指定 [NekoItem] 的容器.
 *
 * 推荐用法: 使用函数 [NekoItemHolder.get] 来获取一个实例.
 * 程序员不需要将实例储存起来, 因为内部会缓存和自动重新加载.
 * 使用相同的参数调用 [get] 永远会返回相同的 [NekoItem].
 * 当然, 如果注册表发生了重载, 那么可能会返回不同的实例.
 *
 * 如果传入的 [itemId] 所对应的 [NekoItem] 不存在于注册表中,
 * 那么函数 [createNekoStack] 和 [createItemStack] 将返回 *默认实例*.
 * 默认实例全局唯一, 且永远存在, 永远不为空. 如果程序员想要自定义默认实例,
 * 可以在配置文件中添加一个 `id` 为 [ItemRegistry.ERROR_NEKO_ITEM_ID] 的物品.
 *
 * @param itemId 萌芽物品的唯一标识
 */
@ConsistentCopyVisibility
internal data class NekoItemHolder
private constructor(
    private val itemId: Key,
    private val fallback: Key = ItemRegistry.ERROR_NEKO_ITEM_ID,
) {
    // 所有 property 必须懒加载 (provider) 以支持重载!

    private val prototype: Provider<NekoItem> = provider {
        ItemRegistry.CUSTOM.find(itemId) ?: ItemRegistry.CUSTOM.find(fallback) ?: ItemRegistry.CUSTOM[ItemRegistry.ERROR_NEKO_ITEM_ID]
    } // FIXME Provider#orElse 不触发更新

    private val realized: NekoStack by prototype.map(NekoItem::realize)

    private fun update() {
        prototype.update()
    }

    /**
     * 返回一个新的 [NekoStack] 实例.
     */
    fun createNekoStack(): NekoStack {
        return realized.clone()
    }

    /**
     * 返回一个新的 [ItemStack] 实例.
     */
    fun createItemStack(): ItemStack {
        return realized.itemStack
    }

    /**
     * [NekoItemHolder] 的对象池.
     */
    companion object ObjectPool {
        // thread-safe object pool
        private val POOL: ConcurrentHashMap<Key, NekoItemHolder> = ConcurrentHashMap()

        /**
         * @see get
         */
        fun get(id: String): NekoItemHolder {
            return get(Key.key(id))
        }

        /**
         * @see getOrDefault
         */
        fun getOrDefault(id: String, def: String): NekoItemHolder {
            return getOrDefault(Key.key(id), Key.key(def))
        }

        /**
         * 获取一个持有指定 [NekoItem] 的容器.
         *
         * @param id 萌芽物品的唯一标识
         */
        fun get(id: Key): NekoItemHolder {
            return POOL.computeIfAbsent(id, ::NekoItemHolder)
        }

        /**
         * 获取一个持有指定 [NekoItem] 的容器, 如果不存在则使用默认值.
         *
         * @param id 萌芽物品的唯一标识
         * @param def 默认的萌芽物品的唯一标识
         */
        fun getOrDefault(id: Key, def: Key): NekoItemHolder {
            return POOL.computeIfAbsent(id) { NekoItemHolder(id, def) }
        }

        fun reset() {
            POOL.clear()
        }

        fun reload() {
            val iterator = POOL.iterator()
            while (iterator.hasNext()) {
                val (_, holder) = iterator.next()
                holder.update()
            }
        }
    }
}