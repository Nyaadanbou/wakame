@file:OptIn(InternalApi::class)

package cc.mewcraft.wakame.entity

import cc.mewcraft.wakame.WakamePlugin
import cc.mewcraft.wakame.annotation.InternalApi
import org.koin.core.module.Module
import org.koin.dsl.module

fun entityModule(): Module = module {

    single<EntityKeyLookup> {
        val lookupList = mutableListOf<EntityKeyLookup>()

        ////// start constructing the lookup list

        // optionally add MM lookup
        val pl = get<WakamePlugin>()
        if (pl.isPluginPresent("MythicMobs")) {
            lookupList += MythicMobsEntityKeyLookup()
        }

        // always add vanilla lookup
        lookupList += VanillaEntityKeyLookup()

        ////// return the final instance
        CompositedEntityKeyLookup(lookupList)
    }

}