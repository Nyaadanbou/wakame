package cc.mewcraft.wakame.elementstack.component

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.element.ElementType
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementStackContainer(
    private val elementStacks: MutableMap<RegistryEntry<ElementType>, FleksEntity> = hashMapOf()
) : Component<ElementStackContainer> {
    companion object : ComponentType<ElementStackContainer>()

    override fun type(): ComponentType<ElementStackContainer> = ElementStackContainer

    operator fun get(element: RegistryEntry<ElementType>): FleksEntity? {
        return elementStacks[element]
    }

    operator fun set(element: RegistryEntry<ElementType>, entity: FleksEntity): Boolean {
        return elementStacks.put(element, entity) != null
    }

    operator fun contains(element: RegistryEntry<ElementType>): Boolean {
        return elementStacks.containsKey(element)
    }

    fun remove(element: RegistryEntry<ElementType>): Boolean {
        return elementStacks.remove(element) != null
    }
}
