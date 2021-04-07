package flash.minechess.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChallengeUtil {

  public static List<Challenge> challengeList = new ArrayList<>();

  public static void newChallenge(UUID whitePlayer, UUID blackPlayer) {
    Challenge challenge = new Challenge(whitePlayer, blackPlayer);
    if (!challengeList.contains(challenge)) {
      challengeList.add(challenge);
    }
  }

  public static void removeChallenge(Challenge challenge) {
    int index = -1;
    for (int i = 0; i < challengeList.size(); i++) {
      if (challengeList.get(i).equals(challenge)) {
        index = i;
        break;
      }
    }
    if (index >= 0) {
      challengeList.remove(index);
    }
  }

  public static Challenge findChallenge(UUID whitePlayer, UUID blackPlayer) {
    Challenge found = null;
    for (Challenge challenge : challengeList) {
      if (challenge.getWhitePlayer() == whitePlayer && challenge.getBlackPlayer() == blackPlayer) {
        found = challenge;
        break;
      }
    }
    return found;
  }

}
