package flash.minechess.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import flash.minechess.main.Main;
import flash.minechess.util.Challenge;
import flash.minechess.util.ChallengeUtil;
import flash.minechess.util.MatchUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public class ChallengeCommand extends Command {
  @Override
  public String getName() {
    return "challenge";
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 0;
  }

  @Override
  public void build(LiteralArgumentBuilder<CommandSource> builder) {
    builder.then(literal("c").then(
      argument("player", EntityArgument.player())
        .executes(src -> this.challengePlayer(src.getSource(), EntityArgument.getPlayer(src, "player")))));


    builder.then(literal("accept").then(
        argument("player", EntityArgument.player())
            .executes(src -> this.acceptChallenge(src.getSource(), EntityArgument.getPlayer(src, "player")))));
  }

  private int challengePlayer(CommandSource src, PlayerEntity player) throws CommandSyntaxException {
    UUID whitePlayer = src.asPlayer().getUniqueID();
    UUID blackPlayer = player.getUniqueID();
    if (whitePlayer.equals(blackPlayer)) {
      src.asPlayer().sendStatusMessage(
          new TranslationTextComponent(Main.getMessageName("challenge.self")).mergeStyle(TextFormatting.RED),
          false);
    } else if (MatchUtil.findMatch(blackPlayer) != null) {
      src.asPlayer().sendStatusMessage(
          new TranslationTextComponent(Main.getMessageName("challenge.inmatch"), player.getScoreboardName()).mergeStyle(TextFormatting.RED),
          false);
    } else {
      Challenge challenge = new Challenge(whitePlayer, blackPlayer);
      if (!ChallengeUtil.challengeList.contains(challenge)) {
        ChallengeUtil.newChallenge(whitePlayer, blackPlayer);
        src.asPlayer().sendStatusMessage(
            new TranslationTextComponent(Main.getMessageName("challenge.sent"), player.getScoreboardName()).mergeStyle(TextFormatting.GREEN),
            false);
        player.sendStatusMessage(
            new TranslationTextComponent(Main.getMessageName("challenge.received"), src.asPlayer().getScoreboardName()).mergeStyle(TextFormatting.GREEN),
            false);
      } else {
        src.asPlayer().sendStatusMessage(
            new TranslationTextComponent(Main.getMessageName("challenge.alreadysent")).mergeStyle(TextFormatting.RED),
            false);
      }
    }
    return 1;
  }

  private int acceptChallenge(CommandSource src, PlayerEntity player) throws CommandSyntaxException {
    UUID blackPlayer = src.asPlayer().getUniqueID();
    UUID whitePlayer = player.getUniqueID();
    Challenge challenge = ChallengeUtil.findChallenge(whitePlayer, blackPlayer);
    if (challenge != null) {
      src.asPlayer().sendStatusMessage(
          new TranslationTextComponent(Main.getMessageName("challenge.accept.self"), player.getScoreboardName()).mergeStyle(TextFormatting.GREEN),
          false);
      challenge.accept();
    } else {
      src.asPlayer().sendStatusMessage(
          new TranslationTextComponent(Main.getMessageName("challenge.accept.none"), player.getScoreboardName()).mergeStyle(TextFormatting.RED),
          false);
    }
    return 1;
  }

  @Override
  public boolean isDedicatedServerOnly() {
    return false;
  }
}
