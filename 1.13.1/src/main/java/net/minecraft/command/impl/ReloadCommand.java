package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextComponentTranslation;

public class ReloadCommand {
   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reload").requires((p_198599_0_) -> {
         return p_198599_0_.hasPermissionLevel(3);
      })).executes((p_198598_0_) -> {
         ((CommandSource)p_198598_0_.getSource()).sendFeedback(new TextComponentTranslation("commands.reload.success", new Object[0]), true);
         ((CommandSource)p_198598_0_.getSource()).getServer().reload();
         return 0;
      }));
   }
}
