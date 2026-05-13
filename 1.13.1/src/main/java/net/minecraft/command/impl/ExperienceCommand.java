package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

public class ExperienceCommand {
   private static final SimpleCommandExceptionType SET_POINTS_INVALID_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.experience.set.points.invalid", new Object[0]));

   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      LiteralCommandNode<CommandSource> literalcommandnode = dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("experience").requires((p_198442_0_) -> {
         return p_198442_0_.hasPermissionLevel(2);
      })).then(Commands.literal("add").then(Commands.argument("targets", EntityArgument.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer()).executes((p_198445_0_) -> {
         return addExperience((CommandSource)p_198445_0_.getSource(), EntityArgument.getPlayers(p_198445_0_, "targets"), IntegerArgumentType.getInteger(p_198445_0_, "amount"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("points").executes((p_198447_0_) -> {
         return addExperience((CommandSource)p_198447_0_.getSource(), EntityArgument.getPlayers(p_198447_0_, "targets"), IntegerArgumentType.getInteger(p_198447_0_, "amount"), ExperienceCommand.Type.POINTS);
      }))).then(Commands.literal("levels").executes((p_198436_0_) -> {
         return addExperience((CommandSource)p_198436_0_.getSource(), EntityArgument.getPlayers(p_198436_0_, "targets"), IntegerArgumentType.getInteger(p_198436_0_, "amount"), ExperienceCommand.Type.LEVELS);
      })))))).then(Commands.literal("set").then(Commands.argument("targets", EntityArgument.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("amount", IntegerArgumentType.integer(0)).executes((p_198439_0_) -> {
         return setExperience((CommandSource)p_198439_0_.getSource(), EntityArgument.getPlayers(p_198439_0_, "targets"), IntegerArgumentType.getInteger(p_198439_0_, "amount"), ExperienceCommand.Type.POINTS);
      })).then(Commands.literal("points").executes((p_198444_0_) -> {
         return setExperience((CommandSource)p_198444_0_.getSource(), EntityArgument.getPlayers(p_198444_0_, "targets"), IntegerArgumentType.getInteger(p_198444_0_, "amount"), ExperienceCommand.Type.POINTS);
      }))).then(Commands.literal("levels").executes((p_198440_0_) -> {
         return setExperience((CommandSource)p_198440_0_.getSource(), EntityArgument.getPlayers(p_198440_0_, "targets"), IntegerArgumentType.getInteger(p_198440_0_, "amount"), ExperienceCommand.Type.LEVELS);
      })))))).then(Commands.literal("query").then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.player()).then(Commands.literal("points").executes((p_198435_0_) -> {
         return queryExperience((CommandSource)p_198435_0_.getSource(), EntityArgument.getPlayer(p_198435_0_, "targets"), ExperienceCommand.Type.POINTS);
      }))).then(Commands.literal("levels").executes((p_198446_0_) -> {
         return queryExperience((CommandSource)p_198446_0_.getSource(), EntityArgument.getPlayer(p_198446_0_, "targets"), ExperienceCommand.Type.LEVELS);
      })))));
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("xp").requires((p_198441_0_) -> {
         return p_198441_0_.hasPermissionLevel(2);
      })).redirect(literalcommandnode));
   }

   private static int queryExperience(CommandSource source, EntityPlayerMP player, ExperienceCommand.Type type) {
      int i = type.xpGetter.applyAsInt(player);
      source.sendFeedback(new TextComponentTranslation("commands.experience.query." + type.name, new Object[]{player.getDisplayName(), i}), false);
      return i;
   }

   private static int addExperience(CommandSource source, Collection<? extends EntityPlayerMP> targets, int amount, ExperienceCommand.Type type) {
      for(EntityPlayerMP entityplayermp : targets) {
         type.xpAdder.accept(entityplayermp, amount);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.experience.add." + type.name + ".success.single", new Object[]{amount, ((EntityPlayerMP)targets.iterator().next()).getDisplayName()}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.experience.add." + type.name + ".success.multiple", new Object[]{amount, targets.size()}), true);
      }

      return targets.size();
   }

   private static int setExperience(CommandSource source, Collection<? extends EntityPlayerMP> targets, int amount, ExperienceCommand.Type type) throws CommandSyntaxException {
      int i = 0;

      for(EntityPlayerMP entityplayermp : targets) {
         if (type.xpSetter.test(entityplayermp, amount)) {
            ++i;
         }
      }

      if (i == 0) {
         throw SET_POINTS_INVALID_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TextComponentTranslation("commands.experience.set." + type.name + ".success.single", new Object[]{amount, ((EntityPlayerMP)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TextComponentTranslation("commands.experience.set." + type.name + ".success.multiple", new Object[]{amount, targets.size()}), true);
         }

         return targets.size();
      }
   }

   static enum Type {
      POINTS("points", EntityPlayer::giveExperiencePoints, (p_198424_0_, p_198424_1_) -> {
         if (p_198424_1_ >= p_198424_0_.xpBarCap()) {
            return false;
         } else {
            p_198424_0_.func_195394_a(p_198424_1_);
            return true;
         }
      }, (p_198422_0_) -> {
         return MathHelper.floor(p_198422_0_.experience * (float)p_198422_0_.xpBarCap());
      }),
      LEVELS("levels", EntityPlayerMP::addExperienceLevel, (p_198425_0_, p_198425_1_) -> {
         p_198425_0_.func_195399_b(p_198425_1_);
         return true;
      }, (p_198427_0_) -> {
         return p_198427_0_.experienceLevel;
      });

      public final BiConsumer<EntityPlayerMP, Integer> xpAdder;
      public final BiPredicate<EntityPlayerMP, Integer> xpSetter;
      public final String name;
      private final ToIntFunction<EntityPlayerMP> xpGetter;

      private Type(String nameIn, BiConsumer<EntityPlayerMP, Integer> xpAdderIn, BiPredicate<EntityPlayerMP, Integer> xpSetterIn, ToIntFunction<EntityPlayerMP> xpGetterIn) {
         this.xpAdder = xpAdderIn;
         this.name = nameIn;
         this.xpSetter = xpSetterIn;
         this.xpGetter = xpGetterIn;
      }
   }
}
