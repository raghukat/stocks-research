/*    */ package com.stocks.research.mkvapi_util;
/*    */ 
/*    */ import java.io.BufferedReader;
/*    */ import java.io.FileReader;
/*    */ import java.util.ArrayList;
/*    */ import java.util.List;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class SubscribeSpec
/*    */ {
/*    */   String chain1;
/*    */   String chain2;
/*    */   String reportName;
/* 15 */   List<String> fields = new ArrayList<>();
/*    */   
/*    */   public SubscribeSpec(String file) throws Exception {
/* 18 */     loadSubscribeSpec(file);
/*    */   }
/*    */ 
/*    */   
/*    */   private void loadSubscribeSpec(String file) throws Exception {
/* 23 */     BufferedReader rd = new BufferedReader(new FileReader(file));
/*    */     String s;
/* 25 */     while ((s = rd.readLine()) != null) {
/* 26 */       s = s.trim();
/* 27 */       if (s.length() == 0 || s.startsWith("#")) {
/*    */         continue;
/*    */       }
/* 30 */       if (s.startsWith(">")) {
/* 31 */         this.reportName = substitute(s.substring(1)); continue;
/*    */       } 
/* 33 */       if (s.startsWith("@")) {
/* 34 */         if (this.chain1 == null) {
/* 35 */           this.chain1 = substitute(s.substring(1));
/*    */           continue;
/*    */         } 
/* 38 */         this.chain2 = substitute(s.substring(1));
/*    */         
/*    */         continue;
/*    */       } 
/* 42 */       this.fields.add(s);
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   String substitute(String s) {
/* 48 */     String ccy = System.getProperty("CCY");
/* 49 */     if (s.contains("%CCY%")) {
/* 50 */       return s.replaceAll("%CCY%", ccy);
/*    */     }
/*    */     
/* 53 */     return s;
/*    */   }
/*    */ }

