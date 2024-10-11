package cc.mewcraft.wakame.display2

/**
 * 代表一种内容的渲染逻辑.
 *
 * ## 设计哲学
 * 为什么不叫 "ItemComponentRenderer" 或者 "ItemTemplateRenderer"?
 * 因为这个接口不仅会用于渲染物品的 NBT, 也会用于渲染物品的模板 (template).
 * NBT 和 模板这两者都属于数据 (data), 故取名 [IndexedDataRenderer].
 */
fun interface IndexedDataRenderer<in T, in F> {

    /**
     * 如果没有需要渲染的内容, 返回空集合.
     *
     * @param data 被渲染的数据
     * @param spec 特定的渲染格式
     *
     * @return 渲染后的文本
     */
    fun render(data: T, spec: F): IndexedText // TODO: 考虑返回一个 Array<LoreLine> 来节省内存?

}

fun interface IndexedDataRenderer2<in T1, in T2, in F> {
    fun render(data: T1, data2: T2, spec: F): Collection<IndexedText>
}

fun interface IndexedDataRenderer3<in T1, in T2, in T3, in F> {
    fun render(data1: T1, data2: T2, data3: T3, spec: F): Collection<IndexedText>
}

fun interface IndexedDataRenderer4<in T1, in T2, in T3, in T4, in F> {
    fun render(data1: T1, data2: T2, data3: T3, data4: T4, spec: F): Collection<IndexedText>
}

fun interface IndexedDataRenderer5<in T1, in T2, in T3, in T4, in T5, in F> {
    fun render(data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, spec: F): Collection<IndexedText>
}

fun interface IndexedDataRenderer6<in T1, in T2, in T3, in T4, in T5, in T6, in F> {
    fun render(data1: T1, data2: T2, data3: T3, data4: T4, data5: T5, data6: T6, spec: F): Collection<IndexedText>
}
