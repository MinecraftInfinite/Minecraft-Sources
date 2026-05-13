package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class TextureMap extends AbstractTexture implements ITickableTextureObject {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ResourceLocation LOCATION_BLOCKS_TEXTURE = new ResourceLocation("textures/atlas/blocks.png");
   private final List<TextureAtlasSprite> listAnimatedSprites = Lists.<TextureAtlasSprite>newArrayList();
   private final Set<ResourceLocation> sprites = Sets.<ResourceLocation>newHashSet();
   private final Map<ResourceLocation, TextureAtlasSprite> mapUploadedSprites = Maps.<ResourceLocation, TextureAtlasSprite>newHashMap();
   private final String basePath;
   private int mipmapLevels;
   private final TextureAtlasSprite missingImage = MissingTextureSprite.getSprite();

   public TextureMap(String basePathIn) {
      this.basePath = basePathIn;
   }

   public void loadTexture(IResourceManager manager) throws IOException {
   }

   public void stitch(IResourceManager manager, Iterable<ResourceLocation> locations) {
      this.sprites.clear();
      locations.forEach((p_195423_2_) -> {
         this.registerSprite(manager, p_195423_2_);
      });
      this.stitch(manager);
   }

   public void stitch(IResourceManager manager) {
      int i = Minecraft.getGLMaximumTextureSize();
      Stitcher stitcher = new Stitcher(i, i, 0, this.mipmapLevels);
      this.clear();
      int j = Integer.MAX_VALUE;
      int k = 1 << this.mipmapLevels;

      for(ResourceLocation resourcelocation : this.sprites) {
         if (!this.missingImage.getName().equals(resourcelocation)) {
            ResourceLocation resourcelocation1 = this.getSpritePath(resourcelocation);

            TextureAtlasSprite textureatlassprite;
            try (IResource iresource = manager.getResource(resourcelocation1)) {
               PngSizeInfo pngsizeinfo = new PngSizeInfo(iresource);
               AnimationMetadataSection animationmetadatasection = (AnimationMetadataSection)iresource.getMetadata(AnimationMetadataSection.SERIALIZER);
               textureatlassprite = new TextureAtlasSprite(resourcelocation, pngsizeinfo, animationmetadatasection);
            } catch (RuntimeException runtimeexception) {
               LOGGER.error("Unable to parse metadata from {} : {}", resourcelocation1, runtimeexception);
               continue;
            } catch (IOException ioexception) {
               LOGGER.error("Using missing texture, unable to load {} : {}", resourcelocation1, ioexception);
               continue;
            }

            j = Math.min(j, Math.min(textureatlassprite.getWidth(), textureatlassprite.getHeight()));
            int j1 = Math.min(Integer.lowestOneBit(textureatlassprite.getWidth()), Integer.lowestOneBit(textureatlassprite.getHeight()));
            if (j1 < k) {
               LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", resourcelocation1, textureatlassprite.getWidth(), textureatlassprite.getHeight(), MathHelper.log2(k), MathHelper.log2(j1));
               k = j1;
            }

            stitcher.addSprite(textureatlassprite);
         }
      }

      int l = Math.min(j, k);
      int i1 = MathHelper.log2(l);
      if (i1 < this.mipmapLevels) {
         LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", this.basePath, this.mipmapLevels, i1, l);
         this.mipmapLevels = i1;
      }

      this.missingImage.generateMipmaps(this.mipmapLevels);
      stitcher.addSprite(this.missingImage);

      try {
         stitcher.doStitch();
      } catch (StitcherException stitcherexception) {
         throw stitcherexception;
      }

      LOGGER.info("Created: {}x{} {}-atlas", stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), this.basePath);
      TextureUtil.allocateTextureImpl(this.getGlTextureId(), this.mipmapLevels, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());

      for(TextureAtlasSprite textureatlassprite1 : stitcher.getStichSlots()) {
         if (textureatlassprite1 == this.missingImage || this.loadSprite(manager, textureatlassprite1)) {
            this.mapUploadedSprites.put(textureatlassprite1.getName(), textureatlassprite1);

            try {
               textureatlassprite1.uploadMipmaps();
            } catch (Throwable throwable) {
               CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");
               CrashReportCategory crashreportcategory = crashreport.makeCategory("Texture being stitched together");
               crashreportcategory.addDetail("Atlas path", this.basePath);
               crashreportcategory.addDetail("Sprite", textureatlassprite1);
               throw new ReportedException(crashreport);
            }

            if (textureatlassprite1.hasAnimationMetadata()) {
               this.listAnimatedSprites.add(textureatlassprite1);
            }
         }
      }

   }

   private boolean loadSprite(IResourceManager manager, TextureAtlasSprite sprite) {
      ResourceLocation resourcelocation = this.getSpritePath(sprite.getName());
      IResource iresource = null;

      label62: {
         boolean flag;
         try {
            iresource = manager.getResource(resourcelocation);
            sprite.loadSpriteFrames(iresource, this.mipmapLevels + 1);
            break label62;
         } catch (RuntimeException runtimeexception) {
            LOGGER.error("Unable to parse metadata from {}", resourcelocation, runtimeexception);
            flag = false;
         } catch (IOException ioexception) {
            LOGGER.error("Using missing texture, unable to load {}", resourcelocation, ioexception);
            flag = false;
            return flag;
         } finally {
            IOUtils.closeQuietly((Closeable)iresource);
         }

         return flag;
      }

      try {
         sprite.generateMipmaps(this.mipmapLevels);
         return true;
      } catch (Throwable throwable) {
         CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Applying mipmap");
         CrashReportCategory crashreportcategory = crashreport.makeCategory("Sprite being mipmapped");
         crashreportcategory.addDetail("Sprite name", () -> {
            return sprite.getName().toString();
         });
         crashreportcategory.addDetail("Sprite size", () -> {
            return sprite.getWidth() + " x " + sprite.getHeight();
         });
         crashreportcategory.addDetail("Sprite frames", () -> {
            return sprite.getFrameCount() + " frames";
         });
         crashreportcategory.addDetail("Mipmap levels", this.mipmapLevels);
         throw new ReportedException(crashreport);
      }
   }

   private ResourceLocation getSpritePath(ResourceLocation location) {
      return new ResourceLocation(location.getNamespace(), String.format("%s/%s%s", this.basePath, location.getPath(), ".png"));
   }

   public TextureAtlasSprite getAtlasSprite(String iconName) {
      return this.getSprite(new ResourceLocation(iconName));
   }

   public void updateAnimations() {
      this.bindTexture();

      for(TextureAtlasSprite textureatlassprite : this.listAnimatedSprites) {
         textureatlassprite.updateAnimation();
      }

   }

   public void registerSprite(IResourceManager manager, ResourceLocation location) {
      if (location == null) {
         throw new IllegalArgumentException("Location cannot be null!");
      } else {
         this.sprites.add(location);
      }
   }

   public void tick() {
      this.updateAnimations();
   }

   public void setMipmapLevels(int mipmapLevelsIn) {
      this.mipmapLevels = mipmapLevelsIn;
   }

   public TextureAtlasSprite getSprite(ResourceLocation location) {
      TextureAtlasSprite textureatlassprite = this.mapUploadedSprites.get(location);
      return textureatlassprite == null ? this.missingImage : textureatlassprite;
   }

   public void clear() {
      for(TextureAtlasSprite textureatlassprite : this.mapUploadedSprites.values()) {
         textureatlassprite.clearFramesTextureData();
      }

      this.mapUploadedSprites.clear();
      this.listAnimatedSprites.clear();
   }
}
