package cc.mewcraft.wakame.display

/**
 * 为对象生成 [TooltipKey].
 *
 * 当生成一个对象的 [TooltipKey] 较为复杂时,
 * 应该考虑实现该接口来获取对象的 [TooltipKey].
 * 反之, 如果生成一个对象的 [TooltipKey] 很简单
 * (比如固定的), 那么就不必实现这个接口了.
 */
interface TooltipKeyProvider<T> {
    /**
     * 根据某种规则为 [obj] 生成它的 [TooltipKey].
     *
     * 返回 `null` 表示该 [obj] 不应该显示在物品提示框里.
     *
     * @return [obj] 的唯一标识
     */
    fun get(obj: T): TooltipKey?
}
