package flash.minechess.network;

import flash.minechess.main.Main;
import flash.minechess.network.receive_client.PacketResignMatch;
import flash.minechess.network.receive_client.PacketStartMatch;
import flash.minechess.network.receive_client.PacketSyncMatch;
import flash.minechess.network.receive_client.PacketUpdateClient;
import flash.minechess.network.receive_server.PacketUpdateMatch;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketDispatcher {

  private static int packetId = 0;

  private static final String PROTOCOL_VERSION = "1";
  private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
      new ResourceLocation(Main.MODID, "main"),
      () -> PROTOCOL_VERSION,
      PROTOCOL_VERSION::equals,
      PROTOCOL_VERSION::equals
  );

  public static int nextID() {
    return packetId++;
  }

  public static void registerMessages() {
    INSTANCE.registerMessage(nextID(), PacketStartMatch.class, PacketStartMatch::encode, PacketStartMatch::decode, PacketStartMatch::handle);
    INSTANCE.registerMessage(nextID(), PacketSyncMatch.class, PacketSyncMatch::encode, PacketSyncMatch::decode, PacketSyncMatch::handle);
    INSTANCE.registerMessage(nextID(), PacketUpdateMatch.class, PacketUpdateMatch::encode, PacketUpdateMatch::decode, PacketUpdateMatch::handle);
    INSTANCE.registerMessage(nextID(), PacketResignMatch.class, PacketResignMatch::encode, PacketResignMatch::decode, PacketResignMatch::handle);
    INSTANCE.registerMessage(nextID(), PacketUpdateClient.class, PacketUpdateClient::encode, PacketUpdateClient::decode, PacketUpdateClient::handle);
  }

  public static <MSG> void sendTo(MSG msg, PlayerEntity player) {
    INSTANCE.sendTo(msg, ((ServerPlayerEntity)player).connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
  }

  public static <MSG> void sendToAllTracking(MSG msg, LivingEntity entityToTrack) {
    INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entityToTrack), msg);
  }

  public static <MSG> void sendToAll(MSG msg) {
    INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
  }

  public static <MSG> void sendToServer(MSG msg) {
    INSTANCE.sendToServer(msg);
  }

}
