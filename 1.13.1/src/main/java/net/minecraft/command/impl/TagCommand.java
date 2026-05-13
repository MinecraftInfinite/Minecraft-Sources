package net.minecraft.command.impl;

import com.google.common.collect.Sets;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Set;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextComponentUtils;

public class TagCommand {
   private static final SimpleCommandExceptionType ADD_FAILED = new SimpleCommandExceptionType(new TextComponentTranslation("commands.tag.add.failed", new Object[0]));
   private static final SimpleCommandExceptionType REMOVE_FAILED = new SimpleCommandExceptionType(new TextComponentTranslation("commands.tag.remove.failed", new Object[0]));

   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("tag").requires((p_198751_0_) -> {
         return p_198751_0_.hasPermissionLevel(2);
      })).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).then(Commands.literal("add").then(Commands.argument("name", StringArgumentType.word()).executes((p_198746_0_) -> {
         return addTag((CommandSource)p_198746_0_.getSource(), EntityArgument.getEntities(p_198746_0_, "targets"), StringArgumentType.getString(p_198746_0_, "name"));
      })))).then(Commands.literal("remove").then(Commands.argument("name", StringArgumentType.word()).suggests((p_198745_0_, p_198745_1_) -> {
         return ISuggestionProvider.suggest(getAllTags(EntityArgument.getEntities(p_198745_0_, "targets")), p_198745_1_);
      }).executes((p_198742_0_) -> {
         return removeTag((CommandSource)p_198742_0_.getSource(), EntityArgument.getEntities(p_198742_0_, "targets"), StringArgumentType.getString(p_198742_0_, "name"));
      })))).then(Commands.literal("list").executes((p_198747_0_) -> {
         return listTags((CommandSource)p_198747_0_.getSource(), EntityArgument.getEntities(p_198747_0_, "targets"));
      }))));
   }

   private static Collection<String> getAllTags(Collection<? extends Entity> entities) {
      Set<String> set = Sets.<String>newHashSet();

      for(Entity entity : entities) {
         set.addAll(entity.getTags());
      }

      return set;
   }

   private static int addTag(CommandSource source, Collection<? extends Entity> entities, String tagName) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : entities) {
         if (entity.addTag(tagName)) {
            ++i;
         }
      }

      if (i == 0) {
         throw ADD_FAILED.create();
      } else {
         if (entities.size() == 1) {
            source.sendFeedback(new TextComponentTranslation("commands.tag.add.success.single", new Object[]{tagName, ((Entity)entities.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TextComponentTranslation("commands.tag.add.success.multiple", new Object[]{tagName, entities.size()}), true);
         }

         return i;
      }
   }

   private static int removeTag(CommandSource source, Collection<? extends Entity> entities, String tagName) throws CommandSyntaxException {
      int i = 0;

      for(Entity entity : entities) {
         if (entity.removeTag(tagName)) {
            ++i;
         }
      }

      if (i == 0) {
         throw REMOVE_FAILED.create();
      } else {
         if (entities.size() == 1) {
            source.sendFeedback(new TextComponentTranslation("commands.tag.remove.success.single", new Object[]{tagName, ((Entity)entities.iterator().next()).getDisplayName()}), true);
         } else {
            source.sendFeedback(new TextComponentTranslation("commands.tag.remove.success.multiple", new Object[]{tagName, entities.size()}), true);
         }

         return i;
      }
   }

   private static int listTags(CommandSource source, Collection<? extends Entity> entities) {
      Set<String> set = Sets.<String>newHashSet();

      for(Entity entity : entities) {
         set.addAll(entity.getTags());
      }

      if (entities.size() == 1) {
         Entity entity1 = entities.iterator().next();
         if (set.isEmpty()) {
            source.sendFeedback(new TextComponentTranslation("commands.tag.list.single.empty", new Object[]{entity1.getDisplayName()}), false);
         } else {
            source.sendFeedback(new TextComponentTranslation("commands.tag.list.single.success", new Object[]{entity1.getDisplayName(), set.size(), TextComponentUtils.makeGreenSortedList(set)}), false);
         }
      } else if (set.isEmpty()) {
         source.sendFeedback(new TextComponentTranslation("commands.tag.list.multiple.empty", new Object[]{entities.size()}), false);
      } else {
         source.sendFeedback(new TextComponentTranslation("commands.tag.list.multiple.success", new Object[]{entities.size(), set.size(), TextComponentUtils.makeGreenSortedList(set)}), false);
      }

      return set.size();
   }
}
