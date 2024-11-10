package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun attributeModule(): Module = module {
    single { DefaultAttributes } bind Initializable::class

    single { AttributeMapPatchListener() }

    single<AttributeMapAccess> { DefaultAttributeMapAccess }

    single { Attributes } bind AttributeProvider::class
}