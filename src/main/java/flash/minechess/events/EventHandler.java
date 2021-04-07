package flash.minechess.events;

import flash.minechess.network.PacketDispatcher;
import flash.minechess.network.receive_client.PacketSyncMatch;
import flash.minechess.util.Challenge;
import flash.minechess.util.ChallengeUtil;
import flash.minechess.util.Match;
import flash.minechess.util.MatchUtil;
import flash.minechess.util.chess.Move;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import java.util.ArrayList;
import java.util.List;

public class EventHandler {

  // On server shutdown, save all ongoing matches
  @SubscribeEvent
  public void serverShutdown(FMLServerStoppingEvent event) {
    MatchUtil.saveMatches();
  }

  // Handle challenge ticks
  @SubscribeEvent
  public void serverTickEvent(TickEvent.ServerTickEvent event) {
    List<Challenge> toRemove = new ArrayList<>();
    for (Challenge challenge : ChallengeUtil.challengeList) {
      challenge.tick();
      if (challenge.getTick() <= 0 || MatchUtil.findMatch(challenge.getWhitePlayer()) != null || MatchUtil.findMatch(challenge.getBlackPlayer()) != null) {
        toRemove.add(challenge);
      }
    }
    for (Challenge challenge : toRemove) {
      challenge.deny();
    }
  }

  // On player login, sync their ongoing match
  @SubscribeEvent
  public void playerLogIn(PlayerEvent.PlayerLoggedInEvent event) {
    PlayerEntity player = event.getPlayer();
    if (!player.world.isRemote) {
      Match match = MatchUtil.findMatch(player.getUniqueID());
      if (match != null) {
        Move lastMove = match.getBoard().lastMadeMove;
        if (lastMove == null) {
          PacketDispatcher.sendTo(new PacketSyncMatch(match), player);
        } else {
          PacketDispatcher.sendTo(new PacketSyncMatch(match, lastMove), player);
        }
      }
    }
  }

}
