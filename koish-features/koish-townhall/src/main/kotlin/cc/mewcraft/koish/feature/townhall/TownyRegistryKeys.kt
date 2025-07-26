package cc.mewcraft.koish.feature.townhall

import cc.mewcraft.koish.feature.townhall.techtree.TechTree
import cc.mewcraft.wakame.registry2.DynamicRegistryKeys

object TownyRegistryKeys {
    @JvmField
    val TECH_TREES = DynamicRegistryKeys.createRegistryKey<TechTree>("tech_trees")
}