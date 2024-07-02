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

    interface Cluster {
        /**
         * 提供多个 [LoreLine].
         */
        fun provideTooltipLore(): Collection<LoreLine>
    }

    @Deprecated("")
    fun provideDisplayName(): NameLine = NameLine.noop()

    @Deprecated("")
    fun provideDisplayLore(): LoreLine = LoreLine.noop()
}