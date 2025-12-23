package cc.mewcraft.bettergui

import cc.mewcraft.bettergui.action.OpenCatalog
import me.hsgamer.bettergui.builder.ActionBuilder
import me.hsgamer.hscore.common.Validate
import me.hsgamer.hscore.expansion.common.Expansion

class KoishBridge : Expansion {

    override fun onLoad(): Boolean {
        return Validate.isClassLoaded("cc.mewcraft.wakame.api.Koish")
    }

    override fun onEnable() {
        ActionBuilder.INSTANCE.register(::OpenCatalog, "open-catalog")
    }
}