package cc.mewcraft.wakame.item.components.cells.template

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.components.ElementSampleNodeFacade
import cc.mewcraft.wakame.item.components.KizamiSampleNodeFacade
import cc.mewcraft.wakame.item.components.cells.template.cores.templateCoresModule
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.io.path.Path

internal fun cellsTemplateModule(): Module = module {
    includes(
        templateCoresModule(),
    )

    // Cores ...
    single<TemplateCoreSampleNodeFacade> {
        TemplateCoreSampleNodeFacade(Path("random/items/cores"))
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