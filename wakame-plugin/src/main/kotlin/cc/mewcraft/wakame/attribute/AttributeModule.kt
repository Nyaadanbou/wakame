package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.base.AttributeHandler
import cc.mewcraft.wakame.attribute.base.PlayerAttributeAccessor
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

internal fun attributeModule(): Module = module {
    singleOf(::AttributeHandler)

    singleOf(::PlayerAttributeAccessor)
}