package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.NekoStack
import net.kyori.adventure.text.Component

/**
 * Generates stylized name and lore for a [NekoStack].
 */
internal interface TextStylizer {

    /**
     * Generates a custom name for the [item]。
     *
     * Unlike [stylizeLore], the returned component is ready to be used
     * on the item.
     *
     * This function won't modify the given [item].
     *
     * @param item the item to generate name for
     * @return the generated name
     */
    fun stylizeName(item: NekoStack<*>): Component

    /**
     * Generates [lore lines][LoreLine] for the [item]. The returned
     * [lore lines][LoreLine] need to be **finalized** before they are used
     * on [item]. Also, the returned collection should not contain any
     * [constant lore lines][ConstantLoreLine].
     *
     * This function won't modify the given [item].
     *
     * @param item the item to generate lore for
     * @return the generated lore lines
     */
    fun stylizeLore(item: NekoStack<*>): Collection<LoreLine>

}
