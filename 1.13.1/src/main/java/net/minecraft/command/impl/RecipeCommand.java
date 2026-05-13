package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.TextComponentTranslation;

public class RecipeCommand {
   private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.recipe.give.failed", new Object[0]));
   private static final SimpleCommandExceptionType TAKE_FAILED_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.recipe.take.failed", new Object[0]));

   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("recipe").requires((p_198593_0_) -> {
         return p_198593_0_.hasPermissionLevel(2);
      })).then(Commands.literal("give").then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceLocationArgument.resourceLocation()).suggests(SuggestionProviders.ALL_RECIPES).executes((p_198588_0_) -> {
         return giveRecipes((CommandSource)p_198588_0_.getSource(), EntityArgument.getPlayers(p_198588_0_, "targets"), Collections.singleton(ResourceLocationArgument.getRecipe(p_198588_0_, "recipe")));
      }))).then(Commands.literal("*").executes((p_198591_0_) -> {
         return giveRecipes((CommandSource)p_198591_0_.getSource(), EntityArgument.getPlayers(p_198591_0_, "targets"), ((CommandSource)p_198591_0_.getSource()).getServer().getRecipeManager().getRecipes());
      }))))).then(Commands.literal("take").then(((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).then(Commands.argument("recipe", ResourceLocationArgument.resourceLocation()).suggests(SuggestionProviders.ALL_RECIPES).executes((p_198587_0_) -> {
         return takeRecipes((CommandSource)p_198587_0_.getSource(), EntityArgument.getPlayers(p_198587_0_, "targets"), Collections.singleton(ResourceLocationArgument.getRecipe(p_198587_0_, "recipe")));
      }))).then(Commands.literal("*").executes((p_198592_0_) -> {
         return takeRecipes((CommandSource)p_198592_0_.getSource(), EntityArgument.getPlayers(p_198592_0_, "targets"), ((CommandSource)p_198592_0_.getSource()).getServer().getRecipeManager().getRecipes());
      })))));
   }

   private static int giveRecipes(CommandSource source, Collection<EntityPlayerMP> targets, Collection<IRecipe> recipes) throws CommandSyntaxException {
      int i = 0;

      for(EntityPlayerMP entityplayermp : targets) {
         i += entityplayermp.unlockRecipes(recipes);
      }

      if (i == 0) {
         throw GIVE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TextComponentTranslation("commands.recipe.give.success.single", new Object[]{recipes.size(), ((EntityPlayerMP)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TextComponentTranslation("commands.recipe.give.success.multiple", new Object[]{recipes.size(), targets.size()}), true);
         }

         return i;
      }
   }

   private static int takeRecipes(CommandSource source, Collection<EntityPlayerMP> targets, Collection<IRecipe> recipes) throws CommandSyntaxException {
      int i = 0;

      for(EntityPlayerMP entityplayermp : targets) {
         i += entityplayermp.resetRecipes(recipes);
      }

      if (i == 0) {
         throw TAKE_FAILED_EXCEPTION.create();
      } else {
         if (targets.size() == 1) {
            source.sendFeedback(new TextComponentTranslation("commands.recipe.take.success.single", new Object[]{recipes.size(), ((EntityPlayerMP)targets.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TextComponentTranslation("commands.recipe.take.success.multiple", new Object[]{recipes.size(), targets.size()}), true);
         }

         return i;
      }
   }
}
