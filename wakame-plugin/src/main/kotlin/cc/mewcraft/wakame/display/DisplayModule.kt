package cc.mewcraft.wakame.display

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

const val RENDERER_SERIALIZERS = "renderer_serializers"

fun displayModule(): Module = module {

    // non-internals
    singleOf(::ItemRendererImpl) bind ItemRenderer::class
    singleOf(::ItemRendererListener)

    // line index supplier
    singleOf(::LineIndexSupplierImpl) bind LineIndexSupplier::class
    // line key suppliers
    singleOf(::AbilityLineKeySupplierImpl) bind AbilityLineKeySupplier::class
    singleOf(::AttributeLineKeySupplierImpl) bind AttributeLineKeySupplier::class
    singleOf(::MetaLineKeySupplierImpl) bind MetaLineKeySupplier::class

    // finalizer
    singleOf(::LoreLineFinalizerImpl) bind LoreLineFinalizer::class

    // stylizers
    singleOf(::LoreStylizerImpl) bind LoreStylizer::class
    singleOf(::AbilityStylizerImpl) bind AbilityStylizer::class
    singleOf(::AttributeStylizerImpl) bind AttributeStylizer::class
    singleOf(::OperationStylizerImpl) bind OperationStylizer::class // TODO construct the map
    singleOf(::MetaStylizerImpl) bind MetaStylizer::class
    singleOf(::NameStylizerImpl) bind NameStylizer::class

}