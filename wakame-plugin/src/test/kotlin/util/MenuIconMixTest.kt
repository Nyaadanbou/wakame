package util

import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.config.configurate.ObjectMappers
import cc.mewcraft.wakame.util.MenuIconDictionary
import cc.mewcraft.wakame.util.MenuIconLore
import cc.mewcraft.wakame.util.MenuIconLoreSerializer
import cc.mewcraft.wakame.util.MenuIconName
import cc.mewcraft.wakame.util.kregister
import cc.mewcraft.wakame.util.krequire
import commonEnv
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.minimessage.MiniMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import kotlin.test.Test

/**
 * 使用以下代码:
 * - [cc.mewcraft.wakame.util.MenuIconName]
 * - [cc.mewcraft.wakame.util.MenuIconLore]
 * - [cc.mewcraft.wakame.util.MenuIconDictionary]
 *
 * 模拟一个完整的的使用场景.
 */
class MenuIconMixTest : KoinTest {

    @BeforeEach
    fun beforeEach() {
        startKoin {
            modules(
                commonEnv(),
                adventureModule(),
            )
        }
    }

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `mix case 1`() {
        val mm = Injector.get<MiniMessage>()

        val options = ConfigurationOptions.defaults().serializers {
            it.registerAnnotatedObjects(ObjectMappers.DEFAULT)
            it.kregister(MenuIconLoreSerializer)
        }
        val dictNode = BasicConfigurationNode.root<RuntimeException>(options) { node ->
            node.node("choice_item").set("<mark> <item>×<amount>")
            node.node("choice_exp").set("<mark> <exp>×<amount>")
            node.node("mark_success").set("✔")
            node.node("mark_failure").set("✘")
        }
        val nameNode = BasicConfigurationNode.root<RuntimeException>(options) { node ->
            node.set("*anything* <player_name>")
        }
        val loreNode = BasicConfigurationNode.root<RuntimeException>(options) { node ->
            node.set(
                listOf(
                    "the rarity is <rarity_name>!",
                    "there are <choice_amount> choices:",
                    "*prefix* {choice_list} <choice_mark>",
                    "a literal bottom line",
                )
            )
        }

        val actualDict = dictNode.krequire<MenuIconDictionary>()

        // 把 MenuIconDict 传入 MenuIconName.resolve 以便让 MenuIconName 解析额外的占位符
        val actualName = nameNode.krequire<MenuIconName>().resolve(actualDict) {
            unparsed("player_name", "Nailm")
        }

        // 把 MenuIconDict 传入 MenuIconLore.resolve 以便让 MenuIconLore 解析额外的占位符
        val actualLore = loreNode.krequire<MenuIconLore>().resolve(actualDict) {
            standard {
                unparsed("rarity_name", "rare")
                component("choice_amount", text(3))
            }
            folded("choice_list") {
                // 添加固定内容
                literal("literal 1")
                literal(text("literal 2"))

                // 添加第一行
                // 传入 resolve 的字符串是最终内容的 MiniMessage String
                resolve("choice_item") {
                    unparsed("mark", dict("mark_success"))
                    component("item", text("diamond"))
                    component("amount", text(3))
                }

                // 添加第二行 (因为是第二次调用 line)
                resolve("choice_item") {
                    unparsed("mark", dict("mark_success"))
                    component("item", text("diamond"))
                    component("amount", text(3))
                }

                // 添加第三行 (因为是第三次调用 line)
                resolve("choice_exp") {
                    unparsed("mark", dict("mark_failure"))
                    component("amount", text(123))
                }
            }
            folded("choice_list", text("choice 1"), text("choice 2"), text("choice 3"))
        }

        val expectedDict = MenuIconDictionary(
            mapOf(
                "mark_success" to "<green>✔",
                "mark_failure" to "<red>✘",
                "choice_item" to "<!i> <choice_name>",
            )
        )
    }
}