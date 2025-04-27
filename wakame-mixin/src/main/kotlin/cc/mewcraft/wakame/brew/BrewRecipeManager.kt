package cc.mewcraft.wakame.brew

/**
 * 管理 [BrewRecipe].
 */
interface BrewRecipeManager : Iterable<BrewRecipe> {

    /**
     * 获取 [id] 对应的 [BrewRecipe].
     *
     * @param id 配方的 id
     * @return 如果 [id] 对应一个有效的配方则返回对应的配方, 否则返回 `null`
     */
    fun get(id: String): BrewRecipe?

    /**
     * 从配方库中随机返回一个 [BrewRecipe].
     * 如果配方库中没有任何配方则返回 `null`
     */
    fun random(): BrewRecipe?

    companion object {

        var INSTANCE: BrewRecipeManager = NoOp
            private set

        fun register(instance: BrewRecipeManager) {
            INSTANCE = instance
        }
    }

    /**
     * 无操作的 [BrewRecipeManager] 实现.
     */
    private object NoOp : BrewRecipeManager {
        override fun get(id: String): BrewRecipe? = null
        override fun random(): BrewRecipe? = null
        override fun iterator(): Iterator<BrewRecipe> = emptyList<BrewRecipe>().iterator()
    }
}