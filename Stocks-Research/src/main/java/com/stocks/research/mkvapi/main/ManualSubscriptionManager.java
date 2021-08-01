package com.stocks.research.mkvapi.main;

public interface ManualSubscriptionManager extends IonDataDictionaryListener {
  void subscribeChain(String paramString, IonRecordListener<FieldMap> paramIonRecordListener);
  
  void subscribeRecord(String paramString, IonRecordListener<FieldMap> paramIonRecordListener);
}
