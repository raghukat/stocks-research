package com.stocks.research.mkvapi.main;

import java.util.List;

@FunctionalInterface
public interface IonFunctionCallHandler {
  IonFunctionResult onFunctionCalled(String paramString, List<Object> paramList);
}

