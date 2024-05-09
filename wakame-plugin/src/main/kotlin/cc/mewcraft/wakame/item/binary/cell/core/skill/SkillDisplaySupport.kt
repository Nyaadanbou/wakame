package cc.mewcraft.wakame.item.binary.cell.core.skill

import cc.mewcraft.wakame.display.*
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.text.Component

internal data class SkillLoreLine(
    override val key: FullKey,
    override val lines: List<Component>,
) : LoreLine

internal data class SkillLoreMeta(
    override val rawKey: RawKey,
    override val rawIndex: RawIndex,
    override val default: List<Component>?,
) : DynamicLoreMeta {
    override val fullKeys: List<FullKey> =
        listOf(Key(rawKey.namespace(), rawKey.value()))
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