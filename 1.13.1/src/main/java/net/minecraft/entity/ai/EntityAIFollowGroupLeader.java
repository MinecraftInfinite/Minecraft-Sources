package net.minecraft.entity.ai;

import java.util.List;
import net.minecraft.entity.passive.AbstractFish;

public class EntityAIFollowGroupLeader extends EntityAIBase {
   private final AbstractFish taskOwner;
   private AbstractFish leader;
   private int navigateTimer;

   public EntityAIFollowGroupLeader(AbstractFish p_i48858_1_) {
      this.taskOwner = p_i48858_1_;
   }

   public boolean shouldExecute() {
      if (!this.taskOwner.isLeading() && !this.taskOwner.isFollowing()) {
         List<AbstractFish> list = this.taskOwner.world.<AbstractFish>getEntitiesWithinAABB(this.taskOwner.getClass(), this.taskOwner.getBoundingBox().grow(8.0D, 8.0D, 8.0D));
         if (list.size() <= 1) {
            return false;
         } else {
            for(AbstractFish abstractfish : list) {
               if (abstractfish.canAcceptFollowers() && !abstractfish.equals(this.taskOwner)) {
                  ++abstractfish.groupSize;
                  this.leader = abstractfish;
                  return true;
               }
            }

            for(AbstractFish abstractfish1 : list) {
               if (!abstractfish1.equals(this.taskOwner) && !abstractfish1.isFollowing() && !abstractfish1.isLeading()) {
                  abstractfish1.setLeading(true);
                  ++abstractfish1.groupSize;
                  this.leader = abstractfish1;
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public boolean shouldContinueExecuting() {
      if (this.leader.isAlive() && this.leader.isLeading()) {
         double d0 = this.taskOwner.getDistanceSq(this.leader);
         return d0 <= 121.0D;
      } else {
         return false;
      }
   }

   public void startExecuting() {
      this.taskOwner.setFollowing(true);
      this.navigateTimer = 0;
   }

   public void resetTask() {
      this.taskOwner.setFollowing(false);
      --this.leader.groupSize;
      this.leader = null;
   }

   public void tick() {
      if (--this.navigateTimer <= 0) {
         this.navigateTimer = 10;
         this.taskOwner.getNavigator().tryMoveToEntityLiving(this.leader, 1.0D);
      }
   }
}
