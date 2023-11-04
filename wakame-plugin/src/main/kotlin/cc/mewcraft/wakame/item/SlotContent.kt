package cc.mewcraft.wakame.item

import net.kyori.adventure.key.Keyed

/**
 * An abstract content in a slot, which can be an attribute (modifier), an element type, an item level, a skill...
 */
interface SlotContent : Keyed {
    companion object {
        const val SKILL_NAMESPACE = "skill"
        const val ATTRIBUTE_NAMESPACE = "attribute"
    }
}