package cc.mewcraft.wakame.item.templates.components.cells

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.templates.components.ElementSampleNodeFacade
import cc.mewcraft.wakame.item.templates.components.KizamiSampleNodeFacade
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.io.path.Path

internal fun cellsModule(): Module = module {
    // Cores ...
    single<CoreArchetypeSampleNodeFacade> {
        CoreArchetypeSampleNodeFacade(Path("random/items/cores"))
    } bind Initializable::class

    // Elements ...
    single<ElementSampleNodeFacade> {
        ElementSampleNodeFacade(Path("random/items/elements"))
    } bind Initializable::class

    // Kizamiz ...
    single<KizamiSampleNodeFacade> {
        KizamiSampleNodeFacade(Path("random/items/kizamiz"))
    } bind Initializable::class
}