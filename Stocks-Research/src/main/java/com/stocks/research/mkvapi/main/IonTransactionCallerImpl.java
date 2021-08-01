/*    */ package com.stocks.research.mkvapi.main;
/*    */ 
/*    */ import com.iontrading.mkv.MkvRecord;
import com.iontrading.mkv.events.MkvTransactionCallEvent;
import com.iontrading.mkv.events.MkvTransactionCallListener;
import com.iontrading.mkv.helper.MkvSupplyBuilder;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

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
/*    */ public class IonTransactionCallerImpl
/*    */   implements IonTransactionCaller
/*    */ {
/* 20 */   private static final Logger logger = LoggerFactory.getLogger(IonSubscriberImpl.class);
/*    */   
/*    */   private class TransactionListener implements MkvTransactionCallListener {
/*    */     private String recordName;
/*    */     private Map<String, Object> fields;
/*    */     private IonTransactionResultListener transactionResultListener;
/*    */     
/*    */     public TransactionListener(String recordName, Map<String, Object> fields, IonTransactionResultListener transactionResultListener) {
/* 28 */       this.recordName = recordName;
/* 29 */       this.fields = fields;
/* 30 */       this.transactionResultListener = transactionResultListener;
/*    */     }
/*    */ 
/*    */     
/*    */     public void onResult(MkvTransactionCallEvent mkvTransactionCallEvent, byte b, String s) {
/* 35 */       IonTransactionCallerImpl.logger.info("transaction call result: record [{}] result-code {} result-text [{}]", new Object[] { this.recordName, Byte.valueOf(b), s });
/* 36 */       if (this.transactionResultListener != null) {
/* 37 */         this.transactionResultListener.onTransactionCallResult(this.recordName, b, s);
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   
/*    */   public boolean doesRecordExist(String recordName) {
/* 44 */     MkvRecord mkvRecord = IonHelper.getMkvRecord(recordName);
/* 45 */     return (mkvRecord != null);
/*    */   }
/*    */ 
/*    */   
/*    */   public void apply(String recordName, Map<String, Object> fields) {
/* 50 */     apply(recordName, fields, null);
/*    */   }
/*    */ 
/*    */   
/*    */   public void apply(String recordName, Map<String, Object> fields, IonTransactionResultListener transactionResultListener) {
/*    */     try {
/* 56 */       logger.info("transaction: apply to [{}] fields={}", recordName, fields);
/* 57 */       MkvRecord mkvRecord = IonHelper.getMkvRecord(recordName);
/* 58 */       if (mkvRecord == null) {
/* 59 */         logger.warn("transaction: apply - no record found [{}]", recordName);
/* 60 */         throw new RuntimeException("No record [" + recordName + "]");
/*    */       } 
/* 62 */       MkvSupplyBuilder mkvSupplyBuilder = new MkvSupplyBuilder(mkvRecord);
/* 63 */       Set<Map.Entry<String, Object>> entries = fields.entrySet();
/* 64 */       for (Map.Entry<String, Object> e : entries) {
/* 65 */         mkvSupplyBuilder.setField(e.getKey(), e.getValue());
/*    */       }
/* 67 */       TransactionListener transactionListener = new TransactionListener(recordName, fields, transactionResultListener);
/* 68 */       mkvRecord.transaction(mkvSupplyBuilder.getSupply(), transactionListener);
/* 69 */       logger.info("transaction: apply to [{}] done", recordName);
/* 70 */       if (RecordRecorder.isRecording()) {
/* 71 */         RecordRecorder rd = RecordRecorder.lookupRecordRecorder(recordName);
/* 72 */         if (rd != null) {
/* 73 */           rd.recordTransaction(fields);
/*    */         }
/*    */       }
/*    */     
/* 77 */     } catch (Exception e) {
/* 78 */       logger.info("transaction: apply [{}] exception", e);
/*    */     } 
/*    */   }
/*    */ }
