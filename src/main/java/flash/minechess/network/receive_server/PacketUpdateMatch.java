package flash.minechess.network.receive_server;

import flash.minechess.network.PacketDispatcher;
import flash.minechess.network.receive_client.PacketUpdateClient;
import flash.minechess.util.Match;
import flash.minechess.util.MatchUtil;
import flash.minechess.util.chess.Board;
import flash.minechess.util.chess.Move;
import flash.minechess.util.chess.MoveGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketUpdateMatch {
  UUID whitePlayer, blackPlayer;
  int startSquare, targetSquare;

  public PacketUpdateMatch(UUID whitePlayer, UUID blackPlayer, Move move) {
    this.whitePlayer = whitePlayer;
    this.blackPlayer = blackPlayer;
    this.startSquare = move.getStartSquare();
    this.targetSquare = move.getTargetSquare();
  }

  public PacketUpdateMatch(UUID whitePlayer, UUID blackPlayer, int startSquare, int targetSquare) {
    this.whitePlayer = whitePlayer;
    this.blackPlayer = blackPlayer;
    this.startSquare = startSquare;
    this.targetSquare = targetSquare;
  }

  public static void encode(PacketUpdateMatch msg, PacketBuffer buf) {
    buf.writeUniqueId(msg.whitePlayer);
    buf.writeUniqueId(msg.blackPlayer);
    buf.writeInt(msg.startSquare);
    buf.writeInt(msg.targetSquare);
  }

  public static PacketUpdateMatch decode(PacketBuffer buf) {
    return new PacketUpdateMatch(buf.readUniqueId(), buf.readUniqueId(), buf.readInt(), buf.readInt());
  }

  public static void handle(PacketUpdateMatch msg, Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      Match match = MatchUtil.findMatch(msg.whitePlayer, msg.blackPlayer);
      if (match != null) {
        MoveGenerator moveGenerator = new MoveGenerator();
        boolean moveIsLegal = false;
        List<Move> legalMoves = moveGenerator.generateMoves(match.getBoard());
        Move chosenMove = null;
        for (int i = 0; i < legalMoves.size(); i++) {
          Move legalMove = legalMoves.get(i);

          if (legalMove.getStartSquare() == msg.startSquare && legalMove.getTargetSquare() == msg.targetSquare) {
            moveIsLegal = true;
            chosenMove = legalMove;
            break;
          }
        }

        if (moveIsLegal) {
          match.makeMove(chosenMove);
          match.notifyPlayerToMove();

          PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
          PlayerEntity white = playerList.getPlayerByUUID(msg.whitePlayer);
          PlayerEntity black = playerList.getPlayerByUUID(msg.blackPlayer);
          Board board = match.getBoard();
          if (white != null) {
            PacketDispatcher.sendTo(new PacketUpdateClient(board, chosenMove), white);
          }
          if (black != null) {
            PacketDispatcher.sendTo(new PacketUpdateClient(board, chosenMove), black);
          }

          if (match.getGameState() != Match.Result.Playing) {
            MatchUtil.finishMatch(msg.whitePlayer, msg.blackPlayer);
          }
        }
      }
    });
    ctx.get().setPacketHandled(true);
  }

}
