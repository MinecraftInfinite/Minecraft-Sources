package net.minecraft.entity.passive;

import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAIWanderSwim;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateSwimmer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public abstract class AbstractFish extends EntityWaterMob implements IAnimal {
   private static final DataParameter<Boolean> FROM_BUCKET = EntityDataManager.<Boolean>createKey(AbstractFish.class, DataSerializers.BOOLEAN);
   private boolean field_203712_c;
   public int groupSize;
   private boolean following;

   public AbstractFish(EntityType<?> type, World p_i48855_2_) {
      super(type, p_i48855_2_);
      this.moveHelper = new AbstractFish.MoveHelper(this);
      this.field_203712_c = false;
      this.groupSize = 0;
      this.following = false;
   }

   public float getEyeHeight() {
      return this.height * 0.65F;
   }

   protected void registerAttributes() {
      super.registerAttributes();
      this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(3.0D);
   }

   public boolean isNoDespawnRequired() {
      return this.isFromBucket() || super.isNoDespawnRequired();
   }

   public boolean canSpawn(IWorld worldIn, boolean p_205020_2_) {
      BlockPos blockpos = new BlockPos(this);
      return worldIn.getBlockState(blockpos).getBlock() == Blocks.WATER && worldIn.getBlockState(blockpos.up()).getBlock() == Blocks.WATER ? super.canSpawn(worldIn, p_205020_2_) : false;
   }

   protected boolean canDespawn() {
      return !this.isFromBucket() && !this.hasCustomName();
   }

   public boolean canAcceptFollowers() {
      return this.field_203712_c && this.groupSize < this.getMaxGroupSize();
   }

   protected int getMaxGroupSize() {
      return 8;
   }

   public int getMaxSpawnedInChunk() {
      return this.getMaxGroupSize();
   }

   public void setFollowing(boolean p_203700_1_) {
      this.following = p_203700_1_;
   }

   public boolean isFollowing() {
      return this.following;
   }

   protected void registerData() {
      super.registerData();
      this.dataManager.register(FROM_BUCKET, false);
   }

   private boolean isFromBucket() {
      return this.dataManager.get(FROM_BUCKET);
   }

   public void setFromBucket(boolean p_203706_1_) {
      this.dataManager.set(FROM_BUCKET, p_203706_1_);
   }

   public void writeAdditional(NBTTagCompound compound) {
      super.writeAdditional(compound);
      compound.putBoolean("FromBucket", this.isFromBucket());
   }

   public void readAdditional(NBTTagCompound compound) {
      super.readAdditional(compound);
      this.setFromBucket(compound.getBoolean("FromBucket"));
   }

   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
      this.tasks.addTask(2, new EntityAIAvoidEntity(this, EntityPlayer.class, 8.0F, 1.6D, 1.4D, EntitySelectors.NOT_SPECTATING));
      this.tasks.addTask(4, new AbstractFish.AISwim(this));
   }

   protected PathNavigate createNavigator(World worldIn) {
      return new PathNavigateSwimmer(this, worldIn);
   }

   public void tick() {
      super.tick();
      if (this.field_203712_c && this.world.rand.nextInt(200) == 1) {
         List<AbstractFish> list = this.world.<AbstractFish>getEntitiesWithinAABB(this.getClass(), this.getBoundingBox().grow(8.0D, 8.0D, 8.0D));
         if (list.size() <= 1) {
            this.setLeading(false);
            this.groupSize = 0;
         }
      }

   }

   public void travel(float strafe, float vertical, float forward) {
      if (this.isServerWorld() && this.isInWater()) {
         this.moveRelative(strafe, vertical, forward, 0.01F);
         this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         this.motionX *= (double)0.9F;
         this.motionY *= (double)0.9F;
         this.motionZ *= (double)0.9F;
         if (this.getAttackTarget() == null) {
            this.motionY -= 0.005D;
         }
      } else {
         super.travel(strafe, vertical, forward);
      }

   }

   public void livingTick() {
      if (!this.isInWater() && this.onGround && this.collidedVertically) {
         this.motionY += (double)0.4F;
         this.motionX += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.05F);
         this.motionZ += (double)((this.rand.nextFloat() * 2.0F - 1.0F) * 0.05F);
         this.onGround = false;
         this.isAirBorne = true;
         this.playSound(this.getFlopSound(), this.getSoundVolume(), this.getSoundPitch());
      }

      super.livingTick();
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      ItemStack itemstack = player.getHeldItem(hand);
      if (itemstack.getItem() == Items.WATER_BUCKET && this.isAlive()) {
         this.playSound(SoundEvents.ITEM_BUCKET_FILL_FISH, 1.0F, 1.0F);
         itemstack.shrink(1);
         ItemStack itemstack1 = this.getFishBucket();
         this.setBucketData(itemstack1);
         if (!this.world.isRemote) {
            CriteriaTriggers.FILLED_BUCKET.func_204817_a((EntityPlayerMP)player, itemstack1);
         }

         if (itemstack.isEmpty()) {
            player.setHeldItem(hand, itemstack1);
         } else if (!player.inventory.addItemStackToInventory(itemstack1)) {
            player.dropItem(itemstack1, false);
         }

         this.remove();
         return true;
      } else {
         return super.processInteract(player, hand);
      }
   }

   protected void setBucketData(ItemStack bucket) {
      if (this.hasCustomName()) {
         bucket.setDisplayName(this.getCustomName());
      }

   }

   protected abstract ItemStack getFishBucket();

   public void setLeading(boolean p_203702_1_) {
      this.field_203712_c = p_203702_1_;
   }

   public boolean isLeading() {
      return this.field_203712_c;
   }

   protected abstract SoundEvent getFlopSound();

   protected SoundEvent getSwimSound() {
      return SoundEvents.ENTITY_FISH_SWIM;
   }

   static class AISwim extends EntityAIWanderSwim {
      private final AbstractFish fish;

      public AISwim(AbstractFish p_i48856_1_) {
         super(p_i48856_1_, 1.0D, 40);
         this.fish = p_i48856_1_;
      }

      public boolean shouldExecute() {
         return super.shouldExecute() && !this.fish.isFollowing();
      }
   }

   static class MoveHelper extends EntityMoveHelper {
      private final AbstractFish fish;

      MoveHelper(AbstractFish p_i48857_1_) {
         super(p_i48857_1_);
         this.fish = p_i48857_1_;
      }

      public void tick() {
         if (this.fish.areEyesInFluid(FluidTags.WATER)) {
            this.fish.motionY += 0.005D;
         }

         if (this.action == EntityMoveHelper.Action.MOVE_TO && !this.fish.getNavigator().noPath()) {
            double d0 = this.posX - this.fish.posX;
            double d1 = this.posY - this.fish.posY;
            double d2 = this.posZ - this.fish.posZ;
            double d3 = (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
            d1 = d1 / d3;
            float f = (float)(MathHelper.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
            this.fish.rotationYaw = this.limitAngle(this.fish.rotationYaw, f, 90.0F);
            this.fish.renderYawOffset = this.fish.rotationYaw;
            float f1 = (float)(this.speed * this.fish.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getValue());
            this.fish.setAIMoveSpeed(this.fish.getAIMoveSpeed() + (f1 - this.fish.getAIMoveSpeed()) * 0.125F);
            this.fish.motionY += (double)this.fish.getAIMoveSpeed() * d1 * 0.1D;
         } else {
            this.fish.setAIMoveSpeed(0.0F);
         }
      }
   }
}
