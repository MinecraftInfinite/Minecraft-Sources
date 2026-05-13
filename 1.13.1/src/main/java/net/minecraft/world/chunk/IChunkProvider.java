package net.minecraft.world.chunk;

import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.world.gen.IChunkGenerator;

public interface IChunkProvider extends AutoCloseable {
   @Nullable
   Chunk getChunk(int x, int z, boolean p_186025_3_, boolean p_186025_4_);

   @Nullable
   default IChunk getChunkOrPrimer(int x, int z, boolean p_201713_3_) {
      Chunk chunk = this.getChunk(x, z, true, false);
      if (chunk == null && p_201713_3_) {
         throw new UnsupportedOperationException("Could not create an empty chunk");
      } else {
         return chunk;
      }
   }

   boolean tick(BooleanSupplier p_73156_1_);

   String makeString();

   IChunkGenerator<?> getChunkGenerator();

   default void close() {
   }
}
