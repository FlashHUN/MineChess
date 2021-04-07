package flash.minechess.util;

import flash.minechess.main.Main;

import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MatchUtil {

  public static List<Match> matchList = new ArrayList<>();

  @Nullable
  public static Match findMatch(UUID player) {
    int found = -1;
    for (int i = 0; i < matchList.size(); i++) {
      Match match = matchList.get(i);
      if (match.getWhitePlayer().equals(player) || match.getBlackPlayer().equals(player)) {
        found = i;
        break;
      }
    }
    return found >= 0 ? matchList.get(found) : null;
  }

  @Nullable
  public static Match findMatch(UUID whitePlayer, UUID blackPlayer) {
    int found = -1;
    for (int i = 0; i < matchList.size(); i++) {
      Match match = matchList.get(i);
      if (match.getWhitePlayer().equals(whitePlayer) && match.getBlackPlayer().equals(blackPlayer)) {
        found = i;
        break;
      }
    }
    return found >= 0 ? matchList.get(found) : null;
  }

  @Nullable
  public static void newMatch(UUID whitePlayer, UUID blackPlayer) {
    Match match = new Match(whitePlayer, blackPlayer);
    if (!matchList.contains(match)) {
      matchList.add(match);
    }
  }

  public static void finishMatch(UUID whitePlayer, UUID blackPlayer) {
    int matchIndex = -1;
    for (int i = 0; i < matchList.size(); i++) {
      Match match = matchList.get(i);
      if (match.getWhitePlayer().equals(whitePlayer) && match.getBlackPlayer().equals(blackPlayer)) {
        Main.LOGGER.debug("GameState: " + match.getGameState().name());
        if (match.getGameState() != Match.Result.Playing) {
          matchIndex = i;
        }
      }
    }
    if (matchIndex != -1) {
      deleteMatchFile(matchList.get(matchIndex));
      matchList.remove(matchIndex);
    }
  }

  private static void deleteMatchFile(Match match) {
    try {
      String path = new File(".").getCanonicalPath();
      File directory = new File(path+"/"+Main.MODID+"/saved");
      if (directory.exists()) {
        String name = new StringBuilder(match.getWhitePlayer().toString()).append("///").append(match.getBlackPlayer().toString()).toString();
        File toDelete = new File(path+"/"+Main.MODID+"/saved/"+name);
        if (toDelete.exists()) {
          if (toDelete.delete()) {
            Main.LOGGER.debug("Successfully deleted match file " + match.getWhitePlayer() + " vs " + match.getBlackPlayer());
          } else {
            Main.LOGGER.debug("Could not delete match file " + match.getWhitePlayer() + " vs " + match.getBlackPlayer());
          }
        }
      }
    } catch (IOException e) {
      Main.LOGGER.debug("Could not delete match file " + match.getWhitePlayer() + " vs " + match.getBlackPlayer());
    }
  }

  public static void loadMatches() {
    matchList.clear();
    try {
      String path = new File(".").getCanonicalPath();
      File directory = new File(path+"/"+Main.MODID+"/saved");
      if (!directory.exists()) {
        Main.LOGGER.debug("Created MineChess save directory");
        directory.mkdirs();
      }
      for (File file : directory.listFiles()) {
        String[] players = file.getName().split("__");
        Match match = new Match(UUID.fromString(players[0]), UUID.fromString(players[1]));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String fen = reader.readLine();
        reader.close();
        match.loadBoard(fen);
        matchList.add(match);
      }
    } catch (IOException e) {
      Main.LOGGER.debug("Couldn't load previous matches");
    }
  }

  public static void saveMatches() {
    for (Match match : matchList) {
      match.export();
    }
  }

}
