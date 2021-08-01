package com.stocks.research.mkvapi.main;

import java.util.List;

public interface IonFunctionPublisher {
  void publishFunction(String paramString, Class paramClass, List<String> paramList, List<Class> paramList1, IonFunctionCallHandler paramIonFunctionCallHandler);
  
  void publishFunctionAsync(String paramString, Class paramClass, List<String> paramList, List<Class> paramList1, IonFunctionCallHandlerAsync paramIonFunctionCallHandlerAsync);
  
  void publishFunctionForTest(String paramString, Class paramClass, List<String> paramList, List<Class> paramList1, IonFunctionCallHandlerAsync paramIonFunctionCallHandlerAsync);
}
