package com.stocks.research.mkvapi.main;

public interface IonRecordListener<T> {
  void onRecordUpdate(String paramString, T paramT);
}
