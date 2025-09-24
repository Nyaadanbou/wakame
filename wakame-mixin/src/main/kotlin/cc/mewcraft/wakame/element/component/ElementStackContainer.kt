package cc.mewcraft.wakame.element.component

import cc.mewcraft.wakame.ecs.bridge.EComponentType
import cc.mewcraft.wakame.ecs.bridge.EEntity
import cc.mewcraft.wakame.element.Element
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import com.github.quillraven.fleks.Component

data class ElementStackContainer(
    private val elementStacks: MutableMap<RegistryEntry<Element>, EEntity> = hashMapOf(),
) : Component<ElementStackContainer> {
    companion object : EComponentType<ElementStackContainer>()

    override fun type(): EComponentType<ElementStackContainer> = ElementStackContainer

    operator fun get(element: RegistryEntry<Element>): EEntity? {
        return elementStacks[element]
    }

    operator fun set(element: RegistryEntry<Element>, entity: EEntity): Boolean {
        return elementStacks.put(element, entity) != null
    }

    operator fun contains(element: RegistryEntry<Element>): Boolean {
        return elementStacks.containsKey(element)
    }

    fun elementStacks(): Map<RegistryEntry<Element>, EEntity> {
        return elementStacks
    }

    fun remove(element: RegistryEntry<Element>): Boolean {
        return elementStacks.remove(element) != null
    }
}
