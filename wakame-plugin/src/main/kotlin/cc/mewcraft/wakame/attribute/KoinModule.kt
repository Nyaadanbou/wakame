package cc.mewcraft.wakame.attribute

import cc.mewcraft.wakame.initializer.Initializable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.koin.core.module.Module
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.bind
import org.koin.dsl.module

internal fun attributeModule(): Module = module {
    single { DefaultAttributes } bind Initializable::class

    single { AttributeMapPatchListener() }

    single<AttributeMapAccess> { AttributeMapAccessImpl }

    single { Attributes } bind AttributeProvider::class
}