package cc.mewcraft.wakame.ecs.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import com.github.quillraven.fleks.Component


data class Mana(
    var maximum: Int,
) : Component<Mana> {
    companion object : EComponentType<Mana>()

    override fun type(): EComponentType<Mana> = Mana

    var current: Int = maximum
        private set

    operator fun plusAssign(value: Int) = addMana(value)

    fun addMana(value: Int) {
        require(value >= 0) { "value must be non-negative" }
        val x = current + value
        if (x > maximum) return
        current = x
    }

    fun costMana(value: Int): Boolean {
        require(value >= 0) { "value must be non-negative" }
        val x = current - value
        if (x < 0) return false
        current = x
        return true
    }
}
