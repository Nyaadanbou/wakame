package cc.mewcraft.wakame.catalog

import cc.mewcraft.wakame.catalog.item.ItemCatalogManager
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun catalogModule() = module {
    single { ItemCatalogManager } bind Initializable::class
}