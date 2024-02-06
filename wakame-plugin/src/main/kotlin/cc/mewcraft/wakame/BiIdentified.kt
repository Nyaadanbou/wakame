package cc.mewcraft.wakame

/**
 * Represents something that is identified by both [STRING] and [BINARY].
 *
 * @param STRING the human-readable identifier, could be a plain [String]
 * @param BINARY the non-readable identifier, used for compact storage
 */
interface BiIdentified<STRING, BINARY> {
    /**
     * The readable identifier of this object.
     */
    val name: STRING

    /**
     * The binary identifier of this object.
     */
    val binary: BINARY
}