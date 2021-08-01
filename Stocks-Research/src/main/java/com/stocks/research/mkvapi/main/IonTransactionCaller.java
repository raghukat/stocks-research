package com.stocks.research.mkvapi.main;

import java.util.Map;

public interface IonTransactionCaller {
  boolean doesRecordExist(String paramString);
  
  void apply(String paramString, Map<String, Object> paramMap);
  
  void apply(String paramString, Map<String, Object> paramMap, IonTransactionResultListener paramIonTransactionResultListener);
}
