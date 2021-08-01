/*    */ package com.stocks.research.mkvapi.main;
/*    */ 
/*    */ import java.util.List;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class IonFunctionResult
/*    */ {
/*    */   byte returnCode;
/*    */   String errorText;
/*    */   List<Object> returnValues;
/*    */   
/*    */   public byte getReturnCode() {
/* 16 */     return this.returnCode;
/*    */   }
/*    */   
/*    */   public void setReturnCode(byte returnCode) {
/* 20 */     this.returnCode = returnCode;
/*    */   }
/*    */   
/*    */   public String getErrorText() {
/* 24 */     return this.errorText;
/*    */   }
/*    */   
/*    */   public void setErrorText(String errorText) {
/* 28 */     this.errorText = errorText;
/*    */   }
/*    */   
/*    */   public List<Object> getReturnValues() {
/* 32 */     return this.returnValues;
/*    */   }
/*    */   
/*    */   public void setReturnValues(List<Object> returnValues) {
/* 36 */     this.returnValues = returnValues;
/*    */   }
/*    */ 
/*    */   
/*    */   public String toString() {
/* 41 */     return "IonFunctionResult{returnCode=" + this.returnCode + ", errorText='" + this.errorText + '\'' + ", returnValues=" + this.returnValues + '}';
/*    */   }
/*    */ }
