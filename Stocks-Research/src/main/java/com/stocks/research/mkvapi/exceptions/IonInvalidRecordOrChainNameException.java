/*    */ package com.stocks.research.mkvapi.exceptions;
/*    */ 
/*    */ import com.iontrading.mkv.exceptions.MkvException;
/*    */ 
/*    */ public class IonInvalidRecordOrChainNameException extends Exception {
/*    */   public IonInvalidRecordOrChainNameException(String message, MkvException e) {
/*  7 */     super(message, (Throwable)e);
/*    */   }
/*    */   
/*    */   public IonInvalidRecordOrChainNameException(String message) {
/* 11 */     super(message);
/*    */   }
/*    */ }


/* Location:              \mkvapi\exceptions\IonInvalidRecordOrChainNameException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */