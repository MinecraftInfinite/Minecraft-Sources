package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixTypes;
import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.IProgressUpdate;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveFormatOld implements ISaveFormat {
   private static final Logger LOGGER = LogManager.getLogger();
   protected final Path savesDirectory;
   protected final Path field_197717_b;
   protected final DataFixer dataFixer;

   public SaveFormatOld(Path p_i49565_1_, Path p_i49565_2_, DataFixer p_i49565_3_) {
      this.dataFixer = p_i49565_3_;

      try {
         Files.createDirectories(Files.exists(p_i49565_1_) ? p_i49565_1_.toRealPath() : p_i49565_1_);
      } catch (IOException ioexception) {
         throw new RuntimeException(ioexception);
      }

      this.savesDirectory = p_i49565_1_;
      this.field_197717_b = p_i49565_2_;
   }

   @OnlyIn(Dist.CLIENT)
   public String getName() {
      return "Old Format";
   }

   @OnlyIn(Dist.CLIENT)
   public List<WorldSummary> getSaveList() throws AnvilConverterException {
      List<WorldSummary> list = Lists.<WorldSummary>newArrayList();

      for(int i = 0; i < 5; ++i) {
         String s = "World" + (i + 1);
         WorldInfo worldinfo = this.getWorldInfo(s);
         if (worldinfo != null) {
            list.add(new WorldSummary(worldinfo, s, "", worldinfo.getSizeOnDisk(), false));
         }
      }

      return list;
   }

   @OnlyIn(Dist.CLIENT)
   public void flushCache() {
   }

   @Nullable
   public WorldInfo getWorldInfo(String saveName) {
      File file1 = new File(this.savesDirectory.toFile(), saveName);
      if (!file1.exists()) {
         return null;
      } else {
         File file2 = new File(file1, "level.dat");
         if (file2.exists()) {
            WorldInfo worldinfo = getWorldData(file2, this.dataFixer);
            if (worldinfo != null) {
               return worldinfo;
            }
         }

         file2 = new File(file1, "level.dat_old");
         return file2.exists() ? getWorldData(file2, this.dataFixer) : null;
      }
   }

   @Nullable
   public static WorldInfo getWorldData(File p_186353_0_, DataFixer dataFixerIn) {
      try {
         NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(p_186353_0_));
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Data");
         NBTTagCompound nbttagcompound2 = nbttagcompound1.contains("Player", 10) ? nbttagcompound1.getCompound("Player") : null;
         nbttagcompound1.remove("Player");
         int i = nbttagcompound1.contains("DataVersion", 99) ? nbttagcompound1.getInt("DataVersion") : -1;
         return new WorldInfo(NBTUtil.update(dataFixerIn, DataFixTypes.LEVEL, nbttagcompound1, i), dataFixerIn, i, nbttagcompound2);
      } catch (Exception exception) {
         LOGGER.error("Exception reading {}", p_186353_0_, exception);
         return null;
      }
   }

   @OnlyIn(Dist.CLIENT)
   public void renameWorld(String dirName, String newName) {
      File file1 = new File(this.savesDirectory.toFile(), dirName);
      if (file1.exists()) {
         File file2 = new File(file1, "level.dat");
         if (file2.exists()) {
            try {
               NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(new FileInputStream(file2));
               NBTTagCompound nbttagcompound1 = nbttagcompound.getCompound("Data");
               nbttagcompound1.putString("LevelName", newName);
               CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file2));
            } catch (Exception exception) {
               exception.printStackTrace();
            }
         }

      }
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isNewLevelIdAcceptable(String saveName) {
      File file1 = new File(this.savesDirectory.toFile(), saveName);
      if (file1.exists()) {
         return false;
      } else {
         try {
            file1.mkdir();
            file1.delete();
            return true;
         } catch (Throwable throwable) {
            LOGGER.warn("Couldn't make new level", throwable);
            return false;
         }
      }
   }

   @OnlyIn(Dist.CLIENT)
   public boolean deleteWorldDirectory(String saveName) {
      File file1 = new File(this.savesDirectory.toFile(), saveName);
      if (!file1.exists()) {
         return true;
      } else {
         LOGGER.info("Deleting level {}", (Object)saveName);

         for(int i = 1; i <= 5; ++i) {
            LOGGER.info("Attempt {}...", (int)i);
            if (deleteFiles(file1.listFiles())) {
               break;
            }

            LOGGER.warn("Unsuccessful in deleting contents.");
            if (i < 5) {
               try {
                  Thread.sleep(500L);
               } catch (InterruptedException var5) {
                  ;
               }
            }
         }

         return file1.delete();
      }
   }

   @OnlyIn(Dist.CLIENT)
   protected static boolean deleteFiles(File[] files) {
      for(File file1 : files) {
         LOGGER.debug("Deleting {}", (Object)file1);
         if (file1.isDirectory() && !deleteFiles(file1.listFiles())) {
            LOGGER.warn("Couldn't delete directory {}", (Object)file1);
            return false;
         }

         if (!file1.delete()) {
            LOGGER.warn("Couldn't delete file {}", (Object)file1);
            return false;
         }
      }

      return true;
   }

   public ISaveHandler getSaveLoader(String saveName, @Nullable MinecraftServer server) {
      return new SaveHandler(this.savesDirectory.toFile(), saveName, server, this.dataFixer);
   }

   @OnlyIn(Dist.CLIENT)
   public boolean isConvertible(String saveName) {
      return false;
   }

   public boolean isOldMapFormat(String saveName) {
      return false;
   }

   public boolean convertMapFormat(String filename, IProgressUpdate progressCallback) {
      return false;
   }

   @OnlyIn(Dist.CLIENT)
   public boolean canLoadWorld(String saveName) {
      return Files.isDirectory(this.savesDirectory.resolve(saveName));
   }

   public File getFile(String saveName, String filePath) {
      return this.savesDirectory.resolve(saveName).resolve(filePath).toFile();
   }

   @OnlyIn(Dist.CLIENT)
   public Path getWorldFolder(String saveName) {
      return this.savesDirectory.resolve(saveName);
   }

   @OnlyIn(Dist.CLIENT)
   public Path getBackupsFolder() {
      return this.field_197717_b;
   }
}
