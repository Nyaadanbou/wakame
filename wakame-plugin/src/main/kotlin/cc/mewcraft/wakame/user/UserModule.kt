package cc.mewcraft.wakame.user

import org.bukkit.entity.Player
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module

internal fun userModule(): Module = module {
    single<PlayerAdapter<Player>> { PaperPlayerAdapter() } binds arrayOf(PaperPlayerAdapter::class)
    single<UserManager<Player>> { PaperUserManager() } binds arrayOf(PaperUserManager::class)
}