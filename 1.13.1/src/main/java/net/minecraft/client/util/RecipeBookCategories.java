package net.minecraft.client.util;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum RecipeBookCategories {
   SEARCH(new ItemStack[]{new ItemStack(Items.COMPASS)}),
   BUILDING_BLOCKS(new ItemStack[]{new ItemStack(Blocks.BRICKS)}),
   REDSTONE(new ItemStack[]{new ItemStack(Items.REDSTONE)}),
   EQUIPMENT(new ItemStack[]{new ItemStack(Items.IRON_AXE), new ItemStack(Items.GOLDEN_SWORD)}),
   MISC(new ItemStack[]{new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.APPLE)}),
   FURNACE_SEARCH(new ItemStack[]{new ItemStack(Items.COMPASS)}),
   FURNACE_FOOD(new ItemStack[]{new ItemStack(Items.PORKCHOP)}),
   FURNACE_BLOCKS(new ItemStack[]{new ItemStack(Blocks.STONE)}),
   FURNACE_MISC(new ItemStack[]{new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.EMERALD)});

   private final List<ItemStack> icons;

   private RecipeBookCategories(ItemStack... p_i48836_3_) {
      this.icons = ImmutableList.copyOf(p_i48836_3_);
   }

   public List<ItemStack> getIcons() {
      return this.icons;
   }
}
