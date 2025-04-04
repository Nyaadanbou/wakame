package cc.mewcraft.wakame.command

import cc.mewcraft.wakame.BootstrapContextStore
import org.incendo.cloud.Command
import org.incendo.cloud.CommandFactory
import org.incendo.cloud.CommandManager
import org.incendo.cloud.description.Description
import org.incendo.cloud.kotlin.MutableCommandBuilder

internal interface KoishCommandFactory<C : Any> : CommandFactory<C> {

    // 实现该函数以创建新的指令
    fun Builder<C>.createCommands()

    override fun createCommands(commandManager: CommandManager<C>): List<Command<C>> {
        return Builder(commandManager).apply { createCommands() }.getAddedCommands()
    }

    /// Convenient Builder Logics

    class Builder<C : Any>(
        private val commandManager: CommandManager<C>,
    ) {
        private val commandList: ArrayList<Command<C>> = ArrayList()
        private val pluginName = BootstrapContextStore.PLUGIN_NAME.lowercase()

        /**
         * 创建一个 [Command] 并添加到 [commandList].
         */
        fun buildAndAdd(
            lambda: MutableCommandBuilder<C>.() -> Unit,
        ) {
            commandList += build(lambda).build()
        }

        /**
         * 接着修改 [MutableCommandBuilder], 用其创建 [Command], 最后添加到 [commandList].
         */
        fun buildAndAdd(
            builder: MutableCommandBuilder<C>,
            lambda: MutableCommandBuilder<C>.() -> Unit,
        ) {
            commandList.add(builder.copy(lambda).build())
        }

        /**
         * 创建一个 [MutableCommandBuilder].
         */
        fun build(
            lambda: MutableCommandBuilder<C>.() -> Unit,
        ): MutableCommandBuilder<C> {
            return MutableCommandBuilder(pluginName, Description.empty(), emptyArray(), commandManager, lambda)
        }

        /**
         * 获取已经创建好的 [Command].
         */
        fun getAddedCommands(): List<Command<C>> = commandList
    }

}