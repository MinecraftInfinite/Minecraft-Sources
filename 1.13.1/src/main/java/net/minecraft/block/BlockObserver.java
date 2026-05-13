package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class BlockObserver extends BlockDirectional {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

   public BlockObserver(Block.Properties properties) {
      super(properties);
      this.setDefaultState((IBlockState)((IBlockState)(this.stateContainer.getBaseState()).with(FACING, EnumFacing.SOUTH)).with(POWERED, Boolean.valueOf(false)));
   }

   protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
      builder.add(FACING, POWERED);
   }

   public IBlockState rotate(IBlockState state, Rotation rot) {
      return (IBlockState)state.with(FACING, rot.rotate((EnumFacing)state.get(FACING)));
   }

   public IBlockState mirror(IBlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.toRotation((EnumFacing)state.get(FACING)));
   }

   public void tick(IBlockState state, World worldIn, BlockPos pos, Random random) {
      if (state.get(POWERED)) {
         worldIn.setBlockState(pos, (IBlockState)state.with(POWERED, Boolean.valueOf(false)), 2);
      } else {
         worldIn.setBlockState(pos, (IBlockState)state.with(POWERED, Boolean.valueOf(true)), 2);
         worldIn.getPendingBlockTicks().scheduleTick(pos, this, 2);
      }

      this.updateNeighborsInFront(worldIn, pos, state);
   }

   public IBlockState updatePostPlacement(IBlockState stateIn, EnumFacing facing, IBlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
      if (stateIn.get(FACING) == facing && !stateIn.get(POWERED)) {
         this.startSignal(worldIn, currentPos);
      }

      return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
   }

   private void startSignal(IWorld p_203420_1_, BlockPos p_203420_2_) {
      if (!p_203420_1_.isRemote() && !p_203420_1_.getPendingBlockTicks().isTickScheduled(p_203420_2_, this)) {
         p_203420_1_.getPendingBlockTicks().scheduleTick(p_203420_2_, this, 2);
      }

   }

   protected void updateNeighborsInFront(World worldIn, BlockPos pos, IBlockState state) {
      EnumFacing enumfacing = (EnumFacing)state.get(FACING);
      BlockPos blockpos = pos.offset(enumfacing.getOpposite());
      worldIn.neighborChanged(blockpos, this, pos);
      worldIn.notifyNeighborsOfStateExcept(blockpos, this, enumfacing);
   }

   public boolean canProvidePower(IBlockState state) {
      return true;
   }

   public int getStrongPower(IBlockState blockState, IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
      return blockState.getWeakPower(blockAccess, pos, side);
   }

   public int getWeakPower(IBlockState blockState, IBlockReader blockAccess, BlockPos pos, EnumFacing side) {
      return blockState.get(POWERED) && blockState.get(FACING) == side ? 15 : 0;
   }

   public void onBlockAdded(IBlockState state, World worldIn, BlockPos pos, IBlockState oldState) {
      if (state.getBlock() != oldState.getBlock()) {
         if (!worldIn.isRemote() && state.get(POWERED) && !worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
            IBlockState iblockstate = (IBlockState)state.with(POWERED, Boolean.valueOf(false));
            worldIn.setBlockState(pos, iblockstate, 18);
            this.updateNeighborsInFront(worldIn, pos, iblockstate);
         }

      }
   }

   public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
      if (state.getBlock() != newState.getBlock()) {
         if (!worldIn.isRemote && state.get(POWERED) && worldIn.getPendingBlockTicks().isTickScheduled(pos, this)) {
            this.updateNeighborsInFront(worldIn, pos, (IBlockState)state.with(POWERED, Boolean.valueOf(false)));
         }

      }
   }

   public IBlockState getStateForPlacement(BlockItemUseContext context) {
      return (IBlockState)this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite().getOpposite());
   }
}
