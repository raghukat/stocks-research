package com.stocks.research.mkvapi.factory;

import com.stocks.research.mkvapi.main.*;

public interface IonFactory<T> {
  IonSession getSession();

  IonPublisher getPublisher(Class<T> paramClass) throws Exception;

  IonSubscriber getSubscriber(Class<T> paramClass, IonRecordListener<T> paramIonRecordListener, IonChainListener paramIonChainListener);

  IonObjectRequester getObjectRequester();

  IonTransactionCaller getTransactionCaller();

  IonFunctionPublisher getFunctionPublisher();

  IonFunctionCaller getFunctionCaller();
}
