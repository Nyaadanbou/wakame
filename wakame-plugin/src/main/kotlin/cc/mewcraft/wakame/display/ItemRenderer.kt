package cc.mewcraft.wakame.display

/**
 * A renderer that modifies the look of items [T].
 */
interface ItemRenderer<in T> {

    /**
     * Edits the [item] **in-place** so that the item name and item lore
     * are updated to what is configured in the renderer configuration.
     *
     * @param item an item from the server side to be rendered
     */
    fun render(item: T)

}