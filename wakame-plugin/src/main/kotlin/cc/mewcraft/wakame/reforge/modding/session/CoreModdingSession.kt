package cc.mewcraft.wakame.reforge.modding.session

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.item.components.cells.Cell
import cc.mewcraft.wakame.item.components.cells.Core
import cc.mewcraft.wakame.reforge.modding.CellRule

internal class CoreModdingSession(
    override val input: NekoStack,
    override val recipes: ModdingSession.RecipeMap<Core>,
) : ModdingSession<Core> {
    override var output: NekoStack? = null
    override var frozen: Boolean = false

    override fun reforge(): ModdingSession.Result {
        TODO("Not yet implemented")
    }
}

internal class CoreModdingSessionRecipe : ModdingSession.Recipe<Core> {
    override val id: String
        get() = TODO("Not yet implemented")
    override val cell: Cell
        get() = TODO("Not yet implemented")
    override val rule: CellRule
        get() = TODO("Not yet implemented")
    override val display: ModdingSession.Recipe.Display
        get() = TODO("Not yet implemented")
    override var input: NekoStack? = null
        get() = TODO("Not yet implemented")

    override fun test(replacement: Core): ModdingSession.Recipe.TestResult {
        TODO("Not yet implemented")
    }

    override fun apply(replacement: Core): ModdingSession.Recipe.ApplyResult {
        TODO("Not yet implemented")
    }
}