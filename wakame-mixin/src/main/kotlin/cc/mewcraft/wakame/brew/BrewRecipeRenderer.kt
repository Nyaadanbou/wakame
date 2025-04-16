package cc.mewcraft.wakame.brew

import net.kyori.adventure.text.Component

/**
 * 该接口为渲染 [BrewRecipe] 提供抽象.
 */
interface BrewRecipeRenderer {

    /**
     * 渲染 [recipe]. 返回值可以直接用于物品堆叠组件 `minecraft:lore`.
     */
    fun render(recipe: BrewRecipe): List<Component>

    companion object {

        private val NO_OP: BrewRecipeRenderer = object : BrewRecipeRenderer {
            override fun render(recipe: BrewRecipe): List<Component> = emptyList()
        }

        var INSTANCE: BrewRecipeRenderer = NO_OP
            private set

        fun register(instance: BrewRecipeRenderer) {
            INSTANCE = instance
        }
    }
}