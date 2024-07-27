package cc.mewcraft.wakame.reforge.modding

import cc.mewcraft.commons.collections.associateNotNull
import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.rarity.RaritySerializer
import cc.mewcraft.wakame.skill.trigger.SkillTriggerSerializer
import cc.mewcraft.wakame.util.NamespacedPathCollector
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import cc.mewcraft.wakame.util.yamlConfig
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.kotlin.extensions.get
import org.spongepowered.configurate.kotlin.extensions.getList
import java.io.File
import java.lang.reflect.Type

/**
 * [ModdingTable] 的序列化器.
 */
internal object ModdingTableSerializer : KoinComponent {
    private const val REFORGE_DIR_NAME = "reforge"
    private const val MODDING_DIR_NAME = "modding"

    private val logger: Logger by inject()
    private val moddingDir by lazy { get<File>(named(PLUGIN_DATA_DIR)).resolve(REFORGE_DIR_NAME).resolve(MODDING_DIR_NAME) }

    /**
     * 从配置文件夹中加载所有的定制台.
     */
    fun loadAll(): Map<String, ModdingTable> {
        val map = moddingDir
            .walk().maxDepth(1)
            .drop(1)
            .filter { it.isDirectory }
            .mapNotNull {
                try {
                    val table = load(it)
                    it.name to table
                } catch (e: Exception) {
                    logger.error("Can't load modding table: '${it.relativeTo(moddingDir)}'", e)
                    null
                }
            }
            .associate { it }
        return map
    }

    /**
     * 读取指定的配置文件夹, 从中构建一个 [ModdingTable].
     *
     * 文件结构必须如下:
     * ```
     * tableDir/
     * ├─ config.yml
     * ├─ items/
     * │  ├─ namespace_1/
     * │  │  ├─ path_1.yml
     * │  │  ├─ path_2.yml
     * │  ├─ namespace_2/
     * │  │  ├─ path_1.yml
     * │  │  ├─ path_2.yml
     * ```
     *
     * @param tableDir 定制台的配置文件夹
     * @return 读取的 [ModdingTable]
     * @throws IllegalArgumentException 如果 [tableDir] 不是文件夹
     */
    fun load(tableDir: File): ModdingTable {
        require(tableDir.isDirectory) { "Not a directory: '$tableDir'" }

        val tableMainFile = tableDir.resolve("config.yml")
        val tableItemsDir = tableDir.resolve("items")

        // config.yml 的配置节点
        val tableMainNode = yamlConfig {
            withDefaults()
            serializers { kregister(Cost) }
        }.buildAndLoadString(tableMainFile.readText())

        // 解析主要配置
        val tableMainData = object {
            val enabled: Boolean = tableMainNode.node("enabled").getBoolean(true)
            val title: Component = tableMainNode.node("title").get<Component>(Component.text("Unnamed Modding Table"))
            val cost: ModdingTable.Cost = tableMainNode.node("cost").krequire<ModdingTable.Cost>()
        }

        // 解析物品规则
        val itemRuleMap = NamespacedPathCollector(tableItemsDir, true)
            .collect("yml")
            .associateNotNull {
                try {
                    val key = Key.key(it.namespace, it.path)
                    val text = it.file.readText()
                    val itemNode = yamlConfig {
                        withDefaults()
                        serializers {
                            kregister(RaritySerializer)
                            kregister(CellRule)
                            kregister(CoreMatchRuleSerializer)
                            kregister(CurseMatchRuleSerializer)
                            kregister(SkillTriggerSerializer)
                        }
                    }.buildAndLoadString(text)

                    val cellRuleMap = itemNode.node("cells").krequire<Map<String, ModdingTable.CellRule>>()
                    val itemRule = ModdingTableImpl.ItemRule(key, ModdingTableImpl.CellRuleMap(cellRuleMap))
                    key to itemRule
                } catch (e: Exception) {
                    logger.error("Can't load item rule: '${it.file.relativeTo(moddingDir)}'", e)
                    null
                }
            }
            .toMap(HashMap())
            .let(ModdingTableImpl::ItemRuleMap)

        return ModdingTableImpl(
            enabled = tableMainData.enabled,
            title = tableMainData.title,
            cost = tableMainData.cost,
            itemRules = itemRuleMap,
        )
    }

    private object Cost : TypeSerializer<ModdingTable.Cost> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.Cost {
            val base = node.node("base").getDouble(0.0)
            val perCore = node.node("per_core").getDouble(0.0)
            val perCurse = node.node("per_curse").getDouble(0.0)
            val rarityModifiers = node.node("rarity_modifiers").childrenMap()
                .map { (nodeKey, mapChild) ->
                    val rarity = try {
                        Key.key("rarity", nodeKey.toString())
                    } catch (e: InvalidKeyException) {
                        throw IllegalArgumentException("Can't load key '$nodeKey' at ${mapChild.path()}", e)
                    }
                    val modifier = mapChild.double
                    rarity to modifier
                }
                .toMap()
            val itemLevelModifier = node.node("item_level_modifier").getDouble(0.0)
            val coreLevelModifier = node.node("core_level_modifier").getDouble(0.0)
            return ModdingTableImpl.Cost(
                base = base,
                perCore = perCore,
                perCurse = perCurse,
                rarityModifiers = rarityModifiers,
                itemLevelModifier = itemLevelModifier,
                coreLevelModifier = coreLevelModifier,
            )
        }
    }

    private object CellRule : TypeSerializer<ModdingTable.CellRule> {
        override fun deserialize(type: Type, node: ConfigurationNode): ModdingTable.CellRule {
            val permission = node.node("permission").string
            val cost = node.node("cost").getDouble(0.0)
            val modLimit = node.node("mod_limit").getInt(Int.MAX_VALUE)
            val acceptedCores = node.node("accepted_cores").getList<CoreMatchRule>(emptyList())
            val acceptedCurses = node.node("accepted_curses").getList<CurseMatchRule>(emptyList())
            return ModdingTableImpl.CellRule(
                permission = permission,
                cost = cost,
                modLimit = modLimit,
                acceptedCores = acceptedCores,
                acceptedCurses = acceptedCurses,
            )
        }
    }
}