package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.display2.ItemRendererType

/**
 * 代表一个可以生成物品提示框文本的东西.
 */
sealed interface TooltipProvider {

    interface Single {
        /**
         * 提供单个 [LoreLine].
         */
        fun provideTooltipLore(systemName: ItemRendererType): LoreLine
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
        fun provideTooltipLore(systemName: ItemRendererType): Collection<LoreLine>
    }

    interface ClusterWithName {
        /**
         * 提供多个 [NameLine] 和 [LoreLine].
         */
        fun provideTooltipName(): Collection<Pair<NameLine, LoreLine>>
    }
}