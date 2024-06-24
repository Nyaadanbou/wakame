package cc.mewcraft.wakame.display

interface LineKeyFactory<T> {
    /**
     * 根据某种规则为特定的 [obj] 生成唯一的标识。
     *
     * 返回 `null` 表示该 [obj] 不应该显示在物品提示框里。
     *
     * @return [obj] 的唯一标识
     */
    fun get(obj: T): TooltipKey?
}
