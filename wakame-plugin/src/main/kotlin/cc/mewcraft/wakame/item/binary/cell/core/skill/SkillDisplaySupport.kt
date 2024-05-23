package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.display.DisplaySupport.DYNAMIC_LORE_META_CREATOR_REGISTRY
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@PostWorldDependency(runAfter = [RendererConfiguration::class])
@ReloadDependency(runAfter = [RendererConfiguration::class])
internal object SkillCoreInitializer : Initializable {
    override fun onPostWorld() {
        DYNAMIC_LORE_META_CREATOR_REGISTRY.register(SkillLoreMetaCreator())
    }
}

internal class SkillLoreMetaCreator : DynamicLoreMetaCreator {
    override fun test(rawLine: String): Boolean {
        return Key(rawLine).namespace() == Namespaces.SKILL
    }

    override fun create(rawIndex: RawIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return SkillLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default)
    }
}

internal object SkillDisplaySupport : KoinComponent {
    private val DISPLAY_KEY_FACTORY: SkillLineKeyFactory by inject()

    fun getLineKey(core: BinarySkillCore): FullKey {
        return DISPLAY_KEY_FACTORY.get(core)
    }
}

internal data class SkillLoreLine(
    override val key: FullKey,
    override val lines: List<Component>,
) : LoreLine

internal data class SkillLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override fun generateFullKeys(): List<FullKey> {
        return listOf(rawKey)
    }

    override fun createDefault(): List<LoreLine>? {
        if (default.isNullOrEmpty()) {
            return null
        }
        return generateFullKeys().map { key -> SkillLoreLine(key, default) }
    }
}

internal class SkillLineKeyFactory(
    private val config: RendererConfiguration,
) : LineKeyFactory<BinarySkillCore> {
    override fun get(obj: BinarySkillCore): FullKey {
        val fullKey = obj.key // 技能的 full key 就是 Core#key
        val rawKey = fullKey // 技能的 raw key 与 full key 设计上一致
        return if (rawKey !in config.rawKeys) {
            LineKeyFactory.SKIP_DISPLAY
        } else {
            fullKey
        }
    }
}