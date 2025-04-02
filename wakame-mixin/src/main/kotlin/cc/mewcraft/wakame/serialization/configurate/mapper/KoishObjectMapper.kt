package cc.mewcraft.wakame.serialization.configurate.mapper

import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.objectmapping.FieldDiscoverer
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.objectmapping.meta.*
import org.spongepowered.configurate.util.NamingSchemes

/**
 * @see org.spongepowered.configurate.objectmapping.ObjectMapper.Factory
 */
object KoishObjectMapper {

    @JvmField
    val INSTANCE: ObjectMapper.Factory = ObjectMapper.emptyFactoryBuilder()
        .defaultNamingScheme(NamingSchemes.SNAKE_CASE)
        // Resolvers //
        .addNodeResolver(NodeResolver.nodeKey())
        .addNodeResolver(NodeResolver.keyFromSetting())
        .addNodeResolver(NodeResolver.nodeFromParent())
        // Constraints and processors //
        .addProcessor(Comment::class.java, Processor.comments())
        .addConstraint(Matches::class.java, String::class.java, Constraint.pattern())
        .addConstraint(Required::class.java, Constraint.required())
        // Post-processors //
        .addPostProcessor(PostProcessor.methodsAnnotatedPostProcess())
        // Field discovers //
        .addDiscoverer(FieldDiscoverer.emptyConstructorObject())
        .addDiscoverer(FieldDiscoverer.record())
        .addDiscoverer(dataClassFieldDiscoverer())
        .build()

}