package cc.mewcraft.wakame.brew

/**
 * 管理 [BrewRecipe].
 */
interface BrewRecipeManager : Iterable<BrewRecipe> {

    /**
     * 检查 [id] 是否对应一个有效的配方.
     */
    fun validate(id: String): Boolean

    /**
     * 从配方库中随机返回一个 [BrewRecipe].
     * 如果配方库中没有任何配方则返回 `null`
     */
    fun random(): BrewRecipe?

    companion object {

        private val NO_OP: BrewRecipeManager = object : BrewRecipeManager {
            override fun validate(id: String): Boolean = false
            override fun random(): BrewRecipe? = null
            override fun iterator(): Iterator<BrewRecipe> = emptyList<BrewRecipe>().iterator()
        }

        var INSTANCE: BrewRecipeManager = NO_OP
            private set

        fun register(instance: BrewRecipeManager) {
            INSTANCE = instance
        }
    }
}