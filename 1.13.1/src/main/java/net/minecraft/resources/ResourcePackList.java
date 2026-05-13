package net.minecraft.resources;

import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class ResourcePackList<T extends ResourcePackInfo> {
   private final Set<IPackFinder> packFinders = Sets.<IPackFinder>newHashSet();
   private final Map<String, T> packNameToInfo = Maps.<String, T>newLinkedHashMap();
   private final List<T> packInfos = Lists.<T>newLinkedList();
   private final ResourcePackInfo.IFactory<T> packInfoFactory;

   public ResourcePackList(ResourcePackInfo.IFactory<T> packInfoFactoryIn) {
      this.packInfoFactory = packInfoFactoryIn;
   }

   public void reloadPacksFromFinders() {
      Set<String> set = (Set)this.packInfos.stream().<String>map(ResourcePackInfo::getName).collect(Collectors.toCollection(LinkedHashSet::new));
      this.packNameToInfo.clear();
      this.packInfos.clear();

      for(IPackFinder ipackfinder : this.packFinders) {
         ipackfinder.addPackInfosToMap(this.packNameToInfo, this.packInfoFactory);
      }

      this.sortPackNameToInfo();
      this.packInfos.addAll((Collection)set.stream().<T>map(this.packNameToInfo::get).filter(Objects::nonNull).collect(Collectors.toCollection(LinkedHashSet::new)));

      for(T t : this.packNameToInfo.values()) {
         if (t.func_195797_g() && !this.packInfos.contains(t)) {
            t.getPriority().func_198993_a(this.packInfos, t, Functions.identity(), false);
         }
      }

   }

   private void sortPackNameToInfo() {
      List<Entry<String, T>> list = Lists.newArrayList(this.packNameToInfo.entrySet());
      this.packNameToInfo.clear();
      list.stream().sorted(Entry.comparingByKey()).forEachOrdered((p_198984_1_) -> {
         ResourcePackInfo resourcepackinfo = (ResourcePackInfo)this.packNameToInfo.put((String)p_198984_1_.getKey(), (T)p_198984_1_.getValue());
      });
   }

   public void setEnabledPacks(Collection<T> p_198985_1_) {
      this.packInfos.clear();
      this.packInfos.addAll(p_198985_1_);

      for(T t : this.packNameToInfo.values()) {
         if (t.func_195797_g() && !this.packInfos.contains(t)) {
            t.getPriority().func_198993_a(this.packInfos, t, Functions.identity(), false);
         }
      }

   }

   public Collection<T> getAllPacks() {
      return this.packNameToInfo.values();
   }

   public Collection<T> getAvailablePacks() {
      Collection<T> collection = Lists.newArrayList(this.packNameToInfo.values());
      collection.removeAll(this.packInfos);
      return collection;
   }

   public Collection<T> getPackInfos() {
      return this.packInfos;
   }

   @Nullable
   public T getPackInfo(String name) {
      return (T)(this.packNameToInfo.get(name));
   }

   public void addPackFinder(IPackFinder packFinder) {
      this.packFinders.add(packFinder);
   }
}
