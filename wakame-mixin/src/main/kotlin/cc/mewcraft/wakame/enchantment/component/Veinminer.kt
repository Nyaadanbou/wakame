package cc.mewcraft.wakame.enchantment.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.util.KoishKey
import com.github.quillraven.fleks.Component
import org.bukkit.Material

class Veinminer(
    val longestMiningChain: Short,
    val allowedBlockTypes: Set<Material>,
    val blockBreakSound: KoishKey,
) : Component<Veinminer> {

    companion object : EComponentType<Veinminer>()

    override fun type() = Veinminer

}