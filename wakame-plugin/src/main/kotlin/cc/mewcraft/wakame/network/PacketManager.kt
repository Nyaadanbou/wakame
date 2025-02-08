package cc.mewcraft.wakame.network

import cc.mewcraft.wakame.lifecycle.initializer.DisableFun
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InternalInit
import cc.mewcraft.wakame.lifecycle.initializer.InternalInitStage
import cc.mewcraft.wakame.util.MINECRAFT_SERVER
import cc.mewcraft.wakame.util.registerEvents
import cc.mewcraft.wakame.util.serverPlayer
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import net.kyori.adventure.text.Component
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerConnectionListener
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerLoginEvent.Result
import org.bukkit.event.player.PlayerQuitEvent
import java.lang.invoke.MethodHandles
import net.minecraft.world.entity.player.Player as MojangPlayer

private val SERVER_CONNECTION_LISTENER_CHANNELS_GETTER = MethodHandles
    .privateLookupIn(ServerConnectionListener::class.java, MethodHandles.lookup())
    .findGetter(ServerConnectionListener::class.java, "channels", List::class.java)
private val SERVER_COMMON_PACKET_LISTENER_IMPL_CONNECTION_GETTER = MethodHandles
    .privateLookupIn(ServerCommonPacketListenerImpl::class.java, MethodHandles.lookup())
    .findGetter(ServerCommonPacketListenerImpl::class.java, "connection", Connection::class.java)

val Player.packetHandler: PacketHandler?
    get() = PacketManager.playerHandlers[name]

val MojangPlayer.packetHandler: PacketHandler?
    get() = PacketManager.playerHandlers[scoreboardName]

fun Player.send(vararg bufs: FriendlyByteBuf, retain: Boolean = true, flush: Boolean = true) {
    val packetHandler = packetHandler ?: return
    bufs.forEach {
        if (retain) it.retain()
        packetHandler.queueByteBuf(it)
    }
    
    if (flush) packetHandler.channel.flush()
}

@InternalInit(stage = InternalInitStage.POST_WORLD)
internal object PacketManager : Listener {
    
    private lateinit var serverChannel: Channel
    private val connectionsList = MINECRAFT_SERVER.connection.connections
    
    val playerHandlers = HashMap<String, PacketHandler>()
    
    @InitFun
    private fun init() {
        registerEvents()
        registerHandlers()
    }
    
    @DisableFun
    private fun disable() {
        Bukkit.getOnlinePlayers().forEach(::unregisterHandler)
        
        if (::serverChannel.isInitialized) {
            serverChannel.eventLoop().submit {
                val pipeline = serverChannel.pipeline()
                pipeline.context("koish_pipeline_adapter")?.handler()?.run(pipeline::remove)
            }
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerHandlers() {
        val channels = SERVER_CONNECTION_LISTENER_CHANNELS_GETTER.invoke(MINECRAFT_SERVER.connection) as List<ChannelFuture>
        serverChannel = channels.first().channel()
        
        val pipeline = serverChannel.pipeline()
        pipeline.context("koish_pipeline_adapter")?.handler()?.run(pipeline::remove)
        pipeline.addFirst("koish_pipeline_adapter", PipelineAdapter)
        
        Bukkit.getOnlinePlayers().forEach { unregisterHandler(it); registerHandler(it) }
    }
    
    @EventHandler
    private fun handleLogin(event: PlayerLoginEvent) {
        val handler = playerHandlers[event.player.name]
        if (handler == null) {
            event.disallow(Result.KICK_OTHER, Component.text("[Koish] Something went wrong"))
            return
        }
        handler.player = event.player
    }
    
    @EventHandler
    private fun handleJoin(event: PlayerJoinEvent) {
        val handler = playerHandlers[event.player.name]!!
        handler.loggedIn = true
    }
    
    @EventHandler
    private fun handleQuit(event: PlayerQuitEvent) {
        playerHandlers -= event.player.name
    }
    
    object PipelineAdapter : ChannelInboundHandlerAdapter() {
        
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is Channel)
                msg.pipeline().addFirst("koish_pre_init_handler", PreInitHandler)
            super.channelRead(ctx, msg)
        }
        
    }
    
    object PreInitHandler : ChannelInitializer<Channel>() {
        
        override fun initChannel(channel: Channel) {
            channel.pipeline().addLast("koish_init_handler", KoishInitHandler)
        }
        
    }
    
    object KoishInitHandler : ChannelInitializer<Channel>() {
        
        override fun initChannel(channel: Channel) {
            synchronized(connectionsList) {
                channel.eventLoop().submit {
                    channel.pipeline().addBefore("packet_handler", "koish_packet_handler", PacketHandler(channel))
                }
            }
        }
        
    }
    
    private fun registerHandler(player: Player) {
        val connection = SERVER_COMMON_PACKET_LISTENER_IMPL_CONNECTION_GETTER.invoke(player.serverPlayer.connection) as Connection
        val channel = connection.channel
        channel.pipeline().addBefore("packet_handler", "koish_packet_handler", PacketHandler(channel, player))
    }
    
    private fun unregisterHandler(player: Player) {
        val connection = SERVER_COMMON_PACKET_LISTENER_IMPL_CONNECTION_GETTER.invoke(player.serverPlayer.connection) as Connection
        val pipeline = connection.channel.pipeline()
        pipeline.context("koish_packet_handler")?.handler()?.run(pipeline::remove)
    }
    
}