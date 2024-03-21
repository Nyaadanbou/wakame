package cc.mewcraft.wakame

/**
 * Represents something that is identified by both [K] and [B].
 *
 * @param K the human-readable identifier, such as namespaced key
 * @param B the identifier in binary form, used for compact storage
 */
interface BiIdentifiable<K, B> {
    /**
     * The readable identifier of this object.
     */
    val uniqueId: K

    /**
     * The binary identifier of this object.
     */
    val binaryId: B
}