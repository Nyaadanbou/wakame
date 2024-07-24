package cc.mewcraft.wakame.display

/**
 * 代表一个可以生成物品提示框文本的东西.
 */
sealed interface TooltipProvider {

    interface Single {
        /**
         * 提供单个 [LoreLine].
         */
        fun provideTooltipLore(): LoreLine
    }

    interface SingleWithName : Single {
        /**
         * 提供单个 [NameLine].
         */
        fun provideTooltipName(): NameLine
    }

    interface Cluster {
        /**
         * 提供多个 [LoreLine].
         */
        fun provideTooltipLore(): Collection<LoreLine>
    }

    interface ClusterWithName {
        /**
         * 提供多个 [NameLine] 和 [LoreLine].
         */
        fun provideTooltipName(): Collection<Pair<NameLine, LoreLine>>
    }
}