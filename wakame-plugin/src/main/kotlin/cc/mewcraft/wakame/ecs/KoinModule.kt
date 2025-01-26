package cc.mewcraft.wakame.ecs

import org.koin.core.module.Module
import org.koin.dsl.module

internal fun ecsModule(): Module = module {
    single { WakameWorld } // FIXME: 为了减少 diff 这里依然声明一个 WakameWorld 实例, 合并时这里应该可以直接删除
}