package cc.mewcraft.wakame.item.component

import cc.mewcraft.nbt.CompoundTag

// 开发日记 2024/6/26
// 分为 Valued/NonValued 的原因:
// 有些组件不带值, 只有存在与否一说
// 不分为 Valued/NonValue 的原因:
// 每个组件都有可能需要提供 LoreLine,
// 那怕是 NonValued 也可能需要提供一个固定的 LoreLine,
// 而 Map 里的 get 函数又直接返回的
// 这种情况提供 LoreLine

// 开发日记 2024/6/26
// 叫做 ItemComponentType, 但实际上是 ItemComponentCodec 的作用
// (因为 read/write/remove).
// hmm, 要改个名字吗?

/**
 * 代表了一个物品组件的类型.
 *
 * 封装了一个物品组件的读取/写入/移除逻辑.
 *
 * @param T 组件的快照类型, 属于 immutable class
 */
interface ItemComponentType<T> {

    /**
     * `组件类型`的唯一标识. 用于配置文件和NBT的序列化.
     */
    val id: String

    // 开发日记: 2024/6/24 小米
    // 对于 Holder 为 NBT 的组件来说, read/write/remove 函数是不需要实现的 - 它们实际上不会被调用

    /**
     * 读取 [holder] 中包含的组件信息 [T].
     */
    fun read(holder: ItemComponentHolder): T?

    /**
     * 将组件信息 [value] 写入到 [holder] 中.
     */
    fun write(holder: ItemComponentHolder, value: T)

    /**
     * 从 [holder] 中移除当前的组件信息 [T].
     */
    fun remove(holder: ItemComponentHolder)

    // 开发日记 2024/6/26
    // Type 提供一个专门的实现来提供 LoreLine?
    // 这样的问题在于有些组件可能会提供多个 LoreLine,
    // 因此这里单单返回一个 LoreLine 应该是不够的.
    // fun render(holder: ItemComponentHolder): LoreLine

    // 开发日记 1
    // ItemComponentType 应该尽可能的能够同时包含
    // wakame 的物品组件和 vanilla 的物品组件.
    //
    // 这样的话例如想要获取一个物品的食物数据,
    // 只需要从 wakame 的 API 直接获取就行.
    //
    // 缺点: 实现上需要再封装一次, 有额外的维护和性能开销
    // 优点: 统一的接口方便调用, 加了一层抽象方便添加中间逻辑

    // 开发日记 2
    // 对于有些 ItemComponent, 他可能是 NBT + 原版组件的复合体,
    //  例如 wakame 自定义的 FoodComponent, 还带了个吃完食物后释放的技能
    //  对于这种情况, 是不是需要同时传入 item 和 nbt ?

    /**
     * 快速访问当前组件的 NBT 标签.
     */
    fun ItemComponentHolder.hasTag(): Boolean = this.hasTag(id)

    /**
     * 快速访问当前组件的 NBT 标签.
     */
    fun ItemComponentHolder.getTag(): CompoundTag? = this.getTag(id)

    /**
     * 快速访问当前组件的 NBT 标签.
     */
    fun ItemComponentHolder.getTagOrCreate(): CompoundTag = this.getTagOrCreate(id)

    /**
     * 快速访问当前组件的 NBT 标签.
     */
    fun ItemComponentHolder.putTag() = this.putTag(id)

    /**
     * 快速访问当前组件的 NBT 标签.
     */
    fun ItemComponentHolder.removeTag() = this.removeTag(id)
}