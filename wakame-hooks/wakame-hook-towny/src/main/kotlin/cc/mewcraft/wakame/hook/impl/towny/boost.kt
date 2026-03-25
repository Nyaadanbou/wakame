package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.integration.towny.TOWNY_HOOK_CONFIG
import cc.mewcraft.wakame.integration.towny.TownyBoost
import cc.mewcraft.wakame.integration.towny.TownyBoost.ActivateResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.event.NationUpkeepCalculationEvent
import com.palmergames.bukkit.towny.event.TownBlockClaimCostCalculationEvent
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent
import com.palmergames.bukkit.towny.`object`.metadata.CustomDataField
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*
import com.palmergames.bukkit.towny.`object`.Town as TownyTown

// ============================================================
// GroupBoost
// ============================================================

/**
 * 单个权限组的城镇权益.
 *
 * 未配置的权益字段使用恒等值 (加法: 0, 乘法: 1.0), 应用时等效于 NO_OP.
 */
@ConfigSerializable
data class GroupBoost(
    val claimBlockBonus: Int = 0,
    val claimCostMultiplier: Double = 1.0,
    val townUpkeepCostMultiplier: Double = 1.0,
    val nationUpkeepCostMultiplier: Double = 1.0,
)

// ============================================================
// BoostMapDataField
// ============================================================

/**
 * 储存 `Map<UUID, String>` 的 Towny 自定义 metadata 类型.
 *
 * 数据以 JSON 格式序列化: `{"<uuid>": "<group>", ...}`.
 */
class BoostMapDataField : CustomDataField<Map<UUID, String>> {
    constructor(key: String) : super(key)
    constructor(key: String, value: Map<UUID, String>) : super(key, value)

    override fun getTypeID(): String = TYPE_ID

    override fun setValueFromString(strValue: String) {
        val raw: Map<String, String> = gson.fromJson(strValue, MAP_TYPE)
        setValue(raw.mapKeys { (k, _) -> UUID.fromString(k) })
    }

    override fun serializeValueToString(): String? {
        val v = value ?: return null
        val raw = v.mapKeys { (k, _) -> k.toString() }
        return gson.toJson(raw)
    }

    override fun displayFormattedValue(): String = value?.toString() ?: "{}"

    override fun clone(): CustomDataField<Map<UUID, String>> =
        BoostMapDataField(key, value ?: emptyMap())

    companion object {
        const val TYPE_ID = "koish_boost_map"
        private val gson = Gson()
        private val MAP_TYPE = object : TypeToken<Map<String, String>>() {}.type
    }
}

// ============================================================
// BoostConfig
// ============================================================

/**
 * 城镇权益配置.
 *
 * 配置结构: `boost.<权限组>.<权益类型>`.
 * 在配置文件中越靠后的权限组优先级越高.
 */
object BoostConfig {
    /** 权限组 → 权益映射 (按配置文件顺序, 越靠后优先级越高). */
    val boosts: Map<String, GroupBoost> by TOWNY_HOOK_CONFIG.entryOrElse(emptyMap(), "boost")

    /** 获取指定权限组的权益. */
    operator fun get(group: String): GroupBoost? = boosts[group]
}

// ============================================================
// TownBoostData
// ============================================================

/**
 * 城镇权益数据, 储存在 [Town][TownyTown] 的 [BoostMapDataField] metadata 中.
 */
object TownBoostData {
    private const val KEY = "koish_boosts"

    /** 读取城镇上所有已激活的权益. */
    fun get(town: TownyTown): Map<UUID, String> =
        town.getMeta<BoostMapDataField>(KEY)?.value ?: emptyMap()

    /** 写入城镇上所有已激活的权益, 并保存. */
    fun set(town: TownyTown, boosts: Map<UUID, String>) {
        if (boosts.isEmpty()) {
            town.removeMetaData(KEY)
        } else {
            town.addMetaData(BoostMapDataField(KEY, boosts), true)
        }
        town.save()
    }
}

// ============================================================
// TownyBoostImpl
// ============================================================

/**
 * [TownyBoost] 的实现.
 */
class TownyBoostImpl : TownyBoost {

    override fun activate(player: Player): ActivateResult {
        val townyApi = TownyAPI.getInstance()
        val location = player.location

        // 1. 获取玩家所在位置的城镇
        val town = townyApi.getTown(location)
            ?: return ActivateResult.NOT_IN_TOWN

        // 2. 检测玩家的最高优先级权限组及其权益
        val group = BoostConfig.boosts.keys.lastOrNull { player.hasPermission(it) }
            ?: return ActivateResult.NO_VIP_GROUP
        val boost = BoostConfig[group]
            ?: return ActivateResult.NO_VIP_GROUP

        // 3. 移除玩家在同一位面中旧城镇上的权益
        val townyWorld = townyApi.getTownyWorld(location.world)
        if (townyWorld != null) {
            for (existingTown in townyWorld.towns.values) {
                if (existingTown == town) continue
                val boosts = TownBoostData.get(existingTown)
                val oldGroup = boosts[player.uniqueId] ?: continue
                val oldBonus = BoostConfig[oldGroup]?.claimBlockBonus ?: 0
                if (oldBonus != 0) existingTown.addBonusBlocks(-oldBonus)
                TownBoostData.set(existingTown, boosts - player.uniqueId)
                break // 每个位面最多只有一个, 找到就停
            }
        }

        // 4. 在新城镇上激活权益
        val currentBoosts = TownBoostData.get(town).toMutableMap()
        val previousGroup = currentBoosts.put(player.uniqueId, group)

        // 处理 bonus blocks: 减去旧值, 加上新值
        val oldBonus = previousGroup?.let { BoostConfig[it]?.claimBlockBonus } ?: 0
        if (boost.claimBlockBonus - oldBonus != 0) {
            town.addBonusBlocks(boost.claimBlockBonus - oldBonus)
        }

        // 5. 保存
        TownBoostData.set(town, currentBoosts)

        return ActivateResult.SUCCESS
    }

    override fun deactivate(playerId: UUID, world: World): Boolean {
        val townyWorld = TownyAPI.getInstance().getTownyWorld(world) ?: return false

        for (town in townyWorld.towns.values) {
            val boosts = TownBoostData.get(town)
            val group = boosts[playerId] ?: continue

            val bonus = BoostConfig[group]?.claimBlockBonus ?: 0
            if (bonus != 0) town.addBonusBlocks(-bonus)

            TownBoostData.set(town, boosts - playerId)
            return true
        }
        return false
    }
}

// ============================================================
// TownyBoostListener
// ============================================================

/**
 * 监听 Towny 费用计算事件, 根据城镇上已激活的权益调整费用.
 *
 * - [TownBlockClaimCostCalculationEvent] — 圈地花费: 所有权益的修饰系数相乘
 * - [TownUpkeepCalculationEvent] — 城镇维护费: 所有权益的修饰系数相乘
 * - [NationUpkeepCalculationEvent] — 国家维护费: 国家下所有城镇的权益修饰系数相乘
 */
class TownyBoostListener : Listener {

    @EventHandler
    fun on(event: TownBlockClaimCostCalculationEvent) {
        val multiplier = TownBoostData.get(event.town).values
            .computeProduct { it.claimCostMultiplier }
        if (multiplier != 1.0) {
            event.price *= multiplier
        }
    }

    @EventHandler
    fun on(event: TownUpkeepCalculationEvent) {
        val multiplier = TownBoostData.get(event.town).values
            .computeProduct { it.townUpkeepCostMultiplier }
        if (multiplier != 1.0) {
            event.upkeep *= multiplier
        }
    }

    @EventHandler
    fun on(event: NationUpkeepCalculationEvent) {
        val multiplier = event.nation.towns
            .flatMap { TownBoostData.get(it).values }
            .computeProduct { it.nationUpkeepCostMultiplier }
        if (multiplier != 1.0) {
            event.upkeep *= multiplier
        }
    }

    /**
     * 将权限组名称集合映射到对应的 [GroupBoost], 并对 [selector] 指定的修饰系数连乘.
     */
    private fun Collection<String>.computeProduct(selector: (GroupBoost) -> Double): Double =
        mapNotNull { BoostConfig[it] }.fold(1.0) { acc, boost -> acc * selector(boost) }
}