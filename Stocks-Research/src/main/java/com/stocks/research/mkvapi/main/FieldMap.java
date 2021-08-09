/*    */ package com.stocks.research.mkvapi.main;
/*    */ 
/*    */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class FieldMap
/*    */ {
/*    */   private FieldMap partialFieldMap;
/*    */   private String recordName;
/*    */   private boolean isSnapshot;
/*    */   
/*    */   public boolean isSnapshot() {
/* 21 */     return this.isSnapshot;
/*    */   }
/*    */   
/*    */   public void setSnapshot(boolean snapshot) {
/* 25 */     this.isSnapshot = snapshot;
/*    */   }
/*    */   
/* 28 */   Map<String, Value> valueMap = new HashMap<>();
/*    */   public Object getValue(String field) {
/* 30 */     return ((Value)this.valueMap.get(field)).getValue();
/*    */   }
/*    */   public void putValue(String field, Value value) {
/* 33 */     this.valueMap.put(field, value);
/*    */   }
/* 35 */   public Set<String> getFieldNames() { return this.valueMap.keySet(); }
/* 36 */   public int getFieldCount() { return this.valueMap.size(); }
/* 37 */   public Iterator<Map.Entry<String, Value>> iterator() { return this.valueMap.entrySet().iterator(); } public Map<String, Value> getData() {
/* 38 */     return this.valueMap;
/*    */   }
/*    */   public FieldMap getPartialFieldMap() {
/* 41 */     return this.partialFieldMap;
/*    */   }
/*    */   
/*    */   public void setPartialFieldMap(FieldMap partialFieldMap) {
/* 45 */     this.partialFieldMap = partialFieldMap;
/*    */   }
/*    */   
/*    */   public void set(String name, Value value) {
/* 49 */     this.valueMap.put(name, value);
/*    */   }
/*    */   
/*    */   public Object get(String name) {
/* 53 */     return ((Value)this.valueMap.get(name)).getValue();
/*    */   }
/*    */   
/*    */   public String getRecordName() {
/* 57 */     return this.recordName;
/*    */   }
/*    */   
/*    */   public void setRecordName(String recordName) {
/* 61 */     this.recordName = recordName;
/*    */   }
/*    */ 
/*    */   
/*    */   public String toString() {
/* 66 */     return "FieldMap{" + this.valueMap + '}';
/*    */   }
/*    */ 
/*    */   
/*    */   public String toConciseString() {
/* 71 */     Iterator<Map.Entry<String, Value>> i = this.valueMap.entrySet().iterator();
/* 72 */     if (!i.hasNext()) {
/* 73 */       return "{}";
/*    */     }
/* 75 */     StringBuilder sb = new StringBuilder();
/* 76 */     sb.append('{');
/*    */     while (true) {
/* 78 */       Map.Entry<String, Value> e = i.next();
/* 79 */       String key = e.getKey();
/* 80 */       Value value = e.getValue();
/* 81 */       sb.append(key);
/* 82 */       sb.append('=');
/* 83 */       sb.append((value == null) ? "null" : value.getValue());
/* 84 */       if (!i.hasNext())
/* 85 */         return sb.append('}').toString(); 
/* 86 */       sb.append(',').append(' ');
/*    */     } 
/*    */   }
/*    */ }

