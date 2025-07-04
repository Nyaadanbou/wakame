package cc.mewcraft.wakame.lifecycle.initializer

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.ecs.KoishFleks
import cc.mewcraft.wakame.util.internalName
import xyz.xenondevs.commons.collections.mapToArray
import kotlin.reflect.KClass

/**
 * Defines the stage at which something should be initialized.
 */
internal enum class InternalInitStage(val isPreWorld: Boolean) {

    /**
     * Before the world is loaded.
     */
    PRE_WORLD(true),

    /**
     * After the world is loaded.
     */
    POST_WORLD(false),

}

/**
 * Defines the stage at which something should be initialized.
 *
 * @param internalStage The internal [InternalInitStage] to use.
 * @param runAfter The default set of initializables that should this one should be initialized after.
 * @param runBefore The default set of initializables that should this one should be initialized before.
 */
enum class InitStage(
    internal val internalStage: InternalInitStage,
    runAfter: Set<KClass<*>> = emptySet(),
    runBefore: Set<KClass<*>> = emptySet(),
) {

    /**
     * Before configs are initialized.
     * Can be used to register custom config serializers via [Configs.registerSerializer].
     */
    PRE_CONFIG(InternalInitStage.PRE_WORLD, runBefore = setOf(Configs::class)),

    /**
     * Before the world is loaded.
     */
    PRE_WORLD(InternalInitStage.PRE_WORLD, runAfter = setOf(Configs::class)),

    /**
     * After the world has been loaded.
     */
    POST_WORLD(InternalInitStage.POST_WORLD),

    /**
     * Before Fleks is initialized.
     */
    PRE_FLEKS(InternalInitStage.POST_WORLD, runBefore = setOf(KoishFleks::class));

    internal val runAfter: Array<String> = runAfter.mapToArray { it.internalName }
    internal val runBefore: Array<String> = runBefore.mapToArray { it.internalName }

}