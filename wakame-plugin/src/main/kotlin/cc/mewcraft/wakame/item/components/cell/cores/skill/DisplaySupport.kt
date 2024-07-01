package cc.mewcraft.wakame.item.components.cell.cores.skill

import cc.mewcraft.wakame.Namespaces
import cc.mewcraft.wakame.display.DisplaySupport
import cc.mewcraft.wakame.display.DynamicLoreMeta
import cc.mewcraft.wakame.display.DynamicLoreMetaCreator
import cc.mewcraft.wakame.display.LineKeyFactory
import cc.mewcraft.wakame.display.LoreLine
import cc.mewcraft.wakame.display.RawIndex
import cc.mewcraft.wakame.display.RawKey
import cc.mewcraft.wakame.display.RendererConfiguration
import cc.mewcraft.wakame.display.TooltipKey
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PostWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@ReloadDependency(runAfter = [RendererConfiguration::class])
@PostWorldDependency(runAfter = [RendererConfiguration::class])
internal object SkillCoreBootstrap : Initializable {
    override fun onPostWorld() {
        DisplaySupport.DYNAMIC_LORE_META_CREATOR_REGISTRY.register(SkillLoreMetaCreator())
        DisplaySupport.LOGGER.info("Registered DynamicLoreMetaCreator for skill cores")
    }
}

internal class SkillLoreMetaCreator : DynamicLoreMetaCreator {
    override val namespace: String = Namespaces.SKILL

    override fun test(rawLine: String): Boolean {
        return Key(rawLine).namespace() == namespace
    }

    override fun create(rawIndex: RawIndex, rawLine: String, default: List<Component>?): DynamicLoreMeta {
        return SkillLoreMeta(rawKey = Key(rawLine), rawIndex = rawIndex, default = default)
    }
}

internal object SkillDisplaySupport : KoinComponent {
    private val DISPLAY_KEY_FACTORY: SkillLineKeyFactory by inject()
    private val MINI: MiniMessage by inject()

    fun getLineKey(core: CoreSkill): TooltipKey? {
        return DISPLAY_KEY_FACTORY.get(core)
    }

    fun mini(): MiniMessage {
        return MINI
    }
}

internal data class SkillLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override fun generateFullKeys(): List<TooltipKey> {
        return listOf(rawKey)
    }

    override fun createDefault(): List<LoreLine>? {
        if (default.isNullOrEmpty()) {
            return null
        }
        return generateFullKeys().map { key -> LoreLine.simple(key, default) }
    }
}

internal class SkillLineKeyFactory(
    private val config: RendererConfiguration,
) : LineKeyFactory<CoreSkill> {
    override fun get(obj: CoreSkill): TooltipKey? {
        val fullKey = obj.key // 技能的 full key 就是 Core#key
        val rawKey = fullKey // 技能的 raw key 与 full key 设计上一致
        if (rawKey !in config.rawKeys) {
            return null
        }
        return fullKey
    }
}