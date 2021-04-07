package flash.minechess.network.receive_client;

import flash.minechess.main.Main;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketResignMatch {

  UUID playerToResign;

  public PacketResignMatch(UUID playerToResign) {
    this.playerToResign = playerToResign;
  }

  public static void encode(PacketResignMatch msg, PacketBuffer buf) {
    buf.writeUniqueId(msg.playerToResign);
  }

  public static PacketResignMatch decode(PacketBuffer buf) {
    return new PacketResignMatch(buf.readUniqueId());
  }

  public static void handle(PacketResignMatch msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      Main.proxy.resignMatch(msg.playerToResign);
    });
    ctx.get().setPacketHandled(true);
  }

}
