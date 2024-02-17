package cc.mewcraft.wakame.display

import net.kyori.adventure.text.Component
import org.bukkit.inventory.ItemStack

internal interface LoreLineFinalizer {

    /**
     * Finalizes the [lore lines][loreLines] so that it's converted to a list
     * of component and then can be put on an [ItemStack] as a lore.
     *
     * The finalization process includes the following (but not least):
     * - sorting the [lore lines][LoreLine] by certain order
     * - inserting certain text line into the lore
     *
     * See the implementation for more details.
     *
     * @param loreLines a collection of [lore lines][LoreLine] to be finalized
     * @return a list of [components][Component] which is ready to be an item
     *     lore
     */
    fun finalize(loreLines: Collection<LoreLine>): List<Component>

}