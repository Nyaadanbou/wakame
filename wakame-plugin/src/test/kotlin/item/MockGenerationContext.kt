package item

import cc.mewcraft.wakame.item.NekoItem
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationTrigger
import kotlin.random.Random

class MockGenerationContext {
    companion object {
        fun create(target: NekoItem, trigger: GenerationTrigger): GenerationContext {
            return GenerationContext(trigger, target.key, Random.nextLong())
        }
    }
}