package cc.mewcraft.wakame.shadow.world.entity

import me.lucko.shadow.*
import me.lucko.shadow.Target
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.world.entity.player.Player

@ClassTarget(Player::class)
interface ShadowPlayer : Shadow {

    // protected static final EntityDataAccessor<Float> DATA_PLAYER_ABSORPTION_ID
    @get:Field
    @get:Static
    @get:Target("DATA_PLAYER_ABSORPTION_ID")
    val DATA_PLAYER_ABSORPTION_ID: EntityDataAccessor<Float>

}