/*    */ package com.stocks.research.mkvapi.helper;
/*    */ 
/*    */ import com.stocks.research.mkvapi.main.IonTransactionResultListener;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ 
/*    */ public class GenericTransactionResultListener
/*    */   implements IonTransactionResultListener {
/*    */   volatile boolean transactionCallresultReceived;
/* 10 */   private static final Logger log = LoggerFactory.getLogger(GenericTransactionResultListener.class);
/*    */ 
/*    */   
/*    */   public void onTransactionCallResult(String recordName, int resultCode, String resultMessage) {
/* 14 */     this.transactionCallresultReceived = true;
/* 15 */     log.debug("Transaction call on [{}] recordName returned with resultCode [{}], resultMessage [{}]");
/*    */   }
/*    */ }


/* Location:              \mkvapi\helper\GenericTransactionResultListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */