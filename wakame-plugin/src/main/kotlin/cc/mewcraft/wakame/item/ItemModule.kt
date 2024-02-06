package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.binary.binaryItemModule
import cc.mewcraft.wakame.item.scheme.schemeItemModule
import org.koin.core.module.Module
import org.koin.dsl.module

fun itemModule(): Module = module {
    includes(binaryItemModule())
    includes(schemeItemModule())
}