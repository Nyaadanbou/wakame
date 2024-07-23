package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.WakameInjections
import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.koin.core.component.get
import org.slf4j.Logger
import team.unnamed.mocha.MochaEngine
import java.util.stream.Stream

class SkillContextKey<T : Any> private constructor(
    private val key: String,
) : Examinable {

    /**
     * 所有可用的 [SkillContextKey] 实例.
     */
    companion object {
        private val INSTANCES: MutableSet<SkillContextKey<*>> = ReferenceOpenHashSet()

        fun <T : Any> create(key: String): SkillContextKey<T> {
            val instance = SkillContextKey<T>(key)
            if (!INSTANCES.add(instance)) {
                WakameInjections.get<Logger>().warn("重复的 SkillContextKey 实例: $instance")
            }
            return instance
        }

        val CASTER: SkillContextKey<Caster.Composite> = create("caster")
        val ITEM_STACK: SkillContextKey<ItemStack> = create("item_stack")
        val MOCHA_ENGINE: SkillContextKey<MochaEngine<*>> = create("mocha_engine")
        val NEKO_STACK: SkillContextKey<NekoStack> = create("neko_stack")
        val TARGET: SkillContextKey<Target> = create("target")
        val USER: SkillContextKey<User<Player>> = create("user")
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is SkillContextKey<*> && other.key == key
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> {
        return Stream.of(
            ExaminableProperty.of("key", key)
        )
    }

    override fun toString(): String {
        return toSimpleString()
    }
}
