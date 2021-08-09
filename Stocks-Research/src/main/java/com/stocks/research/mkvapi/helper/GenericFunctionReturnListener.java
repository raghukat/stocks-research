/*    */ package com.stocks.research.mkvapi.helper;
/*    */ 
/*    */ import com.stocks.research.mkvapi.main.IonFunctionReturnListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*    */
/*    */
/*    */
/*    */ 
/*    */ public class GenericFunctionReturnListener
/*    */   implements IonFunctionReturnListener {
/* 10 */   private static final Logger log = LoggerFactory.getLogger(IonTransactionListener.class);
/*    */   
/*    */   private volatile boolean returned;
/*    */   
/*    */   public void onFunctionReturnOk(String functionName, List<Object> returnValues) {
/* 15 */     this.returned = true;
/* 16 */     log.info("--- func return OK: function [{}] returned values {} ---", functionName, returnValues);
/*    */   }
/*    */ 
/*    */   
/*    */   public void onFunctionReturnError(String functionName, int errorCode, String errorText) {
/* 21 */     this.returned = true;
/* 22 */     log.info("--- func return ERROR: function [{}] returned error {} [{]] ---", new Object[] { functionName, Integer.valueOf(errorCode), errorText });
/*    */   }
/*    */ }


/* Location:              \mkvapi\helper\GenericFunctionReturnListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */