/*     */ package com.stocks.research.mkvapi.helper;
/*     */ 
/*     */ import com.iontrading.mkv.MkvRecord;
/*     */ import com.iontrading.mkv.MkvSupply;
/*     */ import com.iontrading.mkv.MkvType;
/*     */ import com.iontrading.mkv.enums.MkvFieldType;
/*     */ import com.iontrading.mkv.events.MkvTransactionCallEvent;
/*     */ import com.iontrading.mkv.events.MkvTransactionListener;
/*     */ import com.iontrading.mkv.helper.MkvSubscribeProxy;
/*     */ import com.stocks.research.mkvapi.main.FieldMap;
/*     */ import com.stocks.research.mkvapi.main.IonPublisher;
/*     */ import com.stocks.research.mkvapi.main.IonTransactionHandler;
/*     */ import java.util.HashMap;
/*     */ import java.util.Map;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class IonTransactionListener<T>
/*     */   implements MkvTransactionListener
/*     */ {
/*  27 */   private static final Logger logger = LoggerFactory.getLogger(IonTransactionListener.class);
/*     */   
/*     */   private class TransactionHandlerItem {
/*     */     IonTransactionHandler<T> ionTransactionHandler;
/*     */     IonPublisher<T> ionPublisher;
/*     */     Class<T> clazz;
/*     */     
/*     */     public TransactionHandlerItem(IonTransactionHandler<T> ionTransactionHandler, IonPublisher<T> ionPublisher, Class<T> clazz) {
/*  35 */       this.ionTransactionHandler = ionTransactionHandler;
/*  36 */       this.ionPublisher = ionPublisher;
/*  37 */       this.clazz = clazz;
/*     */     }
/*     */   }
/*     */   
/*  41 */   private ConcurrentHashMap<String, TransactionHandlerItem> txHandlers = new ConcurrentHashMap<>();
/*     */   
/*     */   public void addTransactionHandler(IonTransactionHandler<T> ionTransactionHandler, IonPublisher<T> ionPublisher, String typeName, Class<T> clazz) {
/*  44 */     this.txHandlers.put(typeName, new TransactionHandlerItem(ionTransactionHandler, ionPublisher, clazz));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void onCall(MkvTransactionCallEvent mkvTransactionCallEvent) {
/*     */     try {
/*  51 */       MkvRecord mkvRecord = mkvTransactionCallEvent.getRecord();
/*  52 */       String rname = mkvRecord.getName();
/*  53 */       String caller = mkvTransactionCallEvent.getCaller();
/*  54 */       MkvType mkvType = mkvRecord.getMkvType();
/*  55 */       String typeName = mkvType.getName();
/*  56 */       TransactionHandlerItem handler = this.txHandlers.get(typeName);
/*     */       
/*  58 */       logger.info("transaction: caller=[{}], record-name=[{}], type=[{}] from=[{}] orig=[{}]", new Object[] { caller, rname, typeName, mkvRecord.getFrom(), mkvRecord.getFrom() });
/*  59 */       if (handler == null) {
/*  60 */         logger.warn("TX: transaction received on [{}] but no handler, will return 'unsupported transaction' message");
/*  61 */         mkvTransactionCallEvent.setResult((byte)-1, "unsupported transaction");
/*     */         
/*     */         return;
/*     */       } 
/*     */       
/*  66 */       MkvSupply supply = mkvTransactionCallEvent.getSupply();
/*  67 */       Map<String, Object> fields = new HashMap<>();
/*  68 */       int index = supply.firstIndex();
/*  69 */       while (index != -1) {
/*  70 */         MkvFieldType type = supply.getType(index);
/*  71 */         String fname = mkvType.getFieldName(index);
/*  72 */         Object value = supply.getObject(index);
/*  73 */         logger.info("transaction: got field [{}] type=[{}] field=[{}] value=[{}] class=[{}]", new Object[] { Integer.valueOf(index), type, fname, value, value.getClass().getName() });
/*  74 */         fields.put(fname, value);
/*  75 */         index = supply.nextIndex(index);
/*     */       } 
/*     */       
/*     */       try {
/*  79 */         T object = handler.clazz.newInstance();
/*     */         
/*  81 */         if (handler.clazz == FieldMap.class) {
/*  82 */           FieldMap fmap = (FieldMap)object;
/*  83 */           fmap.setRecordName(mkvRecord.getName());
/*  84 */           MkvType type = mkvRecord.getMkvType();
/*  85 */           IonHelper.fillFromSupply(supply, type, fmap);
/*     */         } else {
/*     */           
/*  88 */           MkvSubscribeProxy proxy = new MkvSubscribeProxy(handler.clazz);
/*  89 */           proxy.update(mkvRecord, mkvRecord.getSupply(), object);
/*     */         } 
/*     */         
/*  92 */         IonTransactionHandler.Result result = handler.ionTransactionHandler.onTransaction(caller, object, fields);
/*     */         
/*  94 */         logger.info("transaction: handler for [{}] returned [{}] data [{}]", new Object[] { rname, result, object });
/*  95 */         mkvTransactionCallEvent.setResult(result.getReturnCode(), result.getMessageText());
/*     */         
/*  97 */         if (result.getReturnCode() == 0) {
/*  98 */           logger.info("transaction: about to re-publish [{}] fields={}", rname, fields);
/*  99 */           handler.ionPublisher.publishData(rname, fields, null);
/* 100 */           logger.info("transaction: re-publish [{}] DONE", rname);
/*     */         } else {
/*     */           
/* 103 */           logger.warn("transaction code was not SUCCESS, [{}], will not republish [{}]", Byte.valueOf(result.getReturnCode()), rname);
/*     */         }
/*     */       
/* 106 */       } catch (Exception e) {
/* 107 */         logger.error("exception handling transaction record-name=[{}]", rname, e);
/* 108 */         mkvTransactionCallEvent.setResult((byte)-1, "exception handling transaction");
/*     */       }
/*     */     
/* 111 */     } catch (Exception e) {
/* 112 */       logger.error("exception processing transaction", e);
/*     */     } 
/*     */   }
/*     */ }


/* Location:              \mkvapi\helper\IonTransactionListener.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.3
 */