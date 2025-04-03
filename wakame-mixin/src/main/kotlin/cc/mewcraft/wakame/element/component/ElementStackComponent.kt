package cc.mewcraft.wakame.element.component

import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArraySet

data class ElementStackComponent(
    var maxAmount: Int,
    var disappearTime: Int,
    val effects: Int2ObjectOpenHashMap<List<RegistryEntry<AbilityMeta>>>,
) : Component<ElementStackComponent> {
    companion object : ComponentType<ElementStackComponent>()

    override fun type(): ComponentType<ElementStackComponent> = ElementStackComponent

    var amount: Int = 1
        set(value) {
            field = (if (value > maxAmount) maxAmount else value)
        }
    val triggeredLevels: IntArraySet = IntArraySet()
}