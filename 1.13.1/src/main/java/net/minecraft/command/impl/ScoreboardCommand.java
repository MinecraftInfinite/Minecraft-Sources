package net.minecraft.command.impl;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ComponentArgument;
import net.minecraft.command.arguments.ObjectiveArgument;
import net.minecraft.command.arguments.ObjectiveCriteriaArgument;
import net.minecraft.command.arguments.OperationArgument;
import net.minecraft.command.arguments.ScoreHolderArgument;
import net.minecraft.command.arguments.ScoreboardSlotArgument;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextComponentUtils;

public class ScoreboardCommand {
   private static final SimpleCommandExceptionType OBJECTIVE_ALREADY_EXISTS_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.scoreboard.objectives.add.duplicate", new Object[0]));
   private static final SimpleCommandExceptionType DISPLAY_ALREADY_CLEAR_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.scoreboard.objectives.display.alreadyEmpty", new Object[0]));
   private static final SimpleCommandExceptionType DISPLAY_ALREADY_SET_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.scoreboard.objectives.display.alreadySet", new Object[0]));
   private static final SimpleCommandExceptionType ENABLE_TRIGGER_FAILED = new SimpleCommandExceptionType(new TextComponentTranslation("commands.scoreboard.players.enable.failed", new Object[0]));
   private static final SimpleCommandExceptionType ENABLE_TRIGGER_INVALID = new SimpleCommandExceptionType(new TextComponentTranslation("commands.scoreboard.players.enable.invalid", new Object[0]));
   private static final Dynamic2CommandExceptionType SCOREBOARD_PLAYER_NOT_FOUND_EXCEPTION = new Dynamic2CommandExceptionType((p_208907_0_, p_208907_1_) -> {
      return new TextComponentTranslation("commands.scoreboard.players.get.null", new Object[]{p_208907_0_, p_208907_1_});
   });

   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("scoreboard").requires((p_198650_0_) -> {
         return p_198650_0_.hasPermissionLevel(2);
      })).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("objectives").then(Commands.literal("list").executes((p_198640_0_) -> {
         return listObjectives((CommandSource)p_198640_0_.getSource());
      }))).then(Commands.literal("add").then(Commands.argument("objective", StringArgumentType.word()).then(((RequiredArgumentBuilder)Commands.argument("criteria", ObjectiveCriteriaArgument.objectiveCriteria()).executes((p_198636_0_) -> {
         return addObjective((CommandSource)p_198636_0_.getSource(), StringArgumentType.getString(p_198636_0_, "objective"), ObjectiveCriteriaArgument.getObjectiveCriteria(p_198636_0_, "criteria"), new TextComponentString(StringArgumentType.getString(p_198636_0_, "objective")));
      })).then(Commands.argument("displayName", ComponentArgument.component()).executes((p_198649_0_) -> {
         return addObjective((CommandSource)p_198649_0_.getSource(), StringArgumentType.getString(p_198649_0_, "objective"), ObjectiveCriteriaArgument.getObjectiveCriteria(p_198649_0_, "criteria"), ComponentArgument.getComponent(p_198649_0_, "displayName"));
      })))))).then(Commands.literal("modify").then(((RequiredArgumentBuilder)Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.literal("displayname").then(Commands.argument("displayName", ComponentArgument.component()).executes((p_211750_0_) -> {
         return setDisplayName((CommandSource)p_211750_0_.getSource(), ObjectiveArgument.getObjective(p_211750_0_, "objective"), ComponentArgument.getComponent(p_211750_0_, "displayName"));
      })))).then(createRenderTypeArgument())))).then(Commands.literal("remove").then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198646_0_) -> {
         return removeObjective((CommandSource)p_198646_0_.getSource(), ObjectiveArgument.getObjective(p_198646_0_, "objective"));
      })))).then(Commands.literal("setdisplay").then(((RequiredArgumentBuilder)Commands.argument("slot", ScoreboardSlotArgument.scoreboardSlot()).executes((p_198652_0_) -> {
         return clearObjectiveDisplaySlot((CommandSource)p_198652_0_.getSource(), ScoreboardSlotArgument.getScoreboardSlot(p_198652_0_, "slot"));
      })).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198639_0_) -> {
         return setObjectiveDisplaySlot((CommandSource)p_198639_0_.getSource(), ScoreboardSlotArgument.getScoreboardSlot(p_198639_0_, "slot"), ObjectiveArgument.getObjective(p_198639_0_, "objective"));
      })))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("players").then(((LiteralArgumentBuilder)Commands.literal("list").executes((p_198642_0_) -> {
         return listPlayers((CommandSource)p_198642_0_.getSource());
      })).then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).executes((p_198631_0_) -> {
         return listPlayerScores((CommandSource)p_198631_0_.getSource(), ScoreHolderArgument.getSingleScoreHolderNoObjectives(p_198631_0_, "target"));
      })))).then(Commands.literal("set").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer()).executes((p_198655_0_) -> {
         return setPlayerScore((CommandSource)p_198655_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198655_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198655_0_, "objective"), IntegerArgumentType.getInteger(p_198655_0_, "score"));
      })))))).then(Commands.literal("get").then(Commands.argument("target", ScoreHolderArgument.scoreHolder()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198660_0_) -> {
         return getPlayerScore((CommandSource)p_198660_0_.getSource(), ScoreHolderArgument.getSingleScoreHolderNoObjectives(p_198660_0_, "target"), ObjectiveArgument.getObjective(p_198660_0_, "objective"));
      }))))).then(Commands.literal("add").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((p_198645_0_) -> {
         return addToPlayerScore((CommandSource)p_198645_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198645_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198645_0_, "objective"), IntegerArgumentType.getInteger(p_198645_0_, "score"));
      })))))).then(Commands.literal("remove").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).then(Commands.argument("objective", ObjectiveArgument.objective()).then(Commands.argument("score", IntegerArgumentType.integer(0)).executes((p_198648_0_) -> {
         return removeFromPlayerScore((CommandSource)p_198648_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198648_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198648_0_, "objective"), IntegerArgumentType.getInteger(p_198648_0_, "score"));
      })))))).then(Commands.literal("reset").then(((RequiredArgumentBuilder)Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).executes((p_198635_0_) -> {
         return resetPlayerAllScores((CommandSource)p_198635_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198635_0_, "targets"));
      })).then(Commands.argument("objective", ObjectiveArgument.objective()).executes((p_198630_0_) -> {
         return resetPlayerScore((CommandSource)p_198630_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198630_0_, "targets"), ObjectiveArgument.getObjective(p_198630_0_, "objective"));
      }))))).then(Commands.literal("enable").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).then(Commands.argument("objective", ObjectiveArgument.objective()).suggests((p_198638_0_, p_198638_1_) -> {
         return suggestTriggers((CommandSource)p_198638_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198638_0_, "targets"), p_198638_1_);
      }).executes((p_198628_0_) -> {
         return enableTrigger((CommandSource)p_198628_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198628_0_, "targets"), ObjectiveArgument.getObjective(p_198628_0_, "objective"));
      }))))).then(Commands.literal("operation").then(Commands.argument("targets", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).then(Commands.argument("targetObjective", ObjectiveArgument.objective()).then(Commands.argument("operation", OperationArgument.operation()).then(Commands.argument("source", ScoreHolderArgument.scoreHolders()).suggests(ScoreHolderArgument.SUGGEST_ENTITY_SELECTOR).then(Commands.argument("sourceObjective", ObjectiveArgument.objective()).executes((p_198657_0_) -> {
         return applyScoreOperation((CommandSource)p_198657_0_.getSource(), ScoreHolderArgument.getScoreHolder(p_198657_0_, "targets"), ObjectiveArgument.getWritableObjective(p_198657_0_, "targetObjective"), OperationArgument.getOperation(p_198657_0_, "operation"), ScoreHolderArgument.getScoreHolder(p_198657_0_, "source"), ObjectiveArgument.getObjective(p_198657_0_, "sourceObjective"));
      })))))))));
   }

   private static LiteralArgumentBuilder<CommandSource> createRenderTypeArgument() {
      LiteralArgumentBuilder<CommandSource> literalargumentbuilder = Commands.literal("rendertype");

      for(ScoreCriteria.RenderType scorecriteria$rendertype : ScoreCriteria.RenderType.values()) {
         literalargumentbuilder.then(Commands.literal(scorecriteria$rendertype.getId()).executes((p_211912_1_) -> {
            return setRenderType((CommandSource)p_211912_1_.getSource(), ObjectiveArgument.getObjective(p_211912_1_, "objective"), scorecriteria$rendertype);
         }));
      }

      return literalargumentbuilder;
   }

   private static CompletableFuture<Suggestions> suggestTriggers(CommandSource source, Collection<String> targets, SuggestionsBuilder suggestions) {
      List<String> list = Lists.<String>newArrayList();
      Scoreboard scoreboard = source.getServer().getScoreboard();

      for(ScoreObjective scoreobjective : scoreboard.getScoreObjectives()) {
         if (scoreobjective.getCriteria() == ScoreCriteria.TRIGGER) {
            boolean flag = false;

            for(String s : targets) {
               if (!scoreboard.entityHasObjective(s, scoreobjective) || scoreboard.getOrCreateScore(s, scoreobjective).isLocked()) {
                  flag = true;
                  break;
               }
            }

            if (flag) {
               list.add(scoreobjective.getName());
            }
         }
      }

      return ISuggestionProvider.suggest(list, suggestions);
   }

   private static int getPlayerScore(CommandSource source, String player, ScoreObjective objective) throws CommandSyntaxException {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      if (!scoreboard.entityHasObjective(player, objective)) {
         throw SCOREBOARD_PLAYER_NOT_FOUND_EXCEPTION.create(objective.getName(), player);
      } else {
         Score score = scoreboard.getOrCreateScore(player, objective);
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.get.success", new Object[]{player, score.getScorePoints(), objective.func_197890_e()}), false);
         return score.getScorePoints();
      }
   }

   private static int applyScoreOperation(CommandSource source, Collection<String> targetEntities, ScoreObjective targetObjectives, OperationArgument.IOperation operation, Collection<String> sourceEntities, ScoreObjective sourceObjective) throws CommandSyntaxException {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      int i = 0;

      for(String s : targetEntities) {
         Score score = scoreboard.getOrCreateScore(s, targetObjectives);

         for(String s1 : sourceEntities) {
            Score score1 = scoreboard.getOrCreateScore(s1, sourceObjective);
            operation.apply(score, score1);
         }

         i += score.getScorePoints();
      }

      if (targetEntities.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.operation.success.single", new Object[]{targetObjectives.func_197890_e(), targetEntities.iterator().next(), i}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.operation.success.multiple", new Object[]{targetObjectives.func_197890_e(), targetEntities.size()}), true);
      }

      return i;
   }

   private static int enableTrigger(CommandSource source, Collection<String> targets, ScoreObjective objective) throws CommandSyntaxException {
      if (objective.getCriteria() != ScoreCriteria.TRIGGER) {
         throw ENABLE_TRIGGER_INVALID.create();
      } else {
         Scoreboard scoreboard = source.getServer().getScoreboard();
         int i = 0;

         for(String s : targets) {
            Score score = scoreboard.getOrCreateScore(s, objective);
            if (score.isLocked()) {
               score.setLocked(false);
               ++i;
            }
         }

         if (i == 0) {
            throw ENABLE_TRIGGER_FAILED.create();
         } else {
            if (targets.size() == 1) {
               source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.enable.success.single", new Object[]{objective.func_197890_e(), targets.iterator().next()}), true);
            } else {
               source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.enable.success.multiple", new Object[]{objective.func_197890_e(), targets.size()}), true);
            }

            return i;
         }
      }
   }

   private static int resetPlayerAllScores(CommandSource source, Collection<String> targets) {
      Scoreboard scoreboard = source.getServer().getScoreboard();

      for(String s : targets) {
         scoreboard.removeObjectiveFromEntity(s, (ScoreObjective)null);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.reset.all.single", new Object[]{targets.iterator().next()}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.reset.all.multiple", new Object[]{targets.size()}), true);
      }

      return targets.size();
   }

   private static int resetPlayerScore(CommandSource source, Collection<String> targets, ScoreObjective objective) {
      Scoreboard scoreboard = source.getServer().getScoreboard();

      for(String s : targets) {
         scoreboard.removeObjectiveFromEntity(s, objective);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.reset.specific.single", new Object[]{objective.func_197890_e(), targets.iterator().next()}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.reset.specific.multiple", new Object[]{objective.func_197890_e(), targets.size()}), true);
      }

      return targets.size();
   }

   private static int setPlayerScore(CommandSource source, Collection<String> targets, ScoreObjective objective, int newValue) {
      Scoreboard scoreboard = source.getServer().getScoreboard();

      for(String s : targets) {
         Score score = scoreboard.getOrCreateScore(s, objective);
         score.setScorePoints(newValue);
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.set.success.single", new Object[]{objective.func_197890_e(), targets.iterator().next(), newValue}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.set.success.multiple", new Object[]{objective.func_197890_e(), targets.size(), newValue}), true);
      }

      return newValue * targets.size();
   }

   private static int addToPlayerScore(CommandSource source, Collection<String> targets, ScoreObjective objective, int amount) {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      int i = 0;

      for(String s : targets) {
         Score score = scoreboard.getOrCreateScore(s, objective);
         score.setScorePoints(score.getScorePoints() + amount);
         i += score.getScorePoints();
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.add.success.single", new Object[]{amount, objective.func_197890_e(), targets.iterator().next(), i}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.add.success.multiple", new Object[]{amount, objective.func_197890_e(), targets.size()}), true);
      }

      return i;
   }

   private static int removeFromPlayerScore(CommandSource source, Collection<String> targets, ScoreObjective objective, int amount) {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      int i = 0;

      for(String s : targets) {
         Score score = scoreboard.getOrCreateScore(s, objective);
         score.setScorePoints(score.getScorePoints() - amount);
         i += score.getScorePoints();
      }

      if (targets.size() == 1) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.remove.success.single", new Object[]{amount, objective.func_197890_e(), targets.iterator().next(), i}), true);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.remove.success.multiple", new Object[]{amount, objective.func_197890_e(), targets.size()}), true);
      }

      return i;
   }

   private static int listPlayers(CommandSource source) {
      Collection<String> collection = source.getServer().getScoreboard().getObjectiveNames();
      if (collection.isEmpty()) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.list.empty", new Object[0]), false);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.list.success", new Object[]{collection.size(), TextComponentUtils.makeGreenSortedList(collection)}), false);
      }

      return collection.size();
   }

   private static int listPlayerScores(CommandSource source, String player) {
      Map<ScoreObjective, Score> map = source.getServer().getScoreboard().getObjectivesForEntity(player);
      if (map.isEmpty()) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.list.entity.empty", new Object[]{player}), false);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.list.entity.success", new Object[]{player, map.size()}), false);

         for(Entry<ScoreObjective, Score> entry : map.entrySet()) {
            source.sendFeedback(new TextComponentTranslation("commands.scoreboard.players.list.entity.entry", new Object[]{((ScoreObjective)entry.getKey()).func_197890_e(), ((Score)entry.getValue()).getScorePoints()}), false);
         }
      }

      return map.size();
   }

   private static int clearObjectiveDisplaySlot(CommandSource source, int slotId) throws CommandSyntaxException {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      if (scoreboard.getObjectiveInDisplaySlot(slotId) == null) {
         throw DISPLAY_ALREADY_CLEAR_EXCEPTION.create();
      } else {
         scoreboard.setObjectiveInDisplaySlot(slotId, (ScoreObjective)null);
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.display.cleared", new Object[]{Scoreboard.getDisplaySlotStrings()[slotId]}), true);
         return 0;
      }
   }

   private static int setObjectiveDisplaySlot(CommandSource source, int slotId, ScoreObjective objective) throws CommandSyntaxException {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      if (scoreboard.getObjectiveInDisplaySlot(slotId) == objective) {
         throw DISPLAY_ALREADY_SET_EXCEPTION.create();
      } else {
         scoreboard.setObjectiveInDisplaySlot(slotId, objective);
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.display.set", new Object[]{Scoreboard.getDisplaySlotStrings()[slotId], objective.getDisplayName()}), true);
         return 0;
      }
   }

   private static int setDisplayName(CommandSource source, ScoreObjective objective, ITextComponent displayName) {
      if (!objective.getDisplayName().equals(displayName)) {
         objective.setDisplayName(displayName);
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.modify.displayname", new Object[]{objective.getName(), objective.func_197890_e()}), true);
      }

      return 0;
   }

   private static int setRenderType(CommandSource source, ScoreObjective objective, ScoreCriteria.RenderType renderType) {
      if (objective.getRenderType() != renderType) {
         objective.setRenderType(renderType);
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.modify.rendertype", new Object[]{objective.func_197890_e()}), true);
      }

      return 0;
   }

   private static int removeObjective(CommandSource source, ScoreObjective objective) {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      scoreboard.removeObjective(objective);
      source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.remove.success", new Object[]{objective.func_197890_e()}), true);
      return scoreboard.getScoreObjectives().size();
   }

   private static int addObjective(CommandSource source, String name, ScoreCriteria criteria, ITextComponent displayName) throws CommandSyntaxException {
      Scoreboard scoreboard = source.getServer().getScoreboard();
      if (scoreboard.getObjective(name) != null) {
         throw OBJECTIVE_ALREADY_EXISTS_EXCEPTION.create();
      } else if (name.length() > 16) {
         throw ObjectiveArgument.OBJECTIVE_NAME_TOO_LONG.create(16);
      } else {
         scoreboard.addObjective(name, criteria, displayName, criteria.getRenderType());
         ScoreObjective scoreobjective = scoreboard.getObjective(name);
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.add.success", new Object[]{scoreobjective.func_197890_e()}), true);
         return scoreboard.getScoreObjectives().size();
      }
   }

   private static int listObjectives(CommandSource source) {
      Collection<ScoreObjective> collection = source.getServer().getScoreboard().getScoreObjectives();
      if (collection.isEmpty()) {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.list.empty", new Object[0]), false);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.scoreboard.objectives.list.success", new Object[]{collection.size(), TextComponentUtils.makeList(collection, ScoreObjective::func_197890_e)}), false);
      }

      return collection.size();
   }
}
