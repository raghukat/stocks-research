/*    */ package com.stocks.research.mkvapi.main;
/*    */ 
/*    */ import java.util.Map;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public interface IonTransactionHandler<T>
/*    */ {
/*    */   public static final byte SUCCESS_CODE = 0;
/*    */   public static final byte GENERAL_ERROR_CODE = -1;
/* 13 */   public static final Result OK = new Result((byte)0, "OK");
/* 14 */   public static final Result ERROR = new Result((byte)-1, "ERROR");
/*    */   
/*    */   Result onTransaction(String paramString, T paramT, Map<String, Object> paramMap);
/*    */   
/*    */   public static class Result {
/*    */     private byte returnCode;
/*    */     private String messageText;
/*    */     
/*    */     public Result(byte returnCode, String messageText) {
/* 23 */       this.returnCode = returnCode;
/* 24 */       this.messageText = messageText;
/*    */     }
/*    */     
/*    */     public byte getReturnCode() {
/* 28 */       return this.returnCode;
/*    */     }
/*    */     
/*    */     public String getMessageText() {
/* 32 */       return this.messageText;
/*    */     }
/*    */   }
/*    */ }
