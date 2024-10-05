package cc.mewcraft.wakame.attack

/**
 * 原版弩攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: crossbow
 * ```
 */
class CrossbowShoot : AttackType {
    companion object {
        const val NAME = "crossbow"
    }
}