package cc.mewcraft.wakame.item.behavior

/**
 * 代表一个物品交互结果的封装.
 *
 * 物品交互结果: 玩家使用该物品与世界发生了交互后所发生的结果.
 */
interface ItemBehavior {

    /**
     * 玩家手持该物品对空气按下使用键 (默认为鼠标右键) 进行交互时, 执行的行为.
     */
    fun handleUse(context: UseContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对方块按下使用键 (默认为鼠标右键) 进行交互时, 执行的行为.
     */
    fun handleUseOn(context: UseOnContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对实体按下使用键 (默认为鼠标右键) 进行交互时, 执行的行为.
     */
    fun handleUseEntity(context: UseEntityContext): InteractionResult = InteractionResult.PASS

    /**
     * 玩家手持该物品对方块按下攻击键 (默认为鼠标左键) 进行交互时, 执行的行为.
     */
    fun handleAttackOn(context: AttackOnContext) = InteractionResult.PASS

    /**
     * 玩家手持该物品对空气按下攻击键 (默认为鼠标左键) 进行交互时, 执行的行为.
     */
    fun handleAttack(context: AttackContext) = InteractionResult.PASS

    /**
     * 玩家手持该物品对实体按下攻击键 (默认为鼠标左键) 进行交互时, 执行的行为.
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
     * 该物品失去耐久度时, 执行的行为.
     */
    fun handleDurabilityDecrease(context: DurabilityDecreaseContext) = BehaviorResult.PASS

    /**
     * 玩家手持该物品并停止使用时, 执行的行为.
     *
     * 相关事件无法取消 (取消也没有意义).
     */
    fun handleStopUse(context: StopUseContext) = BehaviorResult.PASS

    /**
     * 玩家手持该物品并消耗 (`minecraft:consumable` 物品组件) 时, 执行的行为.
     */
    fun handleConsume(context: ConsumeContext) = BehaviorResult.PASS

    // 2025/9/17 芙兰
    // 当确实需要用到上述 handle 未囊括的事件时, 再进行添加, 不建议写一堆无用的 handle.
    // 添加新 handle 时, 注意考虑 HoldLastDamage 行为是否需要处理它.
    // 例如 StopUse 就不被 HoldLastDamage 行为处理, 毕竟 StopUse 只可能由交互产生.
    // 而交互已然被 HoldLastDamage 行为考虑在内, 因此无需冗余代码.
}