package cc.mewcraft.wakame.ability

import cc.mewcraft.wakame.item.CoreCodec

/**
 * Operations of various data of abilities.
 */
sealed interface AbilityCoreCodec<
        BINARY : AbilityBinaryValue,
        SCHEME : AbilitySchemeValue,
        > : CoreCodec<BINARY, SCHEME>
