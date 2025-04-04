package cc.mewcraft.wakame.adventure

import cc.mewcraft.wakame.BootstrapContextStore
import cc.mewcraft.wakame.SERVER
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.join
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.JoinConfiguration
import net.kyori.adventure.text.format.TextColor


/**
 * A collection of formatted messages used by the plugin.
 */
object BuiltInMessages {

    // 定义颜色常量
    val PRIMARY_COLOR = TextColor.color(0x1D3557)  // 深蓝色
    val SECONDARY_COLOR = TextColor.color(0x457B9D)  // 浅蓝色
    val ACCENT_COLOR = TextColor.color(0x2A9D8F)  // 鲜亮绿色
    val INFO_COLOR = TextColor.color(0xF1FAEE)  // 浅灰色

    val STARTUP_BANNER: Args0 = Args0 {
        val infoLine1: Component = Component.text()
            .append(Component.text(BootstrapContextStore.PLUGIN_NAME, PRIMARY_COLOR))  // 使用主色
            .appendSpace()
            .append(Component.text("v${BootstrapContextStore.PLUGIN_VERSION}", SECONDARY_COLOR))  // 使用次色
            .build()
        val infoLine2: Component = Component.text()
            .color(INFO_COLOR)  // 使用辅助色
            .append(Component.text("Running on "))
            .append(Component.text(SERVER.name, INFO_COLOR))  // 统一使用辅助色
            .append(Component.text(" - "))
            .append(Component.text(SERVER.version, INFO_COLOR))  // 统一使用辅助色
            .build()

        // 插入 ASCII Logo 和其他信息
        return@Args0 joinNewline(
            Component.empty(),
            Component.text("  _  __     _     _   ", ACCENT_COLOR),  // 使用强调色
            Component.text(" | |/ /    (_)   | |  ", ACCENT_COLOR),
            Component.text(" | ' / ___  _ ___| |__       ", ACCENT_COLOR).append(infoLine1),
            Component.text(" |  < / _ \\| / __| '_ \\", ACCENT_COLOR),
            Component.text(" | . | (_) | \\__ | | | |     ", ACCENT_COLOR).append(infoLine2),
            Component.text(" |_|\\_\\___/|_|___|_| |_|", ACCENT_COLOR),
            Component.empty()
        )
    }

    private fun joinNewline(vararg components: ComponentLike): Component {
        return join(JoinConfiguration.newlines(), *components)
    }

    fun interface Args0 {
        fun build(): Component

        fun send(sender: Audience) {
            sender.sendMessage(build())
        }
    }

    interface Args1<A0> {
        fun build(arg0: A0): Component

        fun send(sender: Audience, arg0: A0) {
            sender.sendMessage(build(arg0))
        }
    }

    interface Args2<A0, A1> {
        fun build(arg0: A0, arg1: A1): Component

        fun send(sender: Audience, arg0: A0, arg1: A1) {
            sender.sendMessage(build(arg0, arg1))
        }
    }

    interface Args3<A0, A1, A2> {
        fun build(arg0: A0, arg1: A1, arg2: A2): Component

        fun send(sender: Audience, arg0: A0, arg1: A1, arg2: A2) {
            sender.sendMessage(build(arg0, arg1, arg2))
        }
    }

    interface Args4<A0, A1, A2, A3> {
        fun build(arg0: A0, arg1: A1, arg2: A2, arg3: A3): Component

        fun send(sender: Audience, arg0: A0, arg1: A1, arg2: A2, arg3: A3) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3))
        }
    }

    interface Args5<A0, A1, A2, A3, A4> {
        fun build(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4): Component

        fun send(sender: Audience, arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3, arg4))
        }
    }

    interface Args6<A0, A1, A2, A3, A4, A5> {
        fun build(arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5): Component

        fun send(sender: Audience, arg0: A0, arg1: A1, arg2: A2, arg3: A3, arg4: A4, arg5: A5) {
            sender.sendMessage(build(arg0, arg1, arg2, arg3, arg4, arg5))
        }
    }
}