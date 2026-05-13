package net.minecraft.world.border;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class WorldBorder {
   private final List<IBorderListener> listeners = Lists.<IBorderListener>newArrayList();
   private double damagePerBlock = 0.2D;
   private double damageBuffer = 5.0D;
   private int warningTime = 15;
   private int warningDistance = 5;
   private double centerX;
   private double centerZ;
   private int worldSize = 29999984;
   private WorldBorder.IBorderInfo field_212674_i = new WorldBorder.StationaryBorderInfo(6.0E7D);

   public boolean contains(BlockPos pos) {
      return (double)(pos.getX() + 1) > this.minX() && (double)pos.getX() < this.maxX() && (double)(pos.getZ() + 1) > this.minZ() && (double)pos.getZ() < this.maxZ();
   }

   public boolean contains(ChunkPos range) {
      return (double)range.getXEnd() > this.minX() && (double)range.getXStart() < this.maxX() && (double)range.getZEnd() > this.minZ() && (double)range.getZStart() < this.maxZ();
   }

   public boolean contains(AxisAlignedBB bb) {
      return bb.maxX > this.minX() && bb.minX < this.maxX() && bb.maxZ > this.minZ() && bb.minZ < this.maxZ();
   }

   public double getClosestDistance(Entity entityIn) {
      return this.getClosestDistance(entityIn.posX, entityIn.posZ);
   }

   public double getClosestDistance(double x, double z) {
      double d0 = z - this.minZ();
      double d1 = this.maxZ() - z;
      double d2 = x - this.minX();
      double d3 = this.maxX() - x;
      double d4 = Math.min(d2, d3);
      d4 = Math.min(d4, d0);
      return Math.min(d4, d1);
   }

   @OnlyIn(Dist.CLIENT)
   public EnumBorderStatus getStatus() {
      return this.field_212674_i.getStatus();
   }

   public double minX() {
      return this.field_212674_i.getMinX();
   }

   public double minZ() {
      return this.field_212674_i.getMinZ();
   }

   public double maxX() {
      return this.field_212674_i.getMaxX();
   }

   public double maxZ() {
      return this.field_212674_i.getMaxZ();
   }

   public double getCenterX() {
      return this.centerX;
   }

   public double getCenterZ() {
      return this.centerZ;
   }

   public void setCenter(double x, double z) {
      this.centerX = x;
      this.centerZ = z;
      this.field_212674_i.func_212653_k();

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onCenterChanged(this, x, z);
      }

   }

   public double getDiameter() {
      return this.field_212674_i.getSize();
   }

   public long getTimeUntilTarget() {
      return this.field_212674_i.func_212657_g();
   }

   public double getTargetSize() {
      return this.field_212674_i.func_212650_h();
   }

   public void setTransition(double newSize) {
      this.field_212674_i = new WorldBorder.StationaryBorderInfo(newSize);

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onSizeChanged(this, newSize);
      }

   }

   public void setTransition(double oldSize, double newSize, long time) {
      this.field_212674_i = (WorldBorder.IBorderInfo)(oldSize != newSize ? new WorldBorder.MovingBorderInfo(oldSize, newSize, time) : new WorldBorder.StationaryBorderInfo(newSize));

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onTransitionStarted(this, oldSize, newSize, time);
      }

   }

   protected List<IBorderListener> getListeners() {
      return Lists.newArrayList(this.listeners);
   }

   public void addListener(IBorderListener listener) {
      this.listeners.add(listener);
   }

   public void setSize(int size) {
      this.worldSize = size;
      this.field_212674_i.func_212652_j();
   }

   public int getSize() {
      return this.worldSize;
   }

   public double getDamageBuffer() {
      return this.damageBuffer;
   }

   public void setDamageBuffer(double bufferSize) {
      this.damageBuffer = bufferSize;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onDamageBufferChanged(this, bufferSize);
      }

   }

   public double getDamagePerBlock() {
      return this.damagePerBlock;
   }

   public void setDamagePerBlock(double newAmount) {
      this.damagePerBlock = newAmount;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onDamageAmountChanged(this, newAmount);
      }

   }

   @OnlyIn(Dist.CLIENT)
   public double getResizeSpeed() {
      return this.field_212674_i.func_212649_f();
   }

   public int getWarningTime() {
      return this.warningTime;
   }

   public void setWarningTime(int warningTime) {
      this.warningTime = warningTime;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onWarningTimeChanged(this, warningTime);
      }

   }

   public int getWarningDistance() {
      return this.warningDistance;
   }

   public void setWarningDistance(int warningDistance) {
      this.warningDistance = warningDistance;

      for(IBorderListener iborderlistener : this.getListeners()) {
         iborderlistener.onWarningDistanceChanged(this, warningDistance);
      }

   }

   public void tick() {
      this.field_212674_i = this.field_212674_i.func_212651_l();
   }

   interface IBorderInfo {
      double getMinX();

      double getMaxX();

      double getMinZ();

      double getMaxZ();

      double getSize();

      @OnlyIn(Dist.CLIENT)
      double func_212649_f();

      long func_212657_g();

      double func_212650_h();

      @OnlyIn(Dist.CLIENT)
      EnumBorderStatus getStatus();

      void func_212652_j();

      void func_212653_k();

      WorldBorder.IBorderInfo func_212651_l();
   }

   class MovingBorderInfo implements WorldBorder.IBorderInfo {
      private final double field_212660_b;
      private final double field_212661_c;
      private final long field_212662_d;
      private final long field_212663_e;
      private final double field_212664_f;

      private MovingBorderInfo(double p_i49838_2_, double p_i49838_4_, long p_i49838_6_) {
         this.field_212660_b = p_i49838_2_;
         this.field_212661_c = p_i49838_4_;
         this.field_212664_f = (double)p_i49838_6_;
         this.field_212663_e = Util.milliTime();
         this.field_212662_d = this.field_212663_e + p_i49838_6_;
      }

      public double getMinX() {
         return Math.max(WorldBorder.this.getCenterX() - this.getSize() / 2.0D, (double)(-WorldBorder.this.worldSize));
      }

      public double getMinZ() {
         return Math.max(WorldBorder.this.getCenterZ() - this.getSize() / 2.0D, (double)(-WorldBorder.this.worldSize));
      }

      public double getMaxX() {
         return Math.min(WorldBorder.this.getCenterX() + this.getSize() / 2.0D, (double)WorldBorder.this.worldSize);
      }

      public double getMaxZ() {
         return Math.min(WorldBorder.this.getCenterZ() + this.getSize() / 2.0D, (double)WorldBorder.this.worldSize);
      }

      public double getSize() {
         double d0 = (double)(Util.milliTime() - this.field_212663_e) / this.field_212664_f;
         return d0 < 1.0D ? this.field_212660_b + (this.field_212661_c - this.field_212660_b) * d0 : this.field_212661_c;
      }

      @OnlyIn(Dist.CLIENT)
      public double func_212649_f() {
         return Math.abs(this.field_212660_b - this.field_212661_c) / (double)(this.field_212662_d - this.field_212663_e);
      }

      public long func_212657_g() {
         return this.field_212662_d - Util.milliTime();
      }

      public double func_212650_h() {
         return this.field_212661_c;
      }

      @OnlyIn(Dist.CLIENT)
      public EnumBorderStatus getStatus() {
         return this.field_212661_c < this.field_212660_b ? EnumBorderStatus.SHRINKING : EnumBorderStatus.GROWING;
      }

      public void func_212653_k() {
      }

      public void func_212652_j() {
      }

      public WorldBorder.IBorderInfo func_212651_l() {
         return (WorldBorder.IBorderInfo)(this.func_212657_g() <= 0L ? WorldBorder.this.new StationaryBorderInfo(this.field_212661_c) : this);
      }
   }

   class StationaryBorderInfo implements WorldBorder.IBorderInfo {
      private final double size;
      private double minX;
      private double minZ;
      private double maxX;
      private double maxZ;

      public StationaryBorderInfo(double p_i49837_2_) {
         this.size = p_i49837_2_;
         this.func_212665_m();
      }

      public double getMinX() {
         return this.minX;
      }

      public double getMaxX() {
         return this.maxX;
      }

      public double getMinZ() {
         return this.minZ;
      }

      public double getMaxZ() {
         return this.maxZ;
      }

      public double getSize() {
         return this.size;
      }

      @OnlyIn(Dist.CLIENT)
      public EnumBorderStatus getStatus() {
         return EnumBorderStatus.STATIONARY;
      }

      @OnlyIn(Dist.CLIENT)
      public double func_212649_f() {
         return 0.0D;
      }

      public long func_212657_g() {
         return 0L;
      }

      public double func_212650_h() {
         return this.size;
      }

      private void func_212665_m() {
         this.minX = Math.max(WorldBorder.this.getCenterX() - this.size / 2.0D, (double)(-WorldBorder.this.worldSize));
         this.minZ = Math.max(WorldBorder.this.getCenterZ() - this.size / 2.0D, (double)(-WorldBorder.this.worldSize));
         this.maxX = Math.min(WorldBorder.this.getCenterX() + this.size / 2.0D, (double)WorldBorder.this.worldSize);
         this.maxZ = Math.min(WorldBorder.this.getCenterZ() + this.size / 2.0D, (double)WorldBorder.this.worldSize);
      }

      public void func_212652_j() {
         this.func_212665_m();
      }

      public void func_212653_k() {
         this.func_212665_m();
      }

      public WorldBorder.IBorderInfo func_212651_l() {
         return this;
      }
   }
}
