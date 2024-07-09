package cc.mewcraft.wakame.skill.context

import cc.mewcraft.wakame.item.NekoStack
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Target
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toSimpleString
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import team.unnamed.mocha.MochaEngine
import java.util.stream.Stream

class SkillContextKey<T : Any>
private constructor(
    private val key: String,
) : Examinable {

    /**
     * 所有可用的 [SkillContextKey] 实例.
     */
    companion object {
        val CASTER: SkillContextKey<Caster.CompositeNode> = SkillContextKey("caster")
        val ITEM_STACK: SkillContextKey<ItemStack> = SkillContextKey("item_stack")
        val MOCHA_ENGINE: SkillContextKey<MochaEngine<*>> = SkillContextKey("mocha_engine")
        val NEKO_STACK: SkillContextKey<NekoStack> = SkillContextKey("neko_stack")
        val TARGET: SkillContextKey<Target> = SkillContextKey("target")
        val USER: SkillContextKey<User<Player>> = SkillContextKey("user")
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
