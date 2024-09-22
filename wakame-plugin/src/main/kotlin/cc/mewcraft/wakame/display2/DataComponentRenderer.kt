package cc.mewcraft.wakame.display2

/**
 * 代表一种内容的渲染逻辑.
 *
 * ## 设计哲学
 * 为什么不叫 "ItemComponentRenderer" 或者 "ItemTemplateRenderer"?
 * 因为这个接口不仅会用于渲染物品的 NBT, 也会用于渲染物品的模板 (template).
 * NBT 和 模板这两者都属于数据 (data), 故取名 [DataComponentRenderer].
 */
fun interface DataComponentRenderer<in T, in F> {

    /**
     * 如果没有需要渲染的内容, 返回空集合.
     *
     * @param data 被渲染的数据
     * @param spec 渲染的格式
     *
     * @return 渲染后的文本
     */
    fun render(data: T, spec: F): Collection<IndexedText>
    // TODO: 考虑返回一个 Array<LoreLine> 来节省内存?

}
