/*    */ package com.stocks.research.mkvapi_util;
/*    */ 
/*    */ import com.stocks.research.mkvapi.factory.Ion;
/*    */ import com.stocks.research.mkvapi.helper.IonHelper;
/*    */ import com.stocks.research.mkvapi.main.FieldMap;
/*    */ import com.stocks.research.mkvapi.main.IonSubscriber;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class UtilMain
/*    */ {
/* 54 */   private static final Logger log = LoggerFactory.getLogger(UtilMain.class);
/*    */   
/*    */   public static void main(String[] args) throws Exception {
/* 57 */     log.info("starting ION...");
/* 58 */     IonHelper.startMkvAndAwaitReady();
/* 59 */     log.info("starting ION...READY");
/*    */     
/* 61 */     SubscribeSpec spec = new SubscribeSpec("src/test/resources/bond_price_comparison.txt");
/*    */     
/* 63 */     ComparativeFieldMapRecordListener recordListener = new ComparativeFieldMapRecordListener(spec.chain1, spec.chain2, spec.fields, spec.reportName);
/*    */     
/* 65 */     log.info("COMPARE [{}] [{}]", spec.chain1, spec.chain2);
/* 66 */     log.info("FIELDS [{}] REPORT [{}]", spec.fields, spec.reportName);
/*    */     
/* 68 */     IonSubscriber subscriber1 = Ion.getSubscriber(FieldMap.class, recordListener);
/* 69 */     Thread.sleep(10000L);
/* 70 */     IonSubscriber subscriber2 = Ion.getSubscriber(FieldMap.class, recordListener);
/*    */     
/* 72 */     subscriber1.subscribeChain(spec.chain1, spec.fields);
/* 73 */     subscriber2.subscribeChain(spec.chain2, spec.fields);
/*    */   }
/*    */ }
