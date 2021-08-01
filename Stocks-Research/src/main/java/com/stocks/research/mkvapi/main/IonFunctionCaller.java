package com.stocks.research.mkvapi.main;

import java.util.List;

public interface IonFunctionCaller {
  boolean doesFunctionExist(String paramString);
  
  void call(String paramString, List<Object> paramList, IonFunctionReturnListener paramIonFunctionReturnListener);
}
