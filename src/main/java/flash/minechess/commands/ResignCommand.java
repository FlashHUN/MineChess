package flash.minechess.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import flash.minechess.main.Main;
import flash.minechess.network.PacketDispatcher;
import flash.minechess.network.receive_client.PacketResignMatch;
import flash.minechess.util.Match;
import flash.minechess.util.MatchUtil;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ResignCommand extends Command {

  @Override
  public String getName() {
    return "resign";
  }

  @Override
  public int getRequiredPermissionLevel() {
    return 0;
  }

  @Override
  public void build(LiteralArgumentBuilder<CommandSource> builder) {
    builder.executes(src -> this.resign(src.getSource()));
  }

  private int resign(CommandSource src) throws CommandSyntaxException {
    Match match = MatchUtil.findMatch(src.asPlayer().getUniqueID());
    if (match != null) {
      match.resign(src.asPlayer().getUniqueID());
      match.notifyResign();
      PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
      PlayerEntity white = playerList.getPlayerByUUID(match.getWhitePlayer());
      PlayerEntity black = playerList.getPlayerByUUID(match.getBlackPlayer());
      if (white != null) {
        PacketDispatcher.sendTo(new PacketResignMatch(src.asPlayer().getUniqueID()), white);
      }
      if (black != null) {
        PacketDispatcher.sendTo(new PacketResignMatch(src.asPlayer().getUniqueID()), black);
      }
      MatchUtil.finishMatch(match.getWhitePlayer(), match.getBlackPlayer());
    } else {
      src.asPlayer().sendStatusMessage(
          new TranslationTextComponent(Main.getMessageName("matches.none")).mergeStyle(TextFormatting.RED),
          false);
    }
    return 1;
  }

  @Override
  public boolean isDedicatedServerOnly() {
    return false;
  }
}
