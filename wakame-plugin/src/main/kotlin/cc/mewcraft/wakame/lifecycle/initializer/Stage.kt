package cc.mewcraft.wakame.lifecycle.initializer

import cc.mewcraft.wakame.ecs.KoishFleks
import cc.mewcraft.wakame.util.internalName
import xyz.xenondevs.commons.collections.mapToArray
import kotlin.reflect.KClass

/**
 * Defines the stage at which something should be initialized.
 */
internal enum class InternalInitStage(val isPreWorld: Boolean) {

    /**
     * Bootstrap stage as specified in Paper API.
     */
    BOOTSTRAP(true),

    /**
     * Before the world is loaded and after bootstrap.
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
     * Bootstrap stage as specified in Paper API.
     */
    BOOTSTRAP(InternalInitStage.BOOTSTRAP),

    /**
     * Before the world is loaded and after bootstrap.
     */
    PRE_WORLD(InternalInitStage.PRE_WORLD),

    /**
     * After the world has been loaded.
     */
    POST_WORLD(InternalInitStage.POST_WORLD),

    /**
     * 在 Fleks 加载之前, 用于使用 [cc.mewcraft.wakame.ecs.SystemBootstrapper] 添加系统.
     */
    PRE_FLEKS(InternalInitStage.POST_WORLD, runBefore = setOf(KoishFleks::class));

    internal val runAfter: Array<String> = runAfter.mapToArray { it.internalName }
    internal val runBefore: Array<String> = runBefore.mapToArray { it.internalName }
}