package cc.mewcraft.wakame.element.component

import cc.mewcraft.wakame.ecs.bridge.FleksEntity
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import com.github.quillraven.fleks.Component
import com.github.quillraven.fleks.ComponentType

data class ElementStackContainer(
    private val elementStacks: MutableMap<RegistryEntry<Element>, FleksEntity> = hashMapOf()
) : Component<ElementStackContainer> {
    companion object : ComponentType<ElementStackContainer>()

    override fun type(): ComponentType<ElementStackContainer> = ElementStackContainer

    operator fun get(element: RegistryEntry<Element>): FleksEntity? {
        return elementStacks[element]
    }

    operator fun set(element: RegistryEntry<Element>, entity: FleksEntity): Boolean {
        return elementStacks.put(element, entity) != null
    }

    operator fun contains(element: RegistryEntry<Element>): Boolean {
        return elementStacks.containsKey(element)
    }

    fun elementStacks(): Map<RegistryEntry<Element>, FleksEntity> {
        return elementStacks
    }

    fun remove(element: RegistryEntry<Element>): Boolean {
        return elementStacks.remove(element) != null
    }
}
