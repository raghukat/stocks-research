/*    */ package com.stocks.research.mkvapi.main;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Value
/*    */ {
/*    */   private Class type;
/*    */   private Object value;
/*    */   
/*    */   public static Value of(Class clazz, Object value) {
/* 12 */     return new Value(clazz, value);
/*    */   }
/*    */   
/*    */   private Value(Class type, Object value) {
/* 16 */     this.type = type;
/* 17 */     this.value = value;
/*    */   }
/*    */   
/*    */   public Class getType() {
/* 21 */     return this.type;
/*    */   }
/*    */   
/*    */   public Object getValue() {
/* 25 */     return this.value;
/*    */   }
/*    */   
/*    */   public String getStringValue() {
/* 29 */     if (getType() == Double.class) {
/* 30 */       return String.format("%.8f", new Object[] { getValue() });
/*    */     }
/* 32 */     return getValue().toString();
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public String toString() {
/* 38 */     return "Value{type=" + this.type + ", value=" + this.value + '}';
/*    */   }
/*    */ }
