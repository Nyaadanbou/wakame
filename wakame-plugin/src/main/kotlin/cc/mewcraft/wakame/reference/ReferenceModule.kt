package cc.mewcraft.wakame.reference

import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun referenceModule(): Module = module {
    singleOf(::EntityReferenceSerializer)
}