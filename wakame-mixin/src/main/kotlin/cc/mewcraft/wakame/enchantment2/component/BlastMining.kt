package cc.mewcraft.wakame.enchantment2.component

import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

class BlastMining(
    val explodeLevel: Int,
) : Component<BlastMining> {

    companion object : ComponentType<BlastMining>()

    override fun type() = BlastMining

}