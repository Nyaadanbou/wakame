package cc.mewcraft.wakame.serialization.configurate.mapperfactory

import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.util.NamingSchemes

/**
 * @see org.spongepowered.configurate.objectmapping.ObjectMapper.Factory
 */
object ObjectMappers {
    @JvmField
    val DEFAULT: ObjectMapper.Factory = ObjectMapper.factoryBuilder()
        .defaultNamingScheme(NamingSchemes.SNAKE_CASE)
        .addDiscoverer(dataClassFieldDiscoverer())
        .build()
}