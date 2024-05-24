package cc.mewcraft.wakame.item.binary.cell.core.empty

import cc.mewcraft.wakame.GenericKeys
import cc.mewcraft.wakame.config.Configs
import cc.mewcraft.wakame.config.entry
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.registry.ITEM_CONFIG_FILE
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

@ReloadDependency(runAfter = [RendererConfiguration::class])
@PostWorldDependency(runAfter = [RendererConfiguration::class])
internal object EmptyCoreInitializer : Initializable {
    override fun onPostWorld() {
        DisplaySupport.DYNAMIC_LORE_META_CREATOR_REGISTRY.register(EmptyCoreLoreMetaCreator())
        DisplaySupport.LOGGER.info("Registered DynamicLoreMetaCreator for empty cores")
    }
}

internal class EmptyCoreLoreMetaCreator : DynamicLoreMetaCreator {
    override fun test(rawLine: String): Boolean {
        return Key(rawLine) == GenericKeys.EMPTY
    }

    override fun create(rawIndex: RawIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return EmptyLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default)
    }
}

internal data object EmptyLoreLine : LoreLine {
    override val key: FullKey = GenericKeys.EMPTY
    override val lines: List<Component> by Configs.YAML[ITEM_CONFIG_FILE].entry<List<Component>>("general", "empty_cell_tooltips")
}

internal data class EmptyLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override fun generateFullKeys(): List<FullKey> = listOf(rawKey)
    override fun createDefault(): List<LoreLine>? = null
}
