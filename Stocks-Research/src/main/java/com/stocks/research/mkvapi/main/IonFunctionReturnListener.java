package com.stocks.research.mkvapi.main;

import java.util.List;

public interface IonFunctionReturnListener {
  void onFunctionReturnOk(String paramString, List<Object> paramList);
  
  void onFunctionReturnError(String paramString1, int paramInt, String paramString2);
}
