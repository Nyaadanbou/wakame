package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.binary.binaryItemModule
import cc.mewcraft.wakame.item.schema.schemaItemModule
import org.bukkit.event.Listener
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun itemModule(): Module = module {
    includes(binaryItemModule())
    includes(schemaItemModule())

    single { ItemBehaviorListener() } bind Listener::class
}