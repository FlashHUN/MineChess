package flash.minechess.network.receive_client;

import flash.minechess.main.Main;
import flash.minechess.util.chess.Board;
import flash.minechess.util.chess.FenUtility;
import flash.minechess.util.chess.Move;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUpdateClient {

  String fen;
  int startSquare, targetSquare;

  public PacketUpdateClient(Board board, Move move) {
    this(FenUtility.currentFen(board), move.getStartSquare(), move.getTargetSquare());
  }

  public PacketUpdateClient(String fen, int startSquare, int targetSquare) {
    this.fen = fen;
    this.startSquare = startSquare;
    this.targetSquare = targetSquare;
  }

  public static void encode(PacketUpdateClient msg, PacketBuffer buf) {
    buf.writeString(msg.fen);
    buf.writeInt(msg.startSquare);
    buf.writeInt(msg.targetSquare);
  }

  public static PacketUpdateClient decode(PacketBuffer buf) {
    return new PacketUpdateClient(buf.readString(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketUpdateClient msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      Main.proxy.updateBoard(msg.fen, new Move(msg.startSquare, msg.targetSquare));
    });
    ctx.get().setPacketHandled(true);
  }

}
