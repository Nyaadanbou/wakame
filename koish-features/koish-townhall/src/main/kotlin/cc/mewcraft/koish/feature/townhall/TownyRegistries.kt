package cc.mewcraft.koish.feature.townhall

import cc.mewcraft.koish.feature.townhall.techtree.TechTree
import cc.mewcraft.wakame.registry2.DynamicRegistries
import cc.mewcraft.wakame.registry2.WritableRegistry

object TownyRegistries {
    @JvmField
    val TECH_TREES: WritableRegistry<TechTree> = DynamicRegistries.registerSimple(TownyRegistryKeys.TECH_TREES)
}