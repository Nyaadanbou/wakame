package cc.mewcraft.wakame.molang

import cc.mewcraft.wakame.util.bind
import org.koin.core.module.Module
import org.koin.dsl.module
import team.unnamed.mocha.MochaEngine

internal fun moLangModule(): Module = module {
    factory {
        MochaEngine.createStandard()
            .also {
                it.bind<LoggerQuery>()
            }
    }
}