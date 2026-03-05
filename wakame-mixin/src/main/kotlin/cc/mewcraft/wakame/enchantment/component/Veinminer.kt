package cc.mewcraft.wakame.enchantment.component

import cc.mewcraft.wakame.util.KoishKey
import org.bukkit.Material

class Veinminer(
    val longestMiningChain: Short,
    val allowedBlockTypes: Set<Material>,
    val blockBreakSound: KoishKey,
    val period: Long,
)