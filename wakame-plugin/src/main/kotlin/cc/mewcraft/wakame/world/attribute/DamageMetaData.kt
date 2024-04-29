package cc.mewcraft.wakame.world.attribute

interface DamageMetaData {
    val packets: List<DamagePacket>
    fun getDamage(): Double
}