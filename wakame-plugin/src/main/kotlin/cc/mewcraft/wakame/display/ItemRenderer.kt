package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoItemStack
import org.bukkit.inventory.ItemStack
import org.jetbrains.annotations.Contract

/**
 * A renderer that renders name and lore for an [NekoItemStack].
 */
interface ItemRenderer {

    /**
     * Edits the [copy] **in-place** so that the [name][ItemStack.displayName]
     * and [lore][ItemStack.lore] are changed.
     *
     * ## Caution!
     *
     * **The caller must ensure the following:**
     * - The [copy] is truly "neko" (that is, [NekoItemStack.isNeko] returns
     *   `true`), or else it will throw.
     * - The [copy] is a copy of the original one, or else the original item
     *   will be changed as being rendered
     *
     * @param copy a copy of original [NekoItemStack] from the server side to
     *     be rendered
     * @throws IllegalArgumentException if the [copy] is not "neko"
     */
    @Contract(pure = false)
    fun render(copy: NekoItemStack)

}