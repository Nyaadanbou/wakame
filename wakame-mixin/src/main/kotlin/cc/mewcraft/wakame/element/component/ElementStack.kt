package cc.mewcraft.wakame.element.component

import cc.mewcraft.wakame.ability.meta.AbilityMeta
import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import com.github.quillraven.fleks.Component

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArraySet

data class ElementStack(
    var maxAmount: Int,
    var disappearTime: Int,
    val effects: Int2ObjectOpenHashMap<List<RegistryEntry<AbilityMeta>>>,
) : Component<ElementStack> {
    companion object : EComponentType<ElementStack>()

    override fun type(): EComponentType<ElementStack> = ElementStack

    var amount: Int = 1
        set(value) {
            field = (if (value > maxAmount) maxAmount else value)
        }
    val triggeredLevels: IntArraySet = IntArraySet()
}