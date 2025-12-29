package cc.mewcraft.bettergui

import cc.mewcraft.bettergui.action.GiveBalance
import cc.mewcraft.bettergui.action.OpenCatalog
import cc.mewcraft.bettergui.requirement.AccBalanceReq
import cc.mewcraft.bettergui.requirement.BalanceReq
import me.hsgamer.bettergui.builder.ActionBuilder
import me.hsgamer.bettergui.builder.RequirementBuilder
import me.hsgamer.hscore.common.Validate
import me.hsgamer.hscore.expansion.common.Expansion

class KoishBridge : Expansion {

    override fun onLoad(): Boolean {
        return Validate.isClassLoaded("cc.mewcraft.wakame.api.Koish")
    }

    override fun onEnable() {
        ActionBuilder.INSTANCE.register(::GiveBalance, "give-balance")
        ActionBuilder.INSTANCE.register(::OpenCatalog, "open-catalog")
        RequirementBuilder.INSTANCE.register(::BalanceReq, "balance")
        RequirementBuilder.INSTANCE.register(::AccBalanceReq, "acc-balance")
    }
}