package com.stocks.research.mkvapi.main;

public interface IonDataDictionaryListener {
  void onRecordAvailable(String paramString);
  
  void onFunctionAvailable(String paramString);
  
  void onChainAvailable(String paramString);
  
  void onPatternAvailable(String paramString);
  
  void onRecordRemoved(String paramString);
  
  void onFunctionRemoved(String paramString);
  
  void onChainRemoved(String paramString);
  
  void onPatternRemoved(String paramString);
}