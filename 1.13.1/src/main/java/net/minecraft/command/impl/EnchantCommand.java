package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EnchantmentArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;

public class EnchantCommand {
   private static final DynamicCommandExceptionType NONLIVING_ENTITY_EXCEPTION = new DynamicCommandExceptionType((p_208839_0_) -> {
      return new TextComponentTranslation("commands.enchant.failed.entity", new Object[]{p_208839_0_});
   });
   private static final DynamicCommandExceptionType ITEMLESS_EXCEPTION = new DynamicCommandExceptionType((p_208835_0_) -> {
      return new TextComponentTranslation("commands.enchant.failed.itemless", new Object[]{p_208835_0_});
   });
   private static final DynamicCommandExceptionType INCOMPATIBLE_ENCHANTS_EXCEPTION = new DynamicCommandExceptionType((p_208837_0_) -> {
      return new TextComponentTranslation("commands.enchant.failed.incompatible", new Object[]{p_208837_0_});
   });
   private static final Dynamic2CommandExceptionType INVALID_LEVEL = new Dynamic2CommandExceptionType((p_208840_0_, p_208840_1_) -> {
      return new TextComponentTranslation("commands.enchant.failed.level", new Object[]{p_208840_0_, p_208840_1_});
   });
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.enchant.failed", new Object[0]));

   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("enchant").requires((p_203630_0_) -> {
         return p_203630_0_.hasPermissionLevel(2);
      })).then(Commands.argument("targets", EntityArgument.entities()).then(((RequiredArgumentBuilder)Commands.argument("enchantment", EnchantmentArgument.enchantment()).executes((p_202648_0_) -> {
         return enchant((CommandSource)p_202648_0_.getSource(), EntityArgument.getEntities(p_202648_0_, "targets"), EnchantmentArgument.getEnchantment(p_202648_0_, "enchantment"), 1);
      })).then(Commands.argument("level", IntegerArgumentType.integer(0)).executes((p_202650_0_) -> {
         return enchant((CommandSource)p_202650_0_.getSource(), EntityArgument.getEntities(p_202650_0_, "targets"), EnchantmentArgument.getEnchantment(p_202650_0_, "enchantment"), IntegerArgumentType.getInteger(p_202650_0_, "level"));
      })))));
   }

   private static int enchant(CommandSource source, Collection<? extends Entity> targets, Enchantment enchantmentIn, int level) throws CommandSyntaxException {
      if (level > enchantmentIn.getMaxLevel()) {
         throw INVALID_LEVEL.create(level, enchantmentIn.getMaxLevel());
      } else {
         int i = 0;

         for(Entity entity : targets) {
            if (entity instanceof EntityLivingBase) {
               EntityLivingBase entitylivingbase = (EntityLivingBase)entity;
               ItemStack itemstack = entitylivingbase.getHeldItemMainhand();
               if (!itemstack.isEmpty()) {
                  if (enchantmentIn.canApply(itemstack) && EnchantmentHelper.areAllCompatibleWith(EnchantmentHelper.getEnchantments(itemstack).keySet(), enchantmentIn)) {
                     itemstack.addEnchantment(enchantmentIn, level);
                     ++i;
                  } else if (targets.size() == 1) {
                     throw INCOMPATIBLE_ENCHANTS_EXCEPTION.create(itemstack.getItem().getDisplayName(itemstack).getString());
                  }
               } else if (targets.size() == 1) {
                  throw ITEMLESS_EXCEPTION.create(entitylivingbase.getName().getString());
               }
            } else if (targets.size() == 1) {
               throw NONLIVING_ENTITY_EXCEPTION.create(entity.getName().getString());
            }
         }

         if (i == 0) {
            throw FAILED_EXCEPTION.create();
         } else {
            if (targets.size() == 1) {
               source.sendFeedback(new TextComponentTranslation("commands.enchant.success.single", new Object[]{enchantmentIn.func_200305_d(level), ((Entity)targets.iterator().next()).getDisplayName()}), true);
            } else {
               source.sendFeedback(new TextComponentTranslation("commands.enchant.success.multiple", new Object[]{enchantmentIn.func_200305_d(level), targets.size()}), true);
            }

            return i;
         }
      }
   }
}
