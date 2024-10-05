package cc.mewcraft.wakame.attack

/**
 * 原版弓攻击.
 *
 * ## Node structure
 * ```yaml
 * <node>:
 *   type: bow
 * ```
 */
class BowShoot : AttackType {
    companion object {
        const val NAME = "bow"
    }
}