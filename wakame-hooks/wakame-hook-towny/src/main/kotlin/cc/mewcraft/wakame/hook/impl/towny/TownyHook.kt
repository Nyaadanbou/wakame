package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.access.ConfigAccess
import cc.mewcraft.wakame.api.protection.ProtectionIntegration
import cc.mewcraft.wakame.api.protection.ProtectionIntegration.ExecutionMode
import cc.mewcraft.wakame.integration.Hook
import cc.mewcraft.wakame.integration.townyboost.TownyBoost
import cc.mewcraft.wakame.integration.townybridgelocal.TownyLocalBridge
import cc.mewcraft.wakame.integration.townybridgenetwork.TownyNetworkBridge
import cc.mewcraft.wakame.messaging.handler.TownyBridgeNetworkPacketHandler
import cc.mewcraft.wakame.util.KOISH_NAMESPACE
import cc.mewcraft.wakame.util.registerEvents
import com.palmergames.bukkit.towny.`object`.metadata.MetadataLoader

@Hook(plugins = ["Towny"])
object TownyHook : ProtectionIntegration by TownyProtectionIntegration {

    // Java interop fix - start
    override fun getExecutionMode(): ExecutionMode {
        return super.getExecutionMode()
    }
    // Java interop fix - end

    init {
        // 只要反序列化发生的时机晚于该函数调用就可以正常反序列化
        ConfigAccess.registerSerializer(KOISH_NAMESPACE, EntryFilter.serializer())

        TownyLocalBridge.setImplementation(TownyLocalBridgeImpl())

        with(TownyNetworkBridgeImpl) {
            registerEvents()
            TownyNetworkBridge.setImplementation(this)
            TownyBridgeNetworkPacketHandler.setImplementation(this)
        }

        // 注册自定义 metadata 类型的反序列化器, 使 Towny 能从磁盘恢复数据
        MetadataLoader.getInstance().registerDeserializer(BoostMapDataField.TYPE_ID) { key, value ->
            BoostMapDataField(key).also { if (value != null) it.setValueFromString(value) }
        }

        TownyBoost.setImplementation(TownyBoostImpl())
        TownyBoostListener().registerEvents()
    }
}