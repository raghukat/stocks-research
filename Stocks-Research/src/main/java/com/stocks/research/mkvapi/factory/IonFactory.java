package com.stocks.research.mkvapi.factory;

import com.stocks.research.mkvapi.main.IonChainListener;
import com.stocks.research.mkvapi.main.IonFunctionCaller;
import com.stocks.research.mkvapi.main.IonFunctionPublisher;
import com.stocks.research.mkvapi.main.IonObjectRequester;
import com.stocks.research.mkvapi.main.IonPublisher;
import com.stocks.research.mkvapi.main.IonRecordListener;
import com.stocks.research.mkvapi.main.IonSession;
import com.stocks.research.mkvapi.main.IonSubscriber;
import com.stocks.research.mkvapi.main.IonTransactionCaller;

public interface IonFactory<T> {
  IonSession getSession();

  IonPublisher getPublisher(Class<T> paramClass) throws Exception;

  IonSubscriber getSubscriber(Class<T> paramClass, IonRecordListener<T> paramIonRecordListener, IonChainListener paramIonChainListener);

  IonObjectRequester getObjectRequester();

  IonTransactionCaller getTransactionCaller();

  IonFunctionPublisher getFunctionPublisher();

  IonFunctionCaller getFunctionCaller();
}
