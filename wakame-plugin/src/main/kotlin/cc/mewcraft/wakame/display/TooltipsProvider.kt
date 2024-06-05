package cc.mewcraft.wakame.display

interface TooltipsProvider {
    fun provideDisplayName(): NameLine = NameLine.noop()
    fun provideDisplayLore(): LoreLine = LoreLine.noop()
}