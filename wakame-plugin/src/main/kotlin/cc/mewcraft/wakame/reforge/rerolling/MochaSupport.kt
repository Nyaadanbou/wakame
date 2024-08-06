package cc.mewcraft.wakame.reforge.rerolling

import team.unnamed.mocha.runtime.MochaFunction
import team.unnamed.mocha.runtime.compiled.Named

interface TotalCostFunction : MochaFunction {
    fun totalCost(
        @Named("base")
        base: Double,
        @Named("rarity_resolved")
        rarityResolved: Double,
        @Named("item_level")
        itemLevel: Int,
        @Named("all_count")
        allCount: Int,
        @Named("selected_count")
        selectedCount: Int,
        @Named("sum_of_each_selected")
        sumOfEachSelected: Double,
        @Named("sum_of_each_unselected")
        sumOfEachUnselected: Double,
    ): Double
}

interface EachCostFunction {
    fun eachCost(
        @Named("mod_limit")
        modLimit: Int,
        @Named("mod_count") // 从 NBT 读取
        modCount: Int,
        @Named("cost")
        cost: Double,
    ): Double
}
