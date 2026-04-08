package cc.mewcraft.wakame.bridge

interface ExtraEnchantmentEffectsRegistrar {
    fun bootstrap()

    companion object Impl : ExtraEnchantmentEffectsRegistrar {
        private var implementation: ExtraEnchantmentEffectsRegistrar = object : ExtraEnchantmentEffectsRegistrar {
            override fun bootstrap() = Unit
        }

        fun setImplementation(value: ExtraEnchantmentEffectsRegistrar) {
            implementation = value
        }

        override fun bootstrap() {
            implementation.bootstrap()
        }
    }
}