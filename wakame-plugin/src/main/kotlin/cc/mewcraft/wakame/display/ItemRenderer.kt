package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoStack
import org.jetbrains.annotations.Contract

/**
 * A renderer that renders name and lore for an [NekoStack].
 */
interface ItemRenderer<T : NekoStack<*>> {

    /**
     * Edits the [nekoStack] **in-place** so that the item name and item lore
     * are updated to what is configured in the renderer configuration.
     *
     * ### Caution!
     *
     * The caller must ensure that the [nekoStack] is truly "neko", that is,
     * [NekoStack.isNeko] returns `true`, or else the behavior is undefined.
     *
     * @param nekoStack a [NekoStack] from the server side to be rendered
     */
    @Contract(pure = false)
    fun render(nekoStack: T)

}