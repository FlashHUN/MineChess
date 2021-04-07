package flash.minechess.util;

import flash.minechess.main.Main;
import flash.minechess.network.PacketDispatcher;
import flash.minechess.network.receive_client.PacketStartMatch;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;

public class Challenge {

  private UUID whitePlayer;
  private UUID blackPlayer;
  private int tick;

  public Challenge(UUID whitePlayer, UUID blackPlayer) {
    this.whitePlayer = whitePlayer;
    this.blackPlayer = blackPlayer;
    this.tick = 1200;
  }

  public UUID getWhitePlayer() {
    return whitePlayer;
  }

  public UUID getBlackPlayer() {
    return blackPlayer;
  }

  public void accept() {
    MatchUtil.newMatch(whitePlayer, blackPlayer);
    PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
    PlayerEntity white = playerList.getPlayerByUUID(whitePlayer);
    PlayerEntity black = playerList.getPlayerByUUID(blackPlayer);
    if (white != null) {
      PacketDispatcher.sendTo(new PacketStartMatch(whitePlayer, blackPlayer), white);
      if (black != null) {
        PacketDispatcher.sendTo(new PacketStartMatch(whitePlayer, blackPlayer), black);
        white.sendStatusMessage(
            new TranslationTextComponent(Main.getMessageName("challenge.accept"), black.getScoreboardName()).mergeStyle(TextFormatting.GREEN),
            false);
      }
    }
    ChallengeUtil.removeChallenge(this);
  }

  public void deny() {
    PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
    PlayerEntity white = playerList.getPlayerByUUID(whitePlayer);
    PlayerEntity black = playerList.getPlayerByUUID(blackPlayer);
    if (black != null) {
      black.sendStatusMessage(
          new TranslationTextComponent(Main.getMessageName("challenge.deny.self"), white.getScoreboardName()).mergeStyle(TextFormatting.RED),
          false);
      if (white != null) {
        white.sendStatusMessage(
            new TranslationTextComponent(Main.getMessageName("challenge.deny"), black.getScoreboardName()).mergeStyle(TextFormatting.RED),
            false);
      }
    }
    ChallengeUtil.removeChallenge(this);
  }

  public void tick() {
    this.tick--;
  }

  public int getTick() {
    return tick;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Challenge challenge = (Challenge) o;
    return whitePlayer.equals(challenge.whitePlayer) && blackPlayer.equals(challenge.blackPlayer);
  }
}
