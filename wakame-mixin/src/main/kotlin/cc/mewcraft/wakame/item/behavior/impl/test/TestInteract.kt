package cc.mewcraft.wakame.item.behavior.impl.test

import cc.mewcraft.wakame.item.behavior.AttackContext
import cc.mewcraft.wakame.item.behavior.AttackEntityContext
import cc.mewcraft.wakame.item.behavior.AttackOnContext
import cc.mewcraft.wakame.item.behavior.InteractionResult
import cc.mewcraft.wakame.item.behavior.ItemBehavior
import cc.mewcraft.wakame.item.behavior.UseContext
import cc.mewcraft.wakame.item.behavior.UseEntityContext
import cc.mewcraft.wakame.item.behavior.UseOnContext
import cc.mewcraft.wakame.util.text.mini
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit

object TestInteract : ItemBehavior {
    override fun handleUseOn(context: UseOnContext): InteractionResult {
        // 此次交互触发了方块交互 - 交互失败
        if (context.isTriggerBlockInteract) return InteractionResult.FAIL

        context.player.sendMessage(
            "你对 <light_purple>方块</light_purple> 进行了 <gold>使用</gold> 交互".mini
                .hoverEvent(
                    text("Tick: ${Bukkit.getCurrentTick()}")
                        .appendNewline()
                        .append(text("主副手: ${context.hand}"))
                        .appendNewline()
                        .append(text("方块坐标: ${context.interactContext.blockPosition}"))
                        .appendNewline()
                        .append(text("交互点: ${context.interactContext.interactPoint}"))
                        .appendNewline()
                        .append(text("交互面: ${context.interactContext.interactFace}"))
                )
        )
        return InteractionResult.SUCCESS
    }

    override fun handleUse(context: UseContext): InteractionResult {
        context.player.sendMessage(
            "你对 <aqua>空气</aqua> 进行了 <gold>使用</gold> 交互".mini
                .hoverEvent(
                    text("Tick: ${Bukkit.getCurrentTick()}")
                        .appendNewline()
                        .append(text("主副手: ${context.hand}"))
                )
        )
        return InteractionResult.SUCCESS
    }

    override fun handleUseEntity(context: UseEntityContext): InteractionResult {
        // 此次交互触发了实体交互 - 交互失败
        if (context.isTriggerEntityInteract) return InteractionResult.FAIL

        context.player.sendMessage(
            "你对 <green>实体</green> 进行了 <gold>使用</gold> 交互".mini
                .hoverEvent(
                    text("Tick: ${Bukkit.getCurrentTick()}")
                        .appendNewline()
                        .append(text("主副手: ${context.hand}"))
                        .appendNewline()
                        .append(text("目标实体类型: ${context.entity.type}"))
                        .appendNewline()
                        .append(text("目标实体UUID: ${context.entity.uniqueId}"))
                )
        )
        return InteractionResult.SUCCESS
    }

    override fun handleAttackOn(context: AttackOnContext): InteractionResult {
        context.player.sendMessage(
            "你对 <light_purple>方块</light_purple> 进行了 <red>攻击</red> 交互".mini
                .hoverEvent(
                    text("Tick: ${Bukkit.getCurrentTick()}")
                        .appendNewline()
                        .append(text("方块坐标: ${context.blockPosition}"))
                        .appendNewline()
                        .append(text("交互面: ${context.interactFace}"))
                )
        )
        return InteractionResult.SUCCESS
    }

    override fun handleAttack(context: AttackContext): InteractionResult {
        context.player.sendMessage(
            "你对 <aqua>空气</aqua> 进行了 <red>攻击</red> 交互".mini
                .hoverEvent(
                    text("Tick: ${Bukkit.getCurrentTick()}")
                )
        )
        return InteractionResult.SUCCESS
    }

    override fun handleAttackEntity(context: AttackEntityContext): InteractionResult {
        context.player.sendMessage(
            "你对 <green>实体</green> 进行了 <red>攻击</red> 交互".mini
                .hoverEvent(
                    text("Tick: ${Bukkit.getCurrentTick()}")
                        .appendNewline()
                        .append(text("目标实体类型: ${context.entity.type}"))
                        .appendNewline()
                        .append(text("目标实体UUID: ${context.entity.uniqueId}"))
                )
        )
        return InteractionResult.SUCCESS
    }
}