@file:JvmName("ItemReferenceApi")

package cc.mewcraft.wakame.item2

import cc.mewcraft.wakame.item2.ItemRef.Companion.checkAll
import cc.mewcraft.wakame.item2.ItemRef.Companion.uncheckedItemRef
import cc.mewcraft.wakame.registry2.BuiltInRegistries
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.item.toJsonString
import cc.mewcraft.wakame.util.require
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.serialize.SerializationException

// ------------
// 物品引用 ItemRef API
//
// 用于统一 指定/查询/比较 *物品类型* 的方式
// ------------

/**
 * 本接口代表一个*物品类型*的引用. 引用的形式为 [Identifier].
 *
 * ### 引用 & 物品类型 & 物品堆叠
 * 本接口的所有属性和函数的行为, 均只考虑对于各个物品系统内部而言的*物品类型*, 不会判断物品堆叠上产生的额外数据.
 *
 * 例如:
 * - 在 Minecraft 的物品系统下, 拥有不同附魔的游戏原版钻石剑将全部算作是同一个 [ItemRef].
 * - 在 Koish 的物品系统物品下, 等级不一样但物品ID一样的冒险者之剑将全部算作是同一个 [ItemRef].
 * - 来自不同物品系统的 [ItemRef] 永远算作不同的引用.
 *
 * ### 本系统作出的假设
 * 本系统假设所有物品系统的所有物品类型在服务端启动时就已经确定, 不会减少也不会增加.
 * 要使该假设成立很简单, 只需要不在服务端运行时去增加或移除物品类型即可.
 * 而幸运的是如此简单的假设却可以大大的降低本系统的复杂度.
 */
sealed interface ItemRef {

    companion object {

        /**
         * [ItemRef] 的序列化器.
         *
         * 该序列化器始终会将字符串反序列化为一个有效的 [ItemRef].
         * 如果字符串并不对应一个有效的 [ItemRef], 则会抛出异常.
         */
        @JvmField
        val SERIALIZER: TypeSerializer2<ItemRef> = TypeSerializer2 { type, node ->
            val id = node.require<Identifier>()
            checkedItemRef(id) ?: throw SerializationException(
                node,
                type,
                "$id does not correspond to a valid ItemRef."
            )
        }

        /**
         * 验证到目前为止创建出来的所有 [ItemRef].
         * 任何无效的 [ItemRef] 将出现在返回值中.
         *
         * 本函数可以*反复*调用. 例如(从上往下的顺序调用):
         * - 合成系统使用 [uncheckedItemRef] 创建 5 个引用
         * - 合成系统调用 [checkAll] 后, 未发现无效引用, 程序继续
         * - 图鉴系统使用 [uncheckedItemRef] 创建 10 个引用
         * - 图鉴系统调用 [checkAll] 后, 未发现无效引用, 程序继续
         * - 技能系统调用 [uncheckedItemRef] 创建 2 个引用
         * - 技能系统调用 [checkAll] 后, 发现了无效引用, 记录并终止程序
         */
        fun checkAll(): String {
            return ItemRefManager.checkAll()
        }

        /**
         * 从 [id] 创建一个 [ItemRef] (可能会复用现有实例).
         *
         * 该函数对于任何 [id] 始终会返回一个 [ItemRef] 实例, 但如果未经检查
         * (即调用 [checkAll]) 而直接调用返回实例上面的属性/函数将会抛出异常.
         *
         * 也就是说, 使用该函数意味着调用方可以分别执行“创建和检查” [ItemRef] 的逻辑, 而非“创建即检查”.
         *
         * 具体的说:
         *
         * 使用该函数创建的 [ItemRef] 必须在 *之后的某个时机* 调用函数 [checkAll] 来验证其有效性.
         * 如果发现了无效的 [ItemRef], 即引用没有指向一个有效的物品类型, 调用方应该*记录并终止程序*.
         * 只使用该函数而不调用 [checkAll] 将导致此函数返回的 [ItemRef] 永远无效, 导致使用时抛异常.
         *
         * 该函数可能的使用场景:
         * 正在加载配置文件的内容, 并且不希望单独处理 `ItemRef?`.
         */
        fun uncheckedItemRef(id: Identifier): ItemRef {
            return ItemRefManager.createUnchecked(id)
        }

        /**
         * 从 [id] 创建一个 [ItemRef] (可能会复用现有实例).
         *
         * 如果当前已注册的 [ItemRefHandler] 都无法识别 [id], 则返回 `null`.
         *
         * 该函数可能的使用场景:
         * 游戏处于运行状态时, 需要接收来自玩家的输入 (在此场景下, 服务器管理员也算玩家).
         */
        fun checkedItemRef(id: Identifier): ItemRef? {
            return ItemRefManager.createChecked(id)
        }

        /**
         * 从 [stack] 创建一个 [ItemRef] (可能会复用现有实例).
         *
         * 该函数对于任何 [ItemStack] 都会返回一个有效的 [ItemRef], 而不是返回 `null` 或抛异常.
         * 如果没有物品系统可以理解 [stack], 一个 Minecraft 系统下的 [ItemRef] 将被作为兜底返回.
         */
        fun checkedItemRef(stack: ItemStack): ItemRef {
            val handler = ItemRefManager.getHandler(stack) ?: error("Cannot get handler from ItemStack: ${stack.toJsonString()}. This is a bug!")
            val id = handler.getId(stack) ?: error("Cannot get type id from ItemStack: ${stack.toJsonString()}. This is a bug!")
            return checkedItemRef(id) ?: error("Cannot get reference from ItemStack: ${stack.toJsonString()}. This is a bug!")
        }

        /**
         * 从 [material] 创建一个 [ItemRef].
         */
        fun checkedItemRef(material: Material): ItemRef {
            return checkedItemRef(material.key) ?: error("Cannot get reference from Material: $material. This is a bug!")
        }

    }

    /**
     * 用来区分不同 [ItemRef] 的唯一标识.
     * 其命名空间用来识别引用所属的物品系统.
     */
    val id: Identifier

    /**
     * 获取一个表示该物品类型的物品名字. 该名字可以展示给玩家, 并被玩家所理解.
     */
    val name: Component

    /**
     * 判断传入的 [id] 是否和当前 [ItemRef] 匹配.
     */
    fun matches(id: Identifier): Boolean

    /**
     * 判断传入的 [ref] 是否和当前 [ItemRef] 匹配.
     */
    fun matches(ref: ItemRef): Boolean

    /**
     * 判断传入的 [stack] 是否和当前 [ItemRef] 匹配.
     */
    fun matches(stack: ItemStack): Boolean

    // 疑问: 对于具有随机性的物品生成, 考虑支持在此控制让其不生成随机的数据, 以提高渲染的性能.
    // 因为在某些场景下(如合成站和图鉴)渲染具有随机性的物品时, 这些随机生成的数据都将被忽略.
    /**
     * 从该 [ItemRef] 创建一个 [ItemStack].
     *
     * @param amount 该物品堆叠的数量, 受游戏本身限制
     * @param player 接收该物品堆叠的玩家. 传入此参数可以让某些系统基于该玩家来生成物品
     * @return 新生成的 [ItemStack]
     * @throws ItemStackGenerationException 如果物品堆叠创建失败
     */
    fun createItemStack(amount: Int = 1, player: Player? = null): ItemStack
}

// ------------
// ItemRefHandler
// 实现该接口以支持处理来自特定系统的物品类型
// ------------

/**
 * @param T 该物品系统之下的内部物品类型
 */
interface ItemRefHandler<T> {

    /**
     * 实现所代表的物品系统的名字, 比如 "Minecraft", "Koish"
     */
    val systemName: String

    /**
     * 判断该系统是否可以处理传入的 id.
     * 必须检查传入的 id 是否指向该系统内一个有效的物品类型.
     *
     * 对于 Koish 和 Minecraft:
     * 这里必须完整的实现, 即从完整的注册表查询 id 的有效性.
     *
     * 对于其他系统 (比如 Brewery):
     * 这里可以只检查 namespace 是否属于该系统, 因为当此函数调用时第三方系统, 无法直接知道物品的注册表是否加载完毕.
     * 也就是说如果 id 是无效的, 那么问题会出现在下面这些函数被调用时, 而非出现在 ItemRef 框架内专门的 id 验证阶段.
     * 而 Koish 能做的就是尽可能的晚的调用下面这些函数.
     * 目前已经做了的事情包括: 让合成配方的注册发生在游戏的第一个 tick, 而非 JavaPlugin#onEnable 中.
     */
    fun accepts(id: Identifier): Boolean

    /**
     * 获取传入的 [ItemStack] 在该系统之下的物品类型 id.
     * 如果该系统无法识别传入的 [ItemStack] 则应该返回 null.
     */
    fun getId(stack: ItemStack): Identifier?

    /**
     * 获取物品 id 对应的物品类型名字, 用于直接展示给玩家.
     *
     * 实现应该假设 id 永远是有效 id.
     * 对于无效的 id 应该返回 null (bug).
     */
    fun getName(id: Identifier): Component?

    /**
     * 在该系统下获取 id 对应的物品类型 T.
     * 对于无效的 id 应该返回 null (bug).
     */
    fun getInternalType(id: Identifier): T?

    /**
     * 从 [id] 和基于可能存在的 [player] 创建一个新的 [ItemStack].
     *
     * 实现应该假设 id 永远是有效 id.
     * 对于无效的 id 应该返回 null (bug).
     */
    fun createItemStack(id: Identifier, amount: Int, player: Player?): ItemStack?

    /**
     * 判断两个物品 id 是否一致.
     *
     * 实现应该假设 [xId] 永远是有效 id, 但对于 [yId] 可以假设其为无效 id.
     * 对于无效的 [xId] 可以直接抛出异常 (bug).
     */
    fun matches(xId: Identifier, yId: Identifier): Boolean {
        return xId == yId
    }

    /**
     * 判断两个物品 id 是否一致.
     *
     * ItemRef 只是个形如 `"x:y"` 的 id.
     * 所以同上, 只不过是对于 [ItemRef].
     */
    fun matches(xId: Identifier, yRef: ItemRef): Boolean {
        val yId = yRef.id
        return matches(xId, yId)
    }

    /**
     * 判断两个物品 id 是否一致.
     *
     * 同上, 只不过是对于 [ItemStack].
     */
    fun matches(xId: Identifier, yStack: ItemStack): Boolean {
        val yId = getId(yStack) ?: return false
        return matches(xId, yId)
    }

    /**
     * 方便函数, 用于当物品类型不存在时, 统一抛出异常的逻辑.
     */
    fun throwItemTypeNotFound(id: Identifier): Nothing {
        val cause = IllegalArgumentException("Cannot find a item type in '$systemName' item system by: $id. This is a bug!")
        throw ItemStackGenerationException(id, cause)
    }
}

// ------------
// 内部实现
// ------------

/**
 *  [ItemRef] 的一般实现.
 */
private data class ItemRefImpl(
    override val id: Identifier,
) : ItemRef {

    // 将在验证成功后赋值变为非空
    @JvmField
    var handler: ItemRefHandler<*>? = null

    private val handlerOrThrow: ItemRefHandler<*>
        get() {
            val handler = handler
            if (handler == null) throw IllegalStateException(
                "ItemRefHandler is not initialized. This is a bug! To fix this, ensure that all ItemRefs have been checked before actually using them."
            )
            return handler
        }

    override val name: Component
        get() = handlerOrThrow.getName(id) ?: handlerOrThrow.throwItemTypeNotFound(id)

    override fun matches(id: Identifier): Boolean {
        // 虽然可以在这里 `this.id == id`, 但使用 handlerOrThrow 是为了满足 API 的定义, 也就是: 调用未检查的 ItemRef 实例函数应该抛出异常
        return handlerOrThrow.matches(this.id, id)
    }

    override fun matches(ref: ItemRef): Boolean {
        // 虽然可以在这里 `this == ref`, 但使用 handlerOrThrow 是为了满足 API 的定义
        return handlerOrThrow.matches(this.id, ref)
    }

    override fun matches(stack: ItemStack): Boolean {
        return handlerOrThrow.matches(this.id, stack)
    }

    override fun createItemStack(amount: Int, player: Player?): ItemStack {
        return handlerOrThrow.createItemStack(this.id, amount, player) ?: handlerOrThrow.throwItemTypeNotFound(id)
    }
}

/**
 * 管理 [ItemRef] 的对象池.
 */
private object ItemRefManager {

    // 已验证的 ItemRef
    private val checkedItemRefs: HashMap<Identifier, ItemRefImpl> = HashMap()

    // 未验证的 ItemRef
    private val uncheckedItemRefs: HashMap<Identifier, ItemRefImpl> = HashMap()

    fun createUnchecked(id: Identifier): ItemRef {
        return uncheckedItemRefs.computeIfAbsent(id, ::ItemRefImpl)
    }

    fun createChecked(id: Identifier): ItemRef? {
        val handler = getHandler(id) ?: return null
        return checkedItemRefs.computeIfAbsent(id, ::ItemRefImpl).also { it.handler = handler }
    }

    fun getHandler(id: Identifier): ItemRefHandler<*>? {
        return getHandler { handler -> handler.accepts(id) }
    }

    fun getHandler(stack: ItemStack): ItemRefHandler<*>? {
        return getHandler { handler -> handler.getId(stack) != null }
    }

    // 返回符合 predicate 条件的 ItemRefHandler
    private inline fun getHandler(predicate: (ItemRefHandler<*>) -> Boolean): ItemRefHandler<*>? {
        // 先从由外部注册的实例中寻找支持的 handler
        for (handler in BuiltInRegistries.ITEM_REF_HANDLER_EXTERNAL) {
            if (predicate(handler)) {
                return handler
            }
        }
        // 再从 Koish 内置的实例中寻找支持的 handler
        for (handler in BuiltInRegistries.ITEM_REF_HANDLER_INTERNAL) {
            if (predicate(handler)) {
                return handler
            }
        }
        // 没有符合条件的 handler
        return null
    }

    fun checkAll(): String {
        val invalidRefs = mutableListOf<String>()

        val iter = uncheckedItemRefs.iterator()
        while (iter.hasNext()) {
            val (id, ref) = iter.next()
            val handler = getHandler(id)
            if (handler != null) {
                // 找到了支持的 handler, 分配给这个 ref
                ref.handler = handler
                // 将 ref 放入已检查集合中, 并从未检查集合移除
                checkedItemRefs[id] = ref
                iter.remove()
            } else {
                // 未找到任何支持的 handler, 验证失败
                invalidRefs += id.toString()
            }
        }

        // TODO #350: 返回值应该是一个专门的对象而不是字符串, 以方便调用者去记录/处理这些无效的 ItemRef.
        return invalidRefs.joinToString(", ")
    }

}
