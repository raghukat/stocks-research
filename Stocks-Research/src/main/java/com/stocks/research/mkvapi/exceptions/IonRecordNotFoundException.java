/*    */ package com.stocks.research.mkvapi.exceptions;
/*    */ 
/*    */ public class IonRecordNotFoundException
/*    */   extends Exception
/*    */ {
/*    */   public IonRecordNotFoundException(String message) {
/*  7 */     super(message);
/*    */   }
/*    */   public IonRecordNotFoundException(String message, Exception e) {
/* 10 */     super(message, e);
/*    */   }
/*    */ }


/* Location:              \mkvapi\exceptions\IonRecordNotFoundException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */