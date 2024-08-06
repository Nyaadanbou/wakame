package cc.mewcraft.wakame.item.template

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import java.util.stream.Stream

/**
 * 代表一个触发 [NekoStack] 物品生成的东西, 包含了开始物品生成所需的基本信息.
 *
 * 设计上本对象必须配合 [GenerationContext] 一起使用.
 */
interface GenerationTrigger : Examinable {
    /**
     * 源等级.
     */
    val level: Int

    companion object {
        /**
         * 获取一个空的触发器.
         *
         * 该触发器没有包含任何信息, 因此部分物品生成可能会失败.
         */
        fun nop(): GenerationTrigger {
            return NopGenerationTrigger
        }

        /**
         * Creates a trigger with everything specific.
         *
         * @param level a constant level
         * @return the created trigger
         */
        fun direct(level: Number): GenerationTrigger {
            return DirectGenerationTrigger(level.toInt())
        }

        /**
         * Wraps a user into a trigger.
         *
         * @param user the user
         * @return the created trigger
         */
        fun wrap(user: User<*>): GenerationTrigger {
            return DirectGenerationTrigger(user.level)
        }

        /**
         * Wraps a crate into a trigger.
         *
         * @param crate the crate
         * @return the created trigger
         */
        fun wrap(crate: Crate): GenerationTrigger {
            return DirectGenerationTrigger(crate.level)
        }
    }
}


/* Implementations */


private object NopGenerationTrigger : GenerationTrigger {
    override val level: Int
        get() = throw UnsupportedOperationException("NopGenerationTrigger does not have a level")

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("level", "nop")
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}

private class DirectGenerationTrigger(
    override val level: Int,
) : GenerationTrigger {
    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("level", level)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
