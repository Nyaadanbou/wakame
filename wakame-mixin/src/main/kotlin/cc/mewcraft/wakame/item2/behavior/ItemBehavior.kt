package cc.mewcraft.wakame.item2.behavior

/**
 * 代表一个“物品交互结果”的封装.
 *
 * 物品交互结果, 即玩家使用该物品与世界发生了交互后所发生的结果.
 *
 * ## 注意事项
 * 本接口覆盖了绝大部分与世界进行交互的事件, 但这里特别不包含 [org.bukkit.event.player.PlayerItemHeldEvent] 和 [io.papermc.paper.event.player.PlayerInventorySlotChangeEvent].
 * 因为这两事件并不在“物品交互结果”这个范畴内, 它们并没有让物品与世界发生交互, 而仅仅是玩家自身的状态发生了变化而已. 这样看来, 这两个事件不符合“物品交互结果”的定义,
 * 因此它们也不应该被放到 [ItemBehavior] 这个架构下.
 *
 * **而且经过我们的实践证明, 这两个事件确实是没有办法纳入 [ItemBehavior] 这个架构下的.**
 *
 * 下面分别解释一下这两个事件.
 *
 * ## [org.bukkit.event.player.PlayerItemHeldEvent]
 * 该事件仅仅是玩家切换了手持的物品, 没有与世界发生交互.
 * 而且, 该事件涉及到两个物品, 一个切换之前的, 一个切换之后的.
 *
 * ## [io.papermc.paper.event.player.PlayerInventorySlotChangeEvent]
 * 玩家背包内的某个物品发生了变化 (包括从空气变成某个物品), 没有与世界发生交互.
 * 从空气变成某个物品, 其实就包括了玩家登录时的情况. 你可以把玩家刚登录时的背包当成是空的,
 * 然后服务端会一个一个根据地图存档里的数据, 将背包里的物品一个一个填充回去.
 * 也就是说, 玩家登录时对于背包里的每个非空气物品都会触发一次该事件.
 */
interface ItemBehavior {
    /**
     * 玩家手持该物品对方块按下使用键(默认为鼠标右键)进行交互时, 执行的行为.
     */
    fun handleUseOn(context: UseOnContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对空气按下使用键(默认为鼠标右键)进行交互时, 执行的行为.
     */
    fun handleUse(context: UseContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对实体按下使用键(默认为鼠标右键)进行交互时, 执行的行为.
     */
    fun handleUseEntity(context: UseEntityContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对方块按下攻击键(默认为鼠标左键)进行交互时, 执行的行为.
     */
    fun handleAttackOn(context: AttackOnContext) = InteractionResult.PASS

    /**
     * 玩家手持该物品对空气按下攻击键(默认为鼠标左键)进行交互时, 执行的行为.
     */
    fun handleAttack(context: AttackContext) = InteractionResult.PASS

    /**
     * 玩家手持该物品对实体按下攻击键(默认为鼠标左键)进行交互时, 执行的行为.
     */
    fun handleAttackEntity(context: AttackEntityContext) = InteractionResult.PASS

    /**
     * 该物品处于激活状态且玩家造成伤害时, 执行的行为.
     */
    fun handleCauseDamage(context: CauseDamageContext) = BehaviorResult.PASS

    /**
     * 该物品处于激活状态且玩家受到伤害时, 执行的行为.
     */
    fun handleReceiveDamage(context: ReceiveDamageContext) = BehaviorResult.PASS

    /**
     * 该物品作为箭矢/光灵箭/三叉戟射出时, 执行的行为.
     */
    fun handleProjectileLaunch(context: ProjectileLaunchContext) = BehaviorResult.PASS

    /**
     * 该物品作为箭矢/光灵箭/三叉戟命中目标时, 执行的行为.
     */
    fun handleProjectileHit(context: ProjectileHitContext) = BehaviorResult.PASS

    /**
     * 该物品失去耐久度时, 执行的行为.
     */
    fun handleDurabilityDecrease(context: DurabilityDecreaseContext) = BehaviorResult.PASS

    /**
     * 玩家手持该物品并停止使用时, 执行的行为.
     * 相关事件无法取消(取消也没有意义).
     */
    fun handleStopUse(context: StopUseContext) = BehaviorResult.PASS

    /**
     * 玩家手持该物品并消耗(Consumable组件)时, 执行的行为.
     */
    fun handleConsume(context: ConsumeContext) = BehaviorResult.PASS

    // 2025/9/17 芙兰
    // 当确实需要用到上述handle未囊括的事件时, 再进行添加, 不建议写一堆无用的handle.
    // 添加新handle时, 注意考虑 HoldLastDamage 行为是否需要处理它.
    // 例如StopUse就不被 HoldLastDamage 行为处理, 毕竟StopUse只可能由交互产生.
    // 而交互已然被 HoldLastDamage 行为考虑在内, 因此无需冗余代码.
}

/**
 * 无任何行为的 [ItemBehavior].
 */
object EmptyItemBehavior : ItemBehavior