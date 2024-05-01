package cc.mewcraft.wakame.world.attribute.damage

interface DefenseMetaData {
    val damageMetaData: DamageMetaData
    val finalDamage: Double
}

class VanillaDefenseMetaData(
    override val damageMetaData: DamageMetaData
) : DefenseMetaData {
    override val finalDamage: Double = calculateFinalDamage()

    private fun calculateFinalDamage(): Double {
        TODO("考虑原版护甲值")
    }
}

class PlayerDefenseMetaData(
    override val damageMetaData: DamageMetaData
) : DefenseMetaData {
    override val finalDamage: Double = calculateFinalDamage()

    private fun calculateFinalDamage(): Double {
        TODO()
    }
}

