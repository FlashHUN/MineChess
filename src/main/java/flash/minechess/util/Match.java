package flash.minechess.util;

import flash.minechess.init.SoundInit;
import flash.minechess.main.Main;
import flash.minechess.util.chess.Board;
import flash.minechess.util.chess.FenUtility;
import flash.minechess.util.chess.Move;
import flash.minechess.util.chess.MoveGenerator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class Match {

  private static FileWriter fw;

  private Result gameResult;
  private Board board;
  private UUID whitePlayer;
  private UUID blackPlayer;

  public Match(UUID whitePlayer, UUID blackPlayer) {
    this.board = new Board();
    this.board.loadStartPosition();
    this.gameResult = Result.Playing;
    this.whitePlayer = whitePlayer;
    this.blackPlayer = blackPlayer;
  }

  public UUID getWhitePlayer() {
    return whitePlayer;
  }

  public UUID getBlackPlayer() {
    return blackPlayer;
  }

  public Board getBoard() {
    return board;
  }

  public String getFen() {
    return FenUtility.currentFen(board);
  }

  public void loadFen(String fen) {
    board.loadPosition(fen);
  }

  public void export() {
    try {
      String path = new File(".").getCanonicalPath();
      File directory = new File(path+"/"+Main.MODID+"/saved");
      if (!directory.exists()) {
        Main.LOGGER.debug("Created MineChess save directory");
        directory.mkdirs();
      }
      File matchFile = new File(path+"/"+Main.MODID+"/saved/"+whitePlayer.toString()+"__"+blackPlayer.toString());
      fw = new FileWriter(matchFile);
      fw.write(getFen());
      fw.flush();
      fw.close();
    } catch (IOException e) {
      Main.LOGGER.debug("Couldn't export match FEN: " + e.getMessage());
    }
  }

  public void loadBoard(String fen) {
    board.loadPosition(fen);
  }

  public void notifyPlayerToMove() {
    gameResult = getGameState();
    MoveGenerator moveGenerator = new MoveGenerator();
    List<Move> moves = moveGenerator.generateMoves(board);
    PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
    PlayerEntity playerToMove = playerList.getPlayerByUUID(board.whiteToMove ? blackPlayer : whitePlayer);
    PlayerEntity opponent = playerList.getPlayerByUUID(board.whiteToMove ? whitePlayer : blackPlayer);
    if (playerToMove != null) {
      if (gameResult == Result.Playing) {
        SoundEvent moveSound = null;
        if (moveGenerator.inCheck()) {
          moveSound = SoundInit.CHECK.get();
        } else if (board.lastMadeMoveIsCapture) {
          moveSound = SoundInit.CAPTURE.get();
        } else {
          moveSound = SoundInit.MOVE.get();
        }
        playerToMove.playSound(moveSound, 1f, 1f);
      } else {
        playerToMove.playSound(SoundInit.CHECK.get(), 1f, 1f);
        if (opponent != null && playerToMove != null) {
          opponent.sendStatusMessage(new TranslationTextComponent(Main.getMessageName("result." + gameResult.name().toLowerCase()), playerToMove.getScoreboardName()), false);
          playerToMove.sendStatusMessage(new TranslationTextComponent(Main.getMessageName("result." + gameResult.name().toLowerCase()), playerToMove.getScoreboardName()), false);
        }
      }
    }
  }

  public Result getGameState() {
    MoveGenerator moveGenerator = new MoveGenerator();
    List<Move> moves = moveGenerator.generateMoves(board);

    // Look for mate/stalemate
    if (moves.size() == 0) {
      if (moveGenerator.inCheck()) {
        return board.whiteToMove ? Result.WhiteIsMated : Result.BlackIsMated;
      }
      return Result.Stalemate;
    }

    // Fifty move rule
    if (board.fiftyMoveCounter >= 100) {
      return Result.FiftyMoveRule;
    }

    return Result.Playing;
  }

  public void makeMove(Move move) {
    board.makeMove(move);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Match match = (Match) o;
    return whitePlayer.equals(match.whitePlayer) && blackPlayer.equals(match.blackPlayer);
  }

  public enum Result { Playing, BlackIsMated, WhiteIsMated, Stalemate, FiftyMoveRule }
}
