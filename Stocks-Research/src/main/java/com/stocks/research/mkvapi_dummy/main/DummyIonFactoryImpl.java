//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.factory.IonFactory;
import com.stocks.research.mkvapi.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyIonFactoryImpl<T> implements IonFactory<T> {
    private static final Logger log = LoggerFactory.getLogger(DummyIonFactoryImpl.class);
    private static final DummySessionImpl dummySessionImpl = new DummySessionImpl();
    private boolean useLoopbackSubscriber;
    private DummyFunctionCallResultProvider userDummyResultProvider;

    public DummyIonFactoryImpl() {
    }

    public boolean isUseLoopbackSubscriber() {
        return this.useLoopbackSubscriber;
    }

    public void setUseLoopbackSubscriber(boolean useloopbackSub) {
        this.useLoopbackSubscriber = useloopbackSub;
    }

    public void setDummyFunctionCallResultProvider(DummyFunctionCallResultProvider dummyFunctionCallResultProvider) {
        this.userDummyResultProvider = dummyFunctionCallResultProvider;
    }

    public DummyFunctionCallResultProvider getDummyFunctionCallResultProvider() {
        return this.userDummyResultProvider;
    }

    public IonSession getSession() {
        return dummySessionImpl;
    }

    public IonPublisher getPublisher(Class<T> clazz) throws Exception {
        log.info("factory: getPublisher return DummyPublisher");
        return new DummyPublisher(clazz);
    }

    @Override
    public IonSubscriber getSubscriber(Class<T> clazz, IonRecordListener<T> ionRecordListener, IonChainListener ionChainListener) {
        if (this.useLoopbackSubscriber) {
            log.info("factory: getSubscriber return DummyLoopbackSubscriber");
            return new DummyLoopbackSubscriber(clazz, ionRecordListener);
        } else {
            log.info("factory: getSubscriber return DummyLoopbackSubscriber");
            return new DummyFileBasedSubscriber(clazz, ionRecordListener, ionChainListener);
        }
    }

    public IonObjectRequester getObjectRequester() {
        return new DummyIonObjectRequesterImpl();
    }

    public IonTransactionCaller getTransactionCaller() {
        if (this.useLoopbackSubscriber) {
            log.info("factory: getTransactionCaller return DummyLoopbackTransactionCaller");
            return new DummyLoopbackTransactionCaller();
        } else {
            log.info("factory: getTransactionCaller return DummyFileBasedTransactionCaller");
            return new DummyFileBasedTransactionCaller();
        }
    }

    public IonFunctionPublisher getFunctionPublisher() {
        return new DummyFunctionPublisher();
    }

    public IonFunctionCaller getFunctionCaller() {
        Object dummyResultProvider = null;

        try {
            if (this.userDummyResultProvider == null) {
                dummyResultProvider = new DummyFileBasedFunctionCallResultProvider();
            } else {
                dummyResultProvider = this.userDummyResultProvider;
            }

            log.info("using dummyResultProvider for function call [{}]", dummyResultProvider);
        } catch (Exception var3) {
            log.error("exception from DummyFileBasedFunctionCallResultProvider", var3);
        }

        return new DummyFunctionCaller((DummyFunctionCallResultProvider)dummyResultProvider);
    }
}
