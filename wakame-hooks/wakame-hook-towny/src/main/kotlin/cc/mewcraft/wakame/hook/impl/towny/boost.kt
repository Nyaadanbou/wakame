package cc.mewcraft.wakame.hook.impl.towny

import cc.mewcraft.lazyconfig.access.entryOrElse
import cc.mewcraft.wakame.adventure.translator.TranslatableMessages
import cc.mewcraft.wakame.integration.townyboost.TownyBoost
import cc.mewcraft.wakame.integration.townyboost.TownyBoost.ActivateResult
import cc.mewcraft.wakame.integration.townybridgelocal.TOWNY_HOOK_CONFIG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.palmergames.bukkit.towny.TownyAPI
import com.palmergames.bukkit.towny.event.TownBlockClaimCostCalculationEvent
import com.palmergames.bukkit.towny.event.TownUpkeepCalculationEvent
import com.palmergames.bukkit.towny.event.statusscreen.TownStatusScreenEvent
import com.palmergames.bukkit.towny.`object`.metadata.CustomDataField
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*
import kotlin.math.roundToInt
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

    override fun displayFormattedValue(): String {
        val v = value
        if (v.isNullOrEmpty()) return ""

        // group → 在配置中的位置 (1-indexed), 即"等级"
        val groupToLevel = BoostConfig.boosts.keys.withIndex()
            .associate { (index, group) -> group to (index + 1) }

        // 统计每个等级的数量, 并按等级排序输出
        return v.values
            .groupingBy { it }.eachCount()
            .entries
            .mapNotNull { (group, count) ->
                val level = groupToLevel[group] ?: return@mapNotNull null
                level to count
            }
            .sortedBy { (level, _) -> level }
            .joinToString(", ") { (level, count) -> "等级$level: $count" }
    }

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
    fun get(town: TownyTown): Map<UUID, String> {
        return town.getMeta<BoostMapDataField>(KEY)?.value ?: emptyMap()
    }

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
            ?: return ActivateResult.NotInTown

        // 2. 检测玩家的最高优先级权限组及其权益
        val group = BoostConfig.boosts.keys.lastOrNull { player.hasPermission(it) }
            ?: return ActivateResult.NoVipGroup
        val boost = BoostConfig[group]
            ?: return ActivateResult.NoVipGroup

        // 3. 如果玩家已在该城镇上激活了相同等级的权益, 则无需重复激活
        val currentBoosts = TownBoostData.get(town)
        if (currentBoosts[player.uniqueId] == group) {
            return ActivateResult.AlreadyActivated(town.name)
        }

        // 4. 移除玩家在同一位面中的旧权益 (包括当前城镇上的)
        deactivate(player.uniqueId, location.world)

        // 5. 在城镇上激活新权益
        val newBoosts = TownBoostData.get(town).toMutableMap()
        newBoosts[player.uniqueId] = group

        if (boost.claimBlockBonus != 0) {
            town.addBonusBlocks(boost.claimBlockBonus)
        }

        TownBoostData.set(town, newBoosts)

        return ActivateResult.Success(town.name)
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
    fun on(event: TownStatusScreenEvent) {
        val boosts = TownBoostData.get(event.town)

        val allGroups = BoostConfig.boosts.keys.toList()
        val groupToLevel = allGroups.withIndex().associate { (index, group) -> group to (index + 1) }

        // 统计每个等级的激活数量
        val levelCounts = sortedMapOf<Int, Int>()
        for (group in allGroups) {
            levelCounts[groupToLevel[group]!!] = 0
        }
        for ((_, group) in boosts) {
            val level = groupToLevel[group] ?: continue
            levelCounts.merge(level, 1, Int::plus)
        }

        // 聚合计算最终效果
        var totalClaimBlockBonus = 0
        var claimCostProduct = 1.0
        var townUpkeepProduct = 1.0
        for ((_, group) in boosts) {
            val gb = BoostConfig[group] ?: continue
            totalClaimBlockBonus += gb.claimBlockBonus
            claimCostProduct *= gb.claimCostMultiplier
            townUpkeepProduct *= gb.townUpkeepCostMultiplier
        }

        // 构建 hover 文本
        val hoverText = Component.join(
            JoinConfiguration.newlines(),
            buildList<Component> {
                add(Component.text("城镇权益为你的城镇提供增益!", NamedTextColor.YELLOW))
                if (boosts.isEmpty()) {
                    add(Component.empty())
                    add(TranslatableMessages.MSG_TOWNY_BOOST_STATUS_NO_BOOST.build())
                }
                add(Component.empty())
                add(Component.text("每级权益数量:", NamedTextColor.AQUA))
                for ((level, count) in levelCounts) {
                    add(Component.text("  等级${romanNumeral(level)} = $count", NamedTextColor.WHITE))
                }
                add(Component.empty())
                add(Component.text("最终权益效果:", NamedTextColor.AQUA))
                add(Component.text("  额外领地上限 = +$totalClaimBlockBonus", NamedTextColor.WHITE))
                add(Component.text("  圈地价格调整 = ${formatMultiplier(claimCostProduct)}", NamedTextColor.WHITE))
                add(Component.text("  维护费调整 = ${formatMultiplier(townUpkeepProduct)}", NamedTextColor.WHITE))
            }
        )

        // 直接通过 StatusScreen#addComponentOf 添加, 使其追加到现有最后一行而非新起一行
        val label = Component.text()
            .append(Component.text("[", NamedTextColor.GRAY))
            .append(Component.text("城镇权益", NamedTextColor.GOLD))
            .append(Component.text("]", NamedTextColor.GRAY))
            .hoverEvent(HoverEvent.showText(hoverText))
            .build()
        event.statusScreen.addComponentOf("koish_boost", label)
    }

    /**
     * 将权限组名称集合映射到对应的 [GroupBoost], 并对 [selector] 指定的修饰系数连乘.
     */
    private fun Collection<String>.computeProduct(selector: (GroupBoost) -> Double): Double =
        mapNotNull { BoostConfig[it] }.fold(1.0) { acc, boost -> acc * selector(boost) }

    private fun romanNumeral(n: Int): String = when (n) {
        1 -> "I"; 2 -> "II"; 3 -> "III"; 4 -> "IV"; 5 -> "V"
        6 -> "VI"; 7 -> "VII"; 8 -> "VIII"; 9 -> "IX"; 10 -> "X"
        else -> n.toString()
    }

    private fun formatMultiplier(product: Double): String {
        val percentChange = ((product - 1.0) * 100).roundToInt() //
        return if (percentChange > 0) {
            "+$percentChange%"
        } else if (percentChange == 0) {
            "-0%"
        } else {
            "$percentChange%"
        }
    }
}