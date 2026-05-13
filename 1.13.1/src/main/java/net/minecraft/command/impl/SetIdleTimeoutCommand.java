package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextComponentTranslation;

public class SetIdleTimeoutCommand {
   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setidletimeout").requires((p_198692_0_) -> {
         return p_198692_0_.hasPermissionLevel(3);
      })).then(Commands.argument("minutes", IntegerArgumentType.integer(0)).executes((p_198691_0_) -> {
         return setTimeout((CommandSource)p_198691_0_.getSource(), IntegerArgumentType.getInteger(p_198691_0_, "minutes"));
      })));
   }

   private static int setTimeout(CommandSource source, int idleTimeout) {
      source.getServer().setPlayerIdleTimeout(idleTimeout);
      source.sendFeedback(new TextComponentTranslation("commands.setidletimeout.success", new Object[]{idleTimeout}), true);
      return idleTimeout;
   }
}
