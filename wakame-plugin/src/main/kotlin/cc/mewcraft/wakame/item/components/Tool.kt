package cc.mewcraft.wakame.item.components

import net.kyori.adventure.util.TriState
import net.kyori.examination.Examinable
import org.bukkit.block.BlockType

interface Tool : Examinable {
    val rules: List<Rule>
    val defaultMiningSpeed: Float
    val damagePerBlock: Int

    data class Rule(
        val blockTypes: Set<BlockType>,
        val speed: Float,
        val correctForDrops: TriState,
    )
}
