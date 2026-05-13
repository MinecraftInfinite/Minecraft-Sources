package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class SpawnPointCommand {
   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawnpoint").requires((p_198699_0_) -> {
         return p_198699_0_.hasPermissionLevel(2);
      })).executes((p_198697_0_) -> {
         return setSpawnPoint((CommandSource)p_198697_0_.getSource(), Collections.singleton(((CommandSource)p_198697_0_.getSource()).asPlayer()), new BlockPos(((CommandSource)p_198697_0_.getSource()).getPos()));
      })).then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes((p_198694_0_) -> {
         return setSpawnPoint((CommandSource)p_198694_0_.getSource(), EntityArgument.getPlayers(p_198694_0_, "targets"), new BlockPos(((CommandSource)p_198694_0_.getSource()).getPos()));
      })).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes((p_198698_0_) -> {
         return setSpawnPoint((CommandSource)p_198698_0_.getSource(), EntityArgument.getPlayers(p_198698_0_, "targets"), BlockPosArgument.getBlockPos(p_198698_0_, "pos"));
      }))));
   }

   private static int setSpawnPoint(CommandSource source, Collection<EntityPlayerMP> targets, BlockPos pos) {
      for(EntityPlayerMP entityplayermp : targets) {
         entityplayermp.setSpawnPoint(pos, true);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.spawnpoint.success.single", new Object[]{pos.getX(), pos.getY(), pos.getZ(), ((EntityPlayerMP)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.spawnpoint.success.multiple", new Object[]{pos.getX(), pos.getY(), pos.getZ(), targets.size()}), true);
      }

      return targets.size();
   }
}
