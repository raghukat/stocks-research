package com.stocks.research.mkvapi.main;

public interface IonSession {
  void start(IonSessionListener paramIonSessionListener);
  
  void start(IonSessionListener paramIonSessionListener, IonDataDictionaryListener paramIonDataDictionaryListener);
  
  boolean isMkvSessionReady();
  
  String getComponentName();
  
  boolean isStopped();
  
  void registerStopStateListener(StopStateListener paramStopStateListener);
}
