package item

import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationTrigger
import net.kyori.adventure.key.Key
import kotlin.random.Random

class MockGenerationContext {
    companion object {
        fun create(target: Key, trigger: GenerationTrigger): GenerationContext {
            return GenerationContext(trigger, target, Random.nextLong())
        }
    }
}