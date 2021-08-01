package com.stocks.research.mkvapi.main;

import com.stocks.research.mkvapi.factory.IonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IonFactoryImpl<T> implements IonFactory<T> {
    private static final Logger log = LoggerFactory.getLogger(IonFactoryImpl.class);

    private static final IonSessionImpl ionSession = new IonSessionImpl();

    public IonSession getSession() {
        return ionSession;
    }

    public IonPublisher getPublisher(Class<T> clazz) throws Exception {
        log.info("factory: getPublisher return IonPublisherImpl");
        if (clazz.equals(FieldMap.class)) {
            log.info("factory: returning field-map publisher");
            return new IonFieldMapPublisherImpl((Class<FieldMap>) clazz);
        }
        return new IonPublisherImpl<>(clazz);
    }

    public IonSubscriber getSubscriber(Class<T> clazz, IonRecordListener<T> ionRecordListener, IonChainListener ionChainListener) {
        log.info("factory: getPublisher return IonSubscriberImpl");
        return new IonSubscriberImpl<>(clazz, ionRecordListener, ionChainListener);
    }

    public IonObjectRequester getObjectRequester() {
        return new IonObjectRequesterImpl();
    }

    public IonTransactionCaller getTransactionCaller() {
        log.info("factory: getTransactionCaller return IonTransactionCallerImpl");
        return new IonTransactionCallerImpl();
    }

    public IonFunctionPublisher getFunctionPublisher() {
        log.info("factory: getFunctionPublisher return IonFunctionPublisherImpl");
        return new IonFunctionPublisherImpl();
    }

    public IonFunctionCaller getFunctionCaller() {
        log.info("factory: getFunctionPublisher return IonFunctionCallerImpl");
        return new IonFunctionCallerImpl();
    }
}
