package net.minecraft.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;

public class SetBlockCommand {
   private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TextComponentTranslation("commands.setblock.failed", new Object[0]));

   public static void register(CommandDispatcher<CommandSource> dispatcher) {
      dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setblock").requires((p_198688_0_) -> {
         return p_198688_0_.hasPermissionLevel(2);
      })).then(Commands.argument("pos", BlockPosArgument.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("block", BlockStateArgument.blockState()).executes((p_198682_0_) -> {
         return setBlock((CommandSource)p_198682_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198682_0_, "pos"), BlockStateArgument.getBlockState(p_198682_0_, "block"), SetBlockCommand.Mode.REPLACE, (Predicate)null);
      })).then(Commands.literal("destroy").executes((p_198685_0_) -> {
         return setBlock((CommandSource)p_198685_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198685_0_, "pos"), BlockStateArgument.getBlockState(p_198685_0_, "block"), SetBlockCommand.Mode.DESTROY, (Predicate)null);
      }))).then(Commands.literal("keep").executes((p_198681_0_) -> {
         return setBlock((CommandSource)p_198681_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198681_0_, "pos"), BlockStateArgument.getBlockState(p_198681_0_, "block"), SetBlockCommand.Mode.REPLACE, (p_198687_0_) -> {
            return p_198687_0_.getWorld().isAirBlock(p_198687_0_.getPos());
         });
      }))).then(Commands.literal("replace").executes((p_198686_0_) -> {
         return setBlock((CommandSource)p_198686_0_.getSource(), BlockPosArgument.getLoadedBlockPos(p_198686_0_, "pos"), BlockStateArgument.getBlockState(p_198686_0_, "block"), SetBlockCommand.Mode.REPLACE, (Predicate)null);
      })))));
   }

   private static int setBlock(CommandSource source, BlockPos pos, BlockStateInput state, SetBlockCommand.Mode mode, @Nullable Predicate<BlockWorldState> predicate) throws CommandSyntaxException {
      WorldServer worldserver = source.getWorld();
      if (predicate != null && !predicate.test(new BlockWorldState(worldserver, pos, true))) {
         throw FAILED_EXCEPTION.create();
      } else {
         boolean flag;
         if (mode == SetBlockCommand.Mode.DESTROY) {
            worldserver.destroyBlock(pos, true);
            flag = !state.getState().isAir();
         } else {
            TileEntity tileentity = worldserver.getTileEntity(pos);
            if (tileentity instanceof IInventory) {
               ((IInventory)tileentity).clear();
            }

            flag = true;
         }

         if (flag && !state.place(worldserver, pos, 2)) {
            throw FAILED_EXCEPTION.create();
         } else {
            worldserver.notifyNeighbors(pos, state.getState().getBlock());
            source.sendFeedback(new TextComponentTranslation("commands.setblock.success", new Object[]{pos.getX(), pos.getY(), pos.getZ()}), true);
            return 1;
         }
      }
   }

   public interface IFilter {
      @Nullable
      BlockStateInput filter(MutableBoundingBox p_filter_1_, BlockPos p_filter_2_, BlockStateInput p_filter_3_, WorldServer p_filter_4_);
   }

   public static enum Mode {
      REPLACE,
      OUTLINE,
      HOLLOW,
      DESTROY;
   }
}
