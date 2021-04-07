package flash.minechess.init;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import flash.minechess.commands.ChallengeCommand;
import flash.minechess.commands.Command;
import flash.minechess.main.Main;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.util.function.Supplier;

import static net.minecraft.command.Commands.literal;

public class CommandInit {

  public static ChallengeCommand CHALLENGE;

  public static void registerCommands(CommandDispatcher<CommandSource> dispatcher, Commands.EnvironmentType env) {
    CHALLENGE = registerCommand(ChallengeCommand::new, dispatcher, env);
  }

  public static <T extends Command> T registerCommand(Supplier<T> supplier, CommandDispatcher<CommandSource> dispatcher, Commands.EnvironmentType env) {
    T command = supplier.get();

    if (!command.isDedicatedServerOnly() || env == Commands.EnvironmentType.DEDICATED || env == Commands.EnvironmentType.ALL) {
      LiteralArgumentBuilder<CommandSource> builder = literal(command.getName());
      builder.requires((sender) -> sender.hasPermissionLevel(command.getRequiredPermissionLevel()));
      command.build(builder);
      dispatcher.register(literal(Main.MODID).then(builder));
    }

    return command;
  }

}
