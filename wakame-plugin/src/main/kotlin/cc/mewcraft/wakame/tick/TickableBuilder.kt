package cc.mewcraft.wakame.tick

class TickableBuilder private constructor() {
    private var initTickCount: Long = 0

    companion object Factory {
        fun newBuilder(): TickableBuilder {
            return TickableBuilder()
        }
    }

    fun initTickCount(initTickCount: Long): TickableBuilder {
        this.initTickCount = initTickCount
        return this
    }

    fun execute(execute: (Long) -> TickResult): Tickable {
        return TickableImpl(initTickCount, execute)
    }

}

private class TickableImpl(
    initTickCount: Long,
    private val execute: (Long) -> TickResult
) : Tickable {
    override var tickCount: Long = initTickCount

    override fun tick(): TickResult {
        return execute.invoke(tickCount)
    }
}