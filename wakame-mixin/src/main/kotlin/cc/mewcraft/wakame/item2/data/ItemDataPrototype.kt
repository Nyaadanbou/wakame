package cc.mewcraft.wakame.item2.data

interface ItemDataPrototype<T> {

    fun generate(): T

    // FIXME: 这接口放在这只能当作标记作用了
    interface GenerationContext

}