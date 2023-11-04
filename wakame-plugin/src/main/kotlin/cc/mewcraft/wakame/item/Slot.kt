package cc.mewcraft.wakame.item

/**
 * A slot on item that contains [SlotContent]s.
 */
interface Slot {
    /**
     * Generates an attribute from this container.
     * The returned attribute is non-deterministic.
     */
    fun generateAttribute(): SlotContent
}