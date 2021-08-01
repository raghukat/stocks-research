package com.stocks.research.mkvapi.main;

import com.iontrading.mkv.exceptions.MkvException;
import com.stocks.research.mkvapi.exceptions.IonChainNotFoundException;
import com.stocks.research.mkvapi.exceptions.IonInvalidRecordOrChainNameException;
import com.stocks.research.mkvapi.exceptions.IonRecordNotFoundException;

import java.util.List;
import java.util.Map;

public interface IonPublisher<T> {
  void publishPattern(String paramString) throws MkvException;
  
  void publishChain(String paramString) throws MkvException;
  
  void publishData(String paramString, T paramT, List<String> paramList) throws MkvException, IonChainNotFoundException, IonInvalidRecordOrChainNameException;
  
  void publishData(String paramString, Map<String, Object> paramMap, List<String> paramList) throws MkvException, IonChainNotFoundException, IonInvalidRecordOrChainNameException;
  
  boolean unpublishRecord(String paramString) throws IonRecordNotFoundException;
  
  String getTypeName();
  
  void setTransactionHandler(IonTransactionHandler paramIonTransactionHandler);
  
  void addRecordToChain(String paramString1, String paramString2);
  
  void removeRecordFromChain(String paramString1, String paramString2);
}
