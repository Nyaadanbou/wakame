package cc.mewcraft.wakame.initializer

/**
 * Something initializable.
 *
 * The functions will be called at certain stages.
 */
internal interface Initializable {
    /**
     * Before the world is loaded.
     */
    fun onPreWorld() {} // FIXME call it somewhere

    /**
     * Before the resource pack generation starts.
     */
    fun onPrePack() {}

    /**
     * After the first stage of resource pack generation ("pre-world") has
     * finished. Lookup registries are now loaded.
     */
    fun onPostPackPreWorld() {}

    /**
     * After the world has been loaded.
     */
    fun onPostWorld() {}

    /**
     * After the world has been loaded, in an async thread.
     */
    suspend fun onPostWorldAsync() {}

    /**
     * After the second (and last) stage of resource pack generation
     * ("post-world") has finished.
     */
    fun onPostPack() {}

    /**
     * After the second (and last) stage of resource pack generation
     * ("post-world") has finished, in an async thread.
     */
    suspend fun onPostPackAsync() {}
}