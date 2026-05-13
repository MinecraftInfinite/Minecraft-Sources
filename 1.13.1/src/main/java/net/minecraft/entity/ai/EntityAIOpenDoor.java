package net.minecraft.entity.ai;

import net.minecraft.entity.EntityLiving;

public class EntityAIOpenDoor extends EntityAIDoorInteract {
   private final boolean closeDoor;
   private int closeDoorTemporisation;

   public EntityAIOpenDoor(EntityLiving entitylivingIn, boolean shouldClose) {
      super(entitylivingIn);
      this.entity = entitylivingIn;
      this.closeDoor = shouldClose;
   }

   public boolean shouldContinueExecuting() {
      return this.closeDoor && this.closeDoorTemporisation > 0 && super.shouldContinueExecuting();
   }

   public void startExecuting() {
      this.closeDoorTemporisation = 20;
      this.func_195921_a(true);
   }

   public void resetTask() {
      this.func_195921_a(false);
   }

   public void tick() {
      --this.closeDoorTemporisation;
      super.tick();
   }
}
