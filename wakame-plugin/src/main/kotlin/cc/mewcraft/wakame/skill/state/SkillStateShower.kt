package cc.mewcraft.wakame.skill.state

import cc.mewcraft.wakame.skill.trigger.SingleTrigger
import cc.mewcraft.wakame.user.User
import me.lucko.helper.text3.mini
import org.bukkit.entity.Player

interface SkillStateShower<P : Any> {
    fun displayProgress(triggers: List<SingleTrigger>, user: User<P>)
    fun displaySuccess(triggers: List<SingleTrigger>, user: User<P>)
    fun displayFailure(triggers: List<SingleTrigger>, user: User<P>)
}

class PlayerSkillStateShower : SkillStateShower<Player> {
    override fun displayProgress(triggers: List<SingleTrigger>, user: User<Player>) {
        user.player.sendActionBar(triggers.joinToString("<gray>-") { "<green>${it.id}" }.mini)
    }

    override fun displaySuccess(triggers: List<SingleTrigger>, user: User<Player>) {
        user.player.sendActionBar(triggers.joinToString("<gray>-") { "<yellow>${it.id}" }.mini)
    }

    override fun displayFailure(triggers: List<SingleTrigger>, user: User<Player>) {
        user.player.sendActionBar(triggers.joinToString("<gray>-") { "<red>${it.id}" }.mini)
    }
}