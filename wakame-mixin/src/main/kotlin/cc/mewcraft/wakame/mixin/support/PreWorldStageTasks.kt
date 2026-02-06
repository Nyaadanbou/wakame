package cc.mewcraft.wakame.mixin.support

object PreWorldStageTasks {

    private val tasks: MutableList<() -> Unit> = mutableListOf()

    fun register(action: () -> Unit) {
        tasks += action
    }

    fun run() {
        tasks.forEach { it.invoke() }
    }
}