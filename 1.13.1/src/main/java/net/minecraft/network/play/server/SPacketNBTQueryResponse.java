package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SPacketNBTQueryResponse implements Packet<INetHandlerPlayClient> {
   private int field_211714_a;
   @Nullable
   private NBTTagCompound tag;

   public SPacketNBTQueryResponse() {
   }

   public SPacketNBTQueryResponse(int p_i49757_1_, @Nullable NBTTagCompound p_i49757_2_) {
      this.field_211714_a = p_i49757_1_;
      this.tag = p_i49757_2_;
   }

   public void readPacketData(PacketBuffer buf) throws IOException {
      this.field_211714_a = buf.readVarInt();
      this.tag = buf.readCompoundTag();
   }

   public void writePacketData(PacketBuffer buf) throws IOException {
      buf.writeVarInt(this.field_211714_a);
      buf.writeCompoundTag(this.tag);
   }

   public void processPacket(INetHandlerPlayClient handler) {
      handler.handleNBTQueryResponse(this);
   }

   @OnlyIn(Dist.CLIENT)
   public int func_211713_b() {
      return this.field_211714_a;
   }

   @Nullable
   @OnlyIn(Dist.CLIENT)
   public NBTTagCompound getTag() {
      return this.tag;
   }

   public boolean shouldSkipErrors() {
      return true;
   }
}
