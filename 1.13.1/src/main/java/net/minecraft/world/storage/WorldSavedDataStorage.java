package net.minecraft.world.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.dimension.DimensionType;

public class WorldSavedDataStorage {
   private final Map<DimensionType, DimensionSavedDataManager> field_212427_a;
   @Nullable
   private final ISaveHandler saveHandler;

   public WorldSavedDataStorage(@Nullable ISaveHandler saveHandlerIn) {
      this.saveHandler = saveHandlerIn;
      Builder<DimensionType, DimensionSavedDataManager> builder = ImmutableMap.<DimensionType, DimensionSavedDataManager>builder();

      for(DimensionType dimensiontype : DimensionType.func_212681_b()) {
         DimensionSavedDataManager dimensionsaveddatamanager = new DimensionSavedDataManager(dimensiontype, saveHandlerIn);
         builder.put(dimensiontype, dimensionsaveddatamanager);
         dimensionsaveddatamanager.loadIdCounts();
      }

      this.field_212427_a = builder.build();
   }

   @Nullable
   public <T extends WorldSavedData> T get(DimensionType p_212426_1_, Function<String, T> p_212426_2_, String p_212426_3_) {
      return (T)((DimensionSavedDataManager)this.field_212427_a.get(p_212426_1_)).getOrLoadData(p_212426_2_, p_212426_3_);
   }

   public void set(DimensionType p_212424_1_, String p_212424_2_, WorldSavedData p_212424_3_) {
      ((DimensionSavedDataManager)this.field_212427_a.get(p_212424_1_)).setData(p_212424_2_, p_212424_3_);
   }

   public void save() {
      this.field_212427_a.values().forEach(DimensionSavedDataManager::save);
   }

   public int func_212425_a(DimensionType p_212425_1_, String p_212425_2_) {
      return ((DimensionSavedDataManager)this.field_212427_a.get(p_212425_1_)).getUniqueDataId(p_212425_2_);
   }

   public NBTTagCompound func_208028_a(String p_208028_1_, int p_208028_2_) throws IOException {
      return DimensionSavedDataManager.func_212774_a(this.saveHandler, DimensionType.OVERWORLD, p_208028_1_, p_208028_2_);
   }
}
