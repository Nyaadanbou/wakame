package cc.mewcraft.wakame.ability2.meta

interface AbilityDisplay {
    companion object {
        @JvmField
        val EMPTY: AbilityDisplay = EmptyAbilityDisplay
    }
}

private data object EmptyAbilityDisplay : AbilityDisplay