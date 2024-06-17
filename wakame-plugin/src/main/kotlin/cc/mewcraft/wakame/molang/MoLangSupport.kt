package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.util.bind
import team.unnamed.mocha.MochaEngine

object MoLangSupport {
    fun createEngine(): MochaEngine<*> {
        return MochaEngine.createStandard()
            .also {
                it.bind<LoggerQuery>()
            }
    }
}