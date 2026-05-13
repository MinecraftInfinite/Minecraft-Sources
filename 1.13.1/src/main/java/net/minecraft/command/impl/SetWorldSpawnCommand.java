package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class SetWorldSpawnCommand {
   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setworldspawn").requires((p_198704_0_) -> {
         return p_198704_0_.hasPermissionLevel(2);
      })).executes((p_198700_0_) -> {
         return setSpawn((CommandSource)p_198700_0_.getSource(), new BlockPos(((CommandSource)p_198700_0_.getSource()).getPos()));
      })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((p_198703_0_) -> {
         return setSpawn((CommandSource)p_198703_0_.getSource(), BlockPosArgument.getBlockPos(p_198703_0_, "pos"));
      })));
   }

   private static int setSpawn(CommandSource source, BlockPos pos) {
      source.getWorld().setSpawnPoint(pos);
      source.getServer().getPlayerList().sendPacketToAllPlayers(new SPacketSpawnPosition(pos));
      source.sendFeedback(new TextComponentTranslation("commands.setworldspawn.success", new Object[]{pos.getX(), pos.getY(), pos.getZ()}), true);
      return 1;
   }
}
