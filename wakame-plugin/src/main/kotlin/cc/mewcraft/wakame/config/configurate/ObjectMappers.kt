package cc.mewcraft.wakame.config.configurate

import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.util.NamingSchemes

/**
 * @see ObjectMapper.Factory
 */
object ObjectMappers {
    @JvmField
    val DEFAULT: ObjectMapper.Factory = ObjectMapper.factoryBuilder()
        .defaultNamingScheme(NamingSchemes.SNAKE_CASE)
        .addDiscoverer(dataClassFieldDiscoverer())
        .build()
}