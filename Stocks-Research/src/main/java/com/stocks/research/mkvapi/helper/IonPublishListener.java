/*    */ package com.stocks.research.mkvapi.helper;
/*    */ 
/*    */ import com.iontrading.mkv.MkvObject;
/*    */ import com.iontrading.mkv.enums.MkvObjectType;
/*    */ import com.iontrading.mkv.events.MkvPublishListener;
/*    */ import com.stocks.research.mkvapi.main.IonDataDictionaryListener;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ 
/*    */ 
/*    */ 
/*    */ public class IonPublishListener
/*    */   implements MkvPublishListener
/*    */ {
/* 15 */   private static final Logger logger = LoggerFactory.getLogger(IonTransactionListener.class);
/*    */   
/*    */   private IonDataDictionaryListener dataDictionaryListener;
/*    */   
/*    */   public IonPublishListener(IonDataDictionaryListener dataDictionaryListener) {
/* 20 */     this.dataDictionaryListener = dataDictionaryListener;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public void onPublish(MkvObject mkvObject, boolean start, boolean download) {
/* 26 */     MkvObjectType type = mkvObject.getMkvObjectType();
/*    */     
/* 28 */     if (start) {
/* 29 */       if (type == MkvObjectType.RECORD) {
/* 30 */         this.dataDictionaryListener.onRecordAvailable(mkvObject.getName());
/*    */       }
/* 32 */       else if (type == MkvObjectType.FUNCTION) {
/* 33 */         this.dataDictionaryListener.onFunctionAvailable(mkvObject.getName());
/*    */       }
/* 35 */       else if (type == MkvObjectType.CHAIN) {
/* 36 */         this.dataDictionaryListener.onChainAvailable(mkvObject.getName());
/*    */       }
/* 38 */       else if (type == MkvObjectType.PATTERN) {
/* 39 */         this.dataDictionaryListener.onPatternAvailable(mkvObject.getName());
/*    */       
/*    */       }
/*    */     
/*    */     }
/* 44 */     else if (type == MkvObjectType.RECORD) {
/* 45 */       this.dataDictionaryListener.onRecordRemoved(mkvObject.getName());
/*    */     }
/* 47 */     else if (type == MkvObjectType.FUNCTION) {
/* 48 */       this.dataDictionaryListener.onFunctionRemoved(mkvObject.getName());
/*    */     }
/* 50 */     else if (type == MkvObjectType.CHAIN) {
/* 51 */       this.dataDictionaryListener.onChainRemoved(mkvObject.getName());
/*    */     }
/* 53 */     else if (type == MkvObjectType.PATTERN) {
/* 54 */       this.dataDictionaryListener.onPatternRemoved(mkvObject.getName());
/*    */     } 
/*    */   }
/*    */   
/*    */   public void onPublishIdle(String s, boolean b) {}
/*    */   
/*    */   public void onSubscribe(MkvObject mkvObject) {}
/*    */ }


/* Location:              \mkvapi\helper\IonPublishListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */