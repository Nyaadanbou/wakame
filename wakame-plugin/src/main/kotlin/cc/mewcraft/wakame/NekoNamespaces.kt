package cc.mewcraft.wakame

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.attribute.base.Attribute
import cc.mewcraft.wakame.item.Core
import cc.mewcraft.wakame.item.Curse
import cc.mewcraft.wakame.item.binary.stats.ItemStats
import cc.mewcraft.wakame.item.scheme.meta.SchemeMeta

object NekoNamespaces {
    /**
     * The namespace of all [abilities][Ability] in [cores][Core].
     */
    const val ABILITY = "ability"

    /**
     * The namespace of all [attributes][Attribute] in [cores][Core].
     */
    const val ATTRIBUTE = "attribute"

    /**
     * The namespace of all types of [SchemeMeta] in configuration.
     */
    const val META = "meta"

    /**
     * The namespace of all types of [Curse].
     */
    const val CURSE = "curse"

    /**
     * The namespace of all types of [ItemStats].
     */
    const val STATS = "stats"
}