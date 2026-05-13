package net.minecraft.world.gen.feature;

import net.minecraft.block.Block;

public class BlockBlobConfig implements IFeatureConfig {
   public final Block block;
   public final int field_202464_b;

   public BlockBlobConfig(Block block, int p_i48690_2_) {
      this.block = block;
      this.field_202464_b = p_i48690_2_;
   }
}
