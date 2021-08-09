package com.stocks.research.mkvapi.main;

import java.util.List;

public interface IonObjectRequester {
  <T> List<T> getRecords(List<String> paramList1, List<String> paramList2, Class<T> paramClass);
}
