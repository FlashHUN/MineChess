package flash.minechess.proxy;

import flash.minechess.client.gui.screen.BoardScreen;
import flash.minechess.util.Match;
import flash.minechess.util.MatchUtil;
import flash.minechess.util.chess.Move;
import net.minecraft.client.Minecraft;

import java.util.UUID;

public class ClientProxy extends CommonProxy {

  Minecraft minecraft;
  public ClientProxy() {
    minecraft = Minecraft.getInstance();
  }

  public void updateBoard(String fen, Move move) {
    Match match = MatchUtil.findMatch(minecraft.player.getUniqueID());
    if (match != null) {
      match.getBoard().loadPosition(fen);
      if (minecraft.currentScreen instanceof BoardScreen) {
        BoardScreen boardScreen = (BoardScreen) minecraft.currentScreen;
        boardScreen.onMoveMade(match.getBoard(), move);
      }
      if (match.getGameState() != Match.Result.Playing) {
        MatchUtil.finishMatch(match.getWhitePlayer(), match.getBlackPlayer());
      }
    }
  }

  public void startMatch(UUID whitePlayer, UUID blackPlayer) {
    MatchUtil.newMatch(whitePlayer, blackPlayer);
  }

  public void syncMatch(UUID whitePlayer, UUID blackPlayer, String fen, int lastStartSquare, int lastTargetSquare) {
    Match match = new Match(whitePlayer, blackPlayer);
    if (!MatchUtil.matchList.contains(match)) {
      match.loadFen(fen);
      if (lastStartSquare >= 0 && lastStartSquare < 64 && lastTargetSquare >= 0 && lastTargetSquare < 64) {
        match.getBoard().lastMadeMove = new Move(lastStartSquare, lastTargetSquare);
      }
      MatchUtil.matchList.add(match);
    }
  }

  public void resignMatch(UUID playerToResign) {
    Match match = MatchUtil.findMatch(playerToResign);
    if (match != null) {
      match.resign(playerToResign);
      if (match.getGameState() != Match.Result.Playing) {
        MatchUtil.finishMatch(match.getWhitePlayer(), match.getBlackPlayer());
      }
    }
  }

}
