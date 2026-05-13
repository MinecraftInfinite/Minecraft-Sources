package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.Queue;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Fluids;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSponge extends Block {
   protected BlockSponge(Block.Properties properties) {
      super(properties);
   }

   public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
      if (oldState.getBlock() != state.getBlock()) {
         this.tryAbsorb(worldIn, pos);
      }
   }

   public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
      this.tryAbsorb(worldIn, pos);
      super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
   }

   protected void tryAbsorb(World p_196510_1_, BlockPos p_196510_2_) {
      if (this.absorb(p_196510_1_, p_196510_2_)) {
         p_196510_1_.setBlockState(p_196510_2_, Blocks.WET_SPONGE.getDefaultState(), 2);
         p_196510_1_.playEvent(2001, p_196510_2_, Block.getStateId(Blocks.WATER.getDefaultState()));
      }

   }

   private boolean absorb(World worldIn, BlockPos pos) {
      Queue<Tuple<BlockPos, Integer>> queue = Lists.<Tuple<BlockPos, Integer>>newLinkedList();
      queue.add(new Tuple<BlockPos, Integer>(pos, 0));
      int i = 0;

      while(!queue.isEmpty()) {
         Tuple<BlockPos, Integer> tuple = queue.poll();
         BlockPos blockpos = tuple.getA();
         int j = tuple.getB();

         for(EnumFacing enumfacing : EnumFacing.values()) {
            BlockPos blockpos1 = blockpos.offset(enumfacing);
            IBlockState iblockstate = worldIn.getBlockState(blockpos1);
            IFluidState ifluidstate = worldIn.getFluidState(blockpos1);
            Material material = iblockstate.getMaterial();
            if (ifluidstate.isTagged(FluidTags.WATER)) {
               if (iblockstate.getBlock() instanceof IBucketPickupHandler && ((IBucketPickupHandler)iblockstate.getBlock()).pickupFluid(worldIn, blockpos1, iblockstate) != Fluids.EMPTY) {
                  ++i;
                  if (j < 6) {
                     queue.add(new Tuple<BlockPos, Integer>(blockpos1, j + 1));
                  }
               } else if (iblockstate.getBlock() instanceof BlockFlowingFluid) {
                  worldIn.setBlockState(blockpos1, Blocks.AIR.getDefaultState(), 3);
                  ++i;
                  if (j < 6) {
                     queue.add(new Tuple<BlockPos, Integer>(blockpos1, j + 1));
                  }
               } else if (material == Material.OCEAN_PLANT || material == Material.SEA_GRASS) {
                  iblockstate.dropBlockAsItem(worldIn, blockpos1, 0);
                  worldIn.setBlockState(blockpos1, Blocks.AIR.getDefaultState(), 3);
                  ++i;
                  if (j < 6) {
                     queue.add(new Tuple<BlockPos, Integer>(blockpos1, j + 1));
                  }
               }
            }
         }

         if (i > 64) {
            break;
         }
      }

      return i > 0;
   }
}
