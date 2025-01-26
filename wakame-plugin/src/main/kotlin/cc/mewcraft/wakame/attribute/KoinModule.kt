package cc.mewcraft.wakame.attribute

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun attributeModule(): Module = module {

    single<AttributeMapAccess> { DefaultAttributeMapAccess }

    single { Attributes } bind AttributeProvider::class
}