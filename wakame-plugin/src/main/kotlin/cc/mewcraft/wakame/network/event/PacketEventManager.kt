@file:Suppress("UNCHECKED_CAST")

package cc.mewcraft.wakame.network.event

import cc.mewcraft.wakame.network.event.clientbound.ClientboundActionBarPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundBlockDestructionPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundBlockEventPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundBlockUpdatePacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundBossEventPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundContainerSetContentPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundContainerSetDataPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundContainerSetSlotPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundLevelChunkWithLightPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundLevelEventPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundMerchantOffersPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundOpenScreenPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundPlaceGhostRecipePacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundRecipeBookAddPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundRegistryDataPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetCursorItemPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEntityDataPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetEquipmentPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSetPassengersPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSoundEntityPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSoundPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundSystemChatPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundUpdateAttributesPacketEvent
import cc.mewcraft.wakame.network.event.clientbound.ClientboundUpdateRecipesPacketEvent
import cc.mewcraft.wakame.network.event.serverbound.ServerboundContainerClickPacketEvent
import cc.mewcraft.wakame.network.event.serverbound.ServerboundInteractPacketEvent
import cc.mewcraft.wakame.network.event.serverbound.ServerboundPlaceRecipePacketEvent
import cc.mewcraft.wakame.network.event.serverbound.ServerboundPlayerActionPacketEvent
import cc.mewcraft.wakame.network.event.serverbound.ServerboundSelectBundleItemPacketEvent
import cc.mewcraft.wakame.network.event.serverbound.ServerboundSetCreativeModeSlotPacketEvent
import cc.mewcraft.wakame.network.event.serverbound.ServerboundUseItemPacketEvent
import cc.mewcraft.wakame.util.toMethodHandle
import net.minecraft.network.protocol.Packet
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import xyz.xenondevs.commons.collections.removeIf
import java.lang.invoke.MethodHandle
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.reflect.KClass
import net.minecraft.network.PacketListener as MojangPacketListener

private data class Listener(val handle: MethodHandle, val priority: EventPriority, val ignoreIfCancelled: Boolean)

object PacketEventManager {
    
    private val LOCK = ReentrantLock()
    
    private val eventTypes = HashMap<KClass<out Packet<*>>, KClass<out PacketEvent<*>>>()
    private val eventConstructors = HashMap<KClass<out Packet<*>>, (Packet<*>) -> PacketEvent<Packet<*>>>()
    private val playerEventConstructors = HashMap<KClass<out Packet<*>>, (Player, Packet<*>) -> PlayerPacketEvent<Packet<*>>>()
    
    private val listeners = HashMap<KClass<out PacketEvent<*>>, MutableList<Listener>>()
    private val listenerInstances = HashMap<Any, List<Listener>>()
    
    init {
        // clientbound - player
        registerPlayerEventType(::ClientboundSystemChatPacketEvent)
        registerPlayerEventType(::ClientboundActionBarPacketEvent)
        registerPlayerEventType(::ClientboundContainerSetContentPacketEvent)
        registerPlayerEventType(::ClientboundContainerSetSlotPacketEvent)
        registerPlayerEventType(::ClientboundSetEntityDataPacketEvent)
        registerPlayerEventType(::ClientboundSetEquipmentPacketEvent)
        registerPlayerEventType(::ClientboundUpdateRecipesPacketEvent)
        registerPlayerEventType(::ClientboundBlockDestructionPacketEvent)
        registerPlayerEventType(::ClientboundSoundPacketEvent)
        registerPlayerEventType(::ClientboundSoundEntityPacketEvent)
        registerPlayerEventType(::ClientboundSetPassengersPacketEvent)
        registerPlayerEventType(::ClientboundLevelChunkWithLightPacketEvent)
        registerPlayerEventType(::ClientboundBlockUpdatePacketEvent)
        registerPlayerEventType(::ClientboundBlockEventPacketEvent)
        registerPlayerEventType(::ClientboundBossEventPacketEvent)
        registerPlayerEventType(::ClientboundMerchantOffersPacketEvent)
        registerPlayerEventType(::ClientboundLevelEventPacketEvent)
        registerPlayerEventType(::ClientboundContainerSetDataPacketEvent)
        registerPlayerEventType(::ClientboundUpdateAttributesPacketEvent)
        registerPlayerEventType(::ClientboundSetCursorItemPacketEvent)
        registerPlayerEventType(::ClientboundOpenScreenPacketEvent)
        registerPlayerEventType(::ClientboundRecipeBookAddPacketEvent)
        registerPlayerEventType(::ClientboundPlaceGhostRecipePacketEvent)
        registerEventType(::ClientboundRegistryDataPacketEvent)
        
        // serverbound - player
        registerPlayerEventType(::ServerboundPlaceRecipePacketEvent)
        registerPlayerEventType(::ServerboundSetCreativeModeSlotPacketEvent)
        registerPlayerEventType(::ServerboundPlayerActionPacketEvent)
        registerPlayerEventType(::ServerboundUseItemPacketEvent)
        registerPlayerEventType(::ServerboundInteractPacketEvent)
        registerPlayerEventType(::ServerboundContainerClickPacketEvent)
        registerPlayerEventType(::ServerboundSelectBundleItemPacketEvent)
    }

    private inline fun <reified P : Packet<*>, reified E : PacketEvent<P>> registerEventType(noinline constructor: (P) -> E) {
        eventTypes[P::class] = E::class
        eventConstructors[P::class] = constructor as (Packet<*>) -> PacketEvent<Packet<*>>
    }

    private inline fun <reified P : Packet<*>, reified E : PlayerPacketEvent<P>> registerPlayerEventType(noinline constructor: (Player, P) -> E) {
        eventTypes[P::class] = E::class
        playerEventConstructors[P::class] = constructor as (Player, Packet<*>) -> PlayerPacketEvent<Packet<*>>
    }

    internal fun <T : MojangPacketListener, P : Packet<in T>> createAndCallEvent(player: Player?, packet: P): PacketEvent<P>? = LOCK.withLock {
        val packetClass = packet::class

        val packetEventClass = eventTypes[packetClass]
        if (packetEventClass != null && listeners[packetEventClass] != null) {
            val event = playerEventConstructors[packetClass]?.invoke(player ?: return null, packet)
                ?: eventConstructors[packetClass]?.invoke(packet)
                ?: return null

            callEvent(event)

            return event as PacketEvent<P>
        }

        return null
    }

    private fun callEvent(event: PacketEvent<*>) {
        listeners[event::class]?.forEach { (handle, _, ignoreIfCancelled) ->
            if (!ignoreIfCancelled || !event.isCancelled) {
                try {
                    handle.invoke(event)
                } catch (t: Throwable) {
                    t.printStackTrace()
                }
            }
        }
    }

    fun registerListener(listener: PacketListener): Unit = LOCK.withLock {
        val instanceListeners = ArrayList<Listener>()

        listener::class.java.declaredMethods.forEach { method ->
            if (method.isAnnotationPresent(PacketHandler::class.java) && method.parameters.size == 1) {
                val param = method.parameters.first().type.kotlin
                if (param in eventTypes.values) {
                    param as KClass<out PacketEvent<*>>
                    method.isAccessible = true

                    val priority = method.getAnnotation(PacketHandler::class.java).priority
                    val ignoreIfCancelled = method.getAnnotation(PacketHandler::class.java).ignoreIfCancelled

                    val listener = Listener(method.toMethodHandle(listener), priority, ignoreIfCancelled)
                    instanceListeners += listener

                    val list = listeners[param]?.let(::ArrayList) ?: ArrayList()
                    list += listener
                    list.sortBy { it.priority }

                    listeners[param] = list
                }
            }
        }

        if (instanceListeners.isNotEmpty())
            listenerInstances[listener] = instanceListeners
    }

    fun unregisterListener(listener: PacketListener): Unit = LOCK.withLock {
        val toRemove = listenerInstances[listener]?.toHashSet() ?: return

        listeners.removeIf { (_, list) ->
            list.removeIf { it in toRemove }
            return@removeIf list.isEmpty()
        }

        listenerInstances -= listener
    }


}