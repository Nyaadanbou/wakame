package cc.mewcraft.wakame.item.component

/**
 * 用来统一物品组件的具体实现.
 */
interface ItemComponentBridge<T> {

    /**
     * 物品组件的 [ItemComponentType].
     */
    fun codec(id: String): ItemComponentType<T>

}