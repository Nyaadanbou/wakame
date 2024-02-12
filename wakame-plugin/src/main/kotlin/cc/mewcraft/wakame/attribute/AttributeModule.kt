package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.attribute.facade.AttributeFacadeRegistry
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

fun attributeModule(): Module = module {

    single { AttributeFacadeRegistry } bind Initializable::class

}