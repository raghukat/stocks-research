/*    */ package com.stocks.research.mkvapi.exceptions;
/*    */ 
/*    */ import com.iontrading.mkv.exceptions.MkvException;
/*    */ 
/*    */ public class IonChainUnsubscribeFailedException extends Exception {
/*    */   public IonChainUnsubscribeFailedException(String message, MkvException e) {
/*  7 */     super(message, (Throwable)e);
/*    */   }
/*    */   public IonChainUnsubscribeFailedException(String message) {
/* 10 */     super(message);
/*    */   }
/*    */ }


/* Location:              \mkvapi\exceptions\IonChainUnsubscribeFailedException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */