package flash.minechess.network.receive_client;

import flash.minechess.main.Main;
import flash.minechess.util.Match;
import flash.minechess.util.chess.FenUtility;
import flash.minechess.util.chess.Move;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketSyncMatch {

  UUID whitePlayer, blackPlayer;
  String fen;
  int lastStartSquare, lastTargetSquare;

  public PacketSyncMatch(Match match) {
    this(match.getWhitePlayer(), match.getBlackPlayer(), FenUtility.currentFen(match.getBoard()), 100, 100);
  }

  public PacketSyncMatch(Match match, Move move) {
    this(match.getWhitePlayer(), match.getBlackPlayer(), FenUtility.currentFen(match.getBoard()), move.getStartSquare(), move.getStartSquare());
  }

  public PacketSyncMatch(UUID whitePlayer, UUID blackPlayer, String fen, int lastStartSquare, int lastTargetSquare) {
    this.whitePlayer = whitePlayer;
    this.blackPlayer = blackPlayer;
    this.fen = fen;
    this.lastStartSquare = lastStartSquare;
    this.lastTargetSquare = lastTargetSquare;
  }

  public static void encode(PacketSyncMatch msg, PacketBuffer buf) {
    buf.writeUniqueId(msg.whitePlayer);
    buf.writeUniqueId(msg.blackPlayer);
    buf.writeString(msg.fen);
    buf.writeInt(msg.lastStartSquare);
    buf.writeInt(msg.lastTargetSquare);
  }

  public static PacketSyncMatch decode(PacketBuffer buf) {
    return new PacketSyncMatch(buf.readUniqueId(), buf.readUniqueId(), buf.readString(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketSyncMatch msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      Main.proxy.syncMatch(msg.whitePlayer, msg.blackPlayer, msg.fen, msg.lastStartSquare, msg.lastTargetSquare);
    });
    ctx.get().setPacketHandled(true);
  }
}
