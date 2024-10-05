package cc.mewcraft.wakame.attack

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.event.NekoEntityDamageEvent
import cc.mewcraft.wakame.util.krequire
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import java.lang.reflect.Type


/**
 * 攻击类型.
 * 其实现类先不要写成单例.
 * 未来确认是无参数的实现再写成单例.
 */
sealed interface AttackType {
    fun handleAttackEntity(player: Player, itemStack: ItemStack, damagee: Entity, event: NekoEntityDamageEvent) = Unit

    fun handleInteract(player: Player, itemStack: ItemStack, action: Action, event: PlayerInteractEvent) = Unit
}

/**
 * [AttackType] 的序列化器.
 */
internal object AttackTypeSerializer : TypeSerializer<AttackType> {
    override fun deserialize(type: Type, node: ConfigurationNode): AttackType {
        val attackType = node.node("type").krequire<String>()
        return when (attackType) {
            AxeAttack.NAME -> {
                AxeAttack()
            }

            BowShoot.NAME -> {
                BowShoot()
            }

            CrossbowShoot.NAME -> {
                CrossbowShoot()
            }

            HammerAttack.NAME -> {
                HammerAttack()
            }

            MaceAttack.NAME -> {
                MaceAttack()
            }

            SpearAttack.NAME -> {
                val size = node.node("size").getDouble(0.2)
                SpearAttack(size)
            }

            SwordAttack.NAME -> {
                SwordAttack()
            }

            TridentAttack.NAME -> {
                TridentAttack()
            }

            else -> {
                throw SerializationException("Unknown attack type")
            }
        }
    }
}