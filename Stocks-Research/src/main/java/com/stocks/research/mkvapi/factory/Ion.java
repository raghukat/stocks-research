package com.stocks.research.mkvapi.factory;

import com.stocks.research.mkvapi.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Ion {
    private static final Logger logger = LoggerFactory.getLogger(Ion.class);

    public static final String FACTORY_PROPERTY = "mkvapi.factory";

    public static final String DUMMY_FACTORY = "dummy";

    public static final String DEFAULT_FACTORY = "default";

    public static final String DIRECTORY_PROPERTY = "mkvapi.factory.data-directory";

    private static volatile IonFactory ionFactory;

    public static <T> IonFactory getFactory() {
        if (ionFactory == null)
            createFactory();
        return ionFactory;
    }

    private static synchronized void createFactory() {
        if (ionFactory == null) {
            logger.info("initializing global factory");
            String factoryClass = System.getProperty("mkvapi.factory");
            if (factoryClass == null) {
                logger.info("no system property set [mkvapi.factory], defaulting to IonFactoryImpl");
                ionFactory = (IonFactory)new IonFactoryImpl();
            } else if (factoryClass.equalsIgnoreCase("default")) {
                logger.info("default factory class specified in property [mkvapi.factory], creating IonFactoryImpl");
                ionFactory = (IonFactory)new IonFactoryImpl();
            } else if (factoryClass.equalsIgnoreCase("dummy")) {
                logger.info("dummy factory class specified in property [mkvapi.factory], creating DummyIonFactoryImpl");
//                ionFactory = (IonFactory)new DummyIonFactoryImpl();
            } else {
                logger.info("using factory class [{}] as specified in property [mkvapi.factory]", factoryClass);
                try {
                    Object o = (Object)Class.forName(factoryClass);
                    ionFactory = (IonFactory)o;
                } catch (Exception e) {
                    logger.error("failed to create factory instance of [{}]", factoryClass, e);
                }
            }
        }
    }

    public static synchronized void reset() {
        ionFactory = null;
    }

    public static IonSession getSession() {
        return getFactory().getSession();
    }

    public static <T> IonPublisher getPublisher(Class<T> clazz) throws Exception {
        return getFactory().getPublisher(clazz);
    }

    public static <T> IonSubscriber getSubscriber(Class<T> clazz, IonRecordListener<T> ionRecordListener, IonChainListener ionChainListener) {
        return getFactory().getSubscriber(clazz, ionRecordListener, ionChainListener);
    }

    public static <T> IonSubscriber getSubscriber(Class<T> clazz, IonRecordListener<T> ionRecordListener) {
        return getFactory().getSubscriber(clazz, ionRecordListener, (chain, records) -> logger.info("received {} records in the chain {}", records, chain));
    }

    public static IonObjectRequester getIonRequester() {
        return getFactory().getObjectRequester();
    }

    public static IonTransactionCaller getTransactionCaller() {
        return getFactory().getTransactionCaller();
    }

    public static IonFunctionPublisher getFunctionPublisher() {
        return getFactory().getFunctionPublisher();
    }

    public static IonFunctionCaller getFunctionCaller() {
        return getFactory().getFunctionCaller();
    }
}
