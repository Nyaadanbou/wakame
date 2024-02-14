package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.registry.AttributeRegistry
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun attributeModule(): Module = module {

    single { AttributeRegistry } bind Initializable::class

}