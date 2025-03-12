package cc.mewcraft.wakame.element.component

import cc.mewcraft.wakame.ability.Ability
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArraySet

data class ElementStackComponent(
    val effects: Int2ObjectOpenHashMap<List<RegistryEntry<Ability>>>,
    var amount: Int = 1,
    var maxAmount: Int = 10,
    var disappearTick: Int = 100,
    val triggeredLevels: IntArraySet = IntArraySet()
) : Component<ElementStackComponent> {
    companion object : ComponentType<ElementStackComponent>()

    override fun type(): ComponentType<ElementStackComponent> = ElementStackComponent
}