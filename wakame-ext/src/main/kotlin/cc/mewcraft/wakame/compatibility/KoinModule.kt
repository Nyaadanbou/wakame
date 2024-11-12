package cc.mewcraft.wakame.compatibility

import cc.mewcraft.wakame.compatibility.mythicmobs.mythicMobsModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun compatibilityModule(): Module = module {
    includes(
        mythicMobsModule()
    )
}