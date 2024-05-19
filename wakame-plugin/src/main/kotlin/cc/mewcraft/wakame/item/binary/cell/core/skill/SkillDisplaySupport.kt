package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.display.*
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

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
    override val fullKeys: List<FullKey> = listOf(rawKey)
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