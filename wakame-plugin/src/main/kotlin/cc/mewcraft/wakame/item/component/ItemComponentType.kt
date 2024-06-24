package cc.mewcraft.wakame.item.component

// FIXME 思考: 该接口定义的只是 wakame 的组件? 还是要包括原版的组件?
//  也许分开才是上策 ... 统一和抽象会牺牲一部分代码可维护性其实

/**
 * 代表了一个物品组件的类型.
 *
 * 封装了一个物品组件的读取/写入/移除逻辑.
 *
 * @param T 组件的快照类型, 属于 immutable class
 * @param S 储存了物品组件信息的容器类型
 */
interface ItemComponentType<T, S : ItemComponentHolder> {

    /**
     * `组件类型`的唯一标识. 用于配置文件和NBT的序列化.
     */
    val id: String

    /**
     * 标记该`组件类型`构建的`组件快照`需要用到的数据源.
     */
    val holder: Holder // 开发日记: 该成员是为了解决泛型擦除导致无法获取 ItemComponentHolder 类型的问题

    // 开发日记: 2024/6/24 小米
    // 对于 Holder 为 NBT 的组件来说, read/write/remove 函数是不需要实现的 - 它们实际上不会被调用

    fun read(holder: S): T?
    fun write(holder: S, value: T)
    fun remove(holder: S)

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
     * 标记一个 [ItemComponentType] 的构建需要用到的数据源.
     */
    enum class Holder {
        /**
         * 数据源为 NBT, 本质是原版物品上的 `custom_data` 组件
         */
        NBT,

        /**
         * 数据源为物品本身, 本质是原版物品上的某一个或多个组件
         */
        ITEM,

        /**
         * 数据源为 [NBT] 加上 [ITEM].
         */
        COMPLEX,

        // FIXME 思考: 到底需不需要这个? 初始答案似乎是不需要:
        //  例如 Attributable, 数据源它属于 NBT, 组件类型它属于 NonValued,
        //  这种情况直接从 `components` 移除它的 tag 就好.
        //  我们从 NBT + NonValue 的组合就可以作出这个决策.
        // /**
        //  * 无数据源 (???)
        //  */
        // NONE,
    }

}

