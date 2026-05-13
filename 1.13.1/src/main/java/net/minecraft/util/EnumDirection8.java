package net.minecraft.util;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Set;

public enum EnumDirection8 {
   NORTH(new EnumFacing[]{EnumFacing.NORTH}),
   NORTH_EAST(new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST}),
   EAST(new EnumFacing[]{EnumFacing.EAST}),
   SOUTH_EAST(new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.EAST}),
   SOUTH(new EnumFacing[]{EnumFacing.SOUTH}),
   SOUTH_WEST(new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.WEST}),
   WEST(new EnumFacing[]{EnumFacing.WEST}),
   NORTH_WEST(new EnumFacing[]{EnumFacing.NORTH, EnumFacing.WEST});

   private static final int field_208500_i = 1 << NORTH_WEST.ordinal();
   private static final int field_208501_j = 1 << WEST.ordinal();
   private static final int field_208502_k = 1 << SOUTH_WEST.ordinal();
   private static final int field_208503_l = 1 << SOUTH.ordinal();
   private static final int field_208504_m = 1 << SOUTH_EAST.ordinal();
   private static final int field_208505_n = 1 << EAST.ordinal();
   private static final int field_208506_o = 1 << NORTH_EAST.ordinal();
   private static final int field_208507_p = 1 << NORTH.ordinal();
   private final Set<EnumFacing> directions;

   private EnumDirection8(EnumFacing... directionsIn) {
      this.directions = Sets.<EnumFacing>immutableEnumSet(Arrays.asList(directionsIn));
   }

   public Set<EnumFacing> getDirections() {
      return this.directions;
   }
}
