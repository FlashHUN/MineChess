package flash.minechess.network.receive_client;

import flash.minechess.main.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketStartMatch {

  UUID whitePlayer, blackPlayer;

  public PacketStartMatch(UUID whitePlayer, UUID blackPlayer) {
    this.whitePlayer = whitePlayer;
    this.blackPlayer = blackPlayer;
  }

  public static void encode(PacketStartMatch msg, PacketBuffer buf) {
    buf.writeUniqueId(msg.whitePlayer);
    buf.writeUniqueId(msg.blackPlayer);
  }

  public static PacketStartMatch decode(PacketBuffer buf) {
    return new PacketStartMatch(buf.readUniqueId(), buf.readUniqueId());
  }

  public static void handle(PacketStartMatch msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      Main.proxy.startMatch(msg.whitePlayer, msg.blackPlayer);
    });
    ctx.get().setPacketHandled(true);
  }

}
