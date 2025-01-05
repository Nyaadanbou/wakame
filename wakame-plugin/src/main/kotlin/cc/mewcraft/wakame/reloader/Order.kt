package cc.mewcraft.wakame.reloader

import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.util.internalName
import xyz.xenondevs.commons.collections.mapToArray
import kotlin.reflect.KClass

enum class ReloadableOrder(
    runAfter: Set<KClass<*>> = emptySet(),
    runBefore: Set<KClass<*>> = emptySet(),
) {

    /**
     * Before the world is loaded.
     */
    NORMAL(runAfter = setOf(Configs::class)),

    ;

    internal val runAfter: Array<String> = runAfter.mapToArray { it.internalName }
    internal val runBefore: Array<String> = runBefore.mapToArray { it.internalName }

}