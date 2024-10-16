package item

import cc.mewcraft.wakame.item.template.*
import net.kyori.adventure.key.Key
import kotlin.random.Random

class MockGenerationContext {
    companion object {
        fun create(target: Key, trigger: ItemGenerationTrigger): ItemGenerationContext {
            return ItemGenerationContexts.create(trigger, target, Random.nextLong())
        }
    }
}