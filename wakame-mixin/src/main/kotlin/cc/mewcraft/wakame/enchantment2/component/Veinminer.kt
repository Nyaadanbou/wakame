package cc.mewcraft.wakame.enchantment2.component

import cc.mewcraft.wakame.util.Identifier
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import org.bukkit.Material

class Veinminer(
    val longestMiningChain: Short,
    val allowedBlockTypes: Set<Material>,
    val blockBreakSound: Identifier,
) : Component<Veinminer> {

    companion object : ComponentType<Veinminer>()

    override fun type() = Veinminer

}