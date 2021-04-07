package flash.minechess.proxy;

import flash.minechess.util.chess.Move;

import java.util.UUID;

public class CommonProxy {

  public void updateBoard(String fen, Move move) {}

  public void startMatch(UUID whitePlayer, UUID blackPlayer) {}

  public void syncMatch(UUID whitePlayer, UUID blackPlayer, String fen, int lastStartSquare, int lastTargetSquare) {}

}
