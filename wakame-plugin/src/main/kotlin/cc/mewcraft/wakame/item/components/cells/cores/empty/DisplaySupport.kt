package cc.mewcraft.wakame.item.components.cells.cores.empty

import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.derive
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.DisplaySupport
import cc.mewcraft.wakame.display.DynamicLoreMeta
import cc.mewcraft.wakame.display.DynamicLoreMetaCreator
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RawIndex
import cc.mewcraft.wakame.display.RawKey
import cc.mewcraft.wakame.display.RendererConfig
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.item.ItemComponentConstants
import cc.mewcraft.wakame.registry.ItemComponentRegistry
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

@ReloadDependency(runAfter = [RendererConfig::class])
@PostWorldDependency(runAfter = [RendererConfig::class])
internal object EmptyCoreBootstrap : Initializable {
    override fun onPostWorld() {
        DisplaySupport.DYNAMIC_LORE_META_CREATOR_REGISTRY.register(EmptyCoreLoreMetaCreator())
        DisplaySupport.LOGGER.info("Registered DynamicLoreMetaCreator for empty cores")
    }
}

internal class EmptyCoreLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = GenericKeys.EMPTY.namespace()

    override fun test(rawLine: String): Boolean {
        return Key(rawLine) == GenericKeys.EMPTY
    }

    override fun create(rawIndex: RawIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return EmptyLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default)
    }
}

internal data object EmptyLoreLine : LoreLine {
    override val key: TooltipKey = GenericKeys.EMPTY
    override val content: List<Component> by ItemComponentRegistry.CONFIG
        .derive(ItemComponentConstants.CELLS)
        .entry<Component>("tooltips", "empty")
        .map(::listOf)
}

internal data class EmptyLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override fun generateFullKeys(): List<TooltipKey> = listOf(rawKey)
    override fun createDefault(): List<LoreLine>? = null
}
