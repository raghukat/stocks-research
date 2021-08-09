package com.stocks.research.mkvapi.main;

import com.iontrading.mkv.events.MkvFunctionCallEvent;

import java.util.List;

@FunctionalInterface
public interface IonFunctionCallHandlerAsync {
  void onFunctionCalled(String paramString, int paramInt, List<Object> paramList, MkvFunctionCallEvent paramMkvFunctionCallEvent);
}
