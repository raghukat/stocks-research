package com.stocks.research.mkvapi.main;

import com.stocks.research.mkvapi.exceptions.IonChainAlreadySubscribedException;
import com.stocks.research.mkvapi.exceptions.IonPatternSubscribeFailedException;
import com.stocks.research.mkvapi.exceptions.IonRecordUnsubscribeFailedException;

import java.util.List;

public interface IonSubscriber {
  void subscribeChain(String paramString, List<String> paramList) throws IonChainAlreadySubscribedException;
  
  void subscribeRecords(List<String> paramList1, List<String> paramList2);
  
  void unsubscribeAllRecords();
  
  void unsubscribeAllChain();
  
  boolean subscribePattern(String paramString) throws IonPatternSubscribeFailedException;
  
  boolean subscribePattern(String paramString, List<String> paramList) throws IonPatternSubscribeFailedException;
  
  void unsubscribeChain(String paramString);
  
  void unsubscribeRecords(List<String> paramList1, List<String> paramList2) throws IonRecordUnsubscribeFailedException;
}
