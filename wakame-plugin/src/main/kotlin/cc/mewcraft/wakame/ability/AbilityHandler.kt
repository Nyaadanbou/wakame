package cc.mewcraft.wakame.ability

/**
 * Handles skill triggers for players.
 */
abstract class AbilityHandler {
    // TODO 完善 handler 架构
    abstract fun onAttack()
    abstract fun onSneak()
    abstract fun onShoot()
}