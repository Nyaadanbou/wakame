package cc.mewcraft.wakame.display

interface TooltipProvider {
    fun provideDisplayName(): NameLine = NameLine.noop()
    fun provideDisplayLore(): LoreLine = LoreLine.noop()
}