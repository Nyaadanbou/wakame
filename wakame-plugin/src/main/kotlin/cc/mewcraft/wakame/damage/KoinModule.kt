package cc.mewcraft.wakame.damage

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.util.kregister
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module
import org.spongepowered.configurate.serialize.TypeSerializerCollection

internal const val DAMAGE_EXTERNAL = "damage_external"

internal fun damageModule(): Module = module {
    single { DamageListener }
    single { VanillaDamageMappings } bind Initializable::class

    // 用于外部代码
    single<TypeSerializerCollection>(named(DAMAGE_EXTERNAL)) {
        TypeSerializerCollection.builder()
            .kregister(EvaluableDamageBundleSerializer)
            .kregister(EvaluableDamagePacketSerializer)
            .build()
    }
}