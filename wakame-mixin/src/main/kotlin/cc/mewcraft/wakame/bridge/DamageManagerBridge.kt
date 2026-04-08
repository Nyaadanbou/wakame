package cc.mewcraft.wakame.bridge

import org.bukkit.event.entity.EntityDamageEvent

interface DamageManagerBridge {
    companion object Impl : DamageManagerBridge {
        private var implementation: DamageManagerBridge = object : DamageManagerBridge {
            override fun injectDamageLogic(event: EntityDamageEvent, originalLastHurt: Float, isDuringInvulnerable: Boolean): Float = Float.NaN
        }

        fun setImplementation(impl: DamageManagerBridge) {
            implementation = impl
        }

        override fun injectDamageLogic(event: EntityDamageEvent, originalLastHurt: Float, isDuringInvulnerable: Boolean): Float = implementation.injectDamageLogic(event, originalLastHurt, isDuringInvulnerable)
    }

    fun injectDamageLogic(event: EntityDamageEvent, originalLastHurt: Float, isDuringInvulnerable: Boolean): Float
}