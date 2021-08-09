//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.exceptions.IonChainAlreadySubscribedException;
import com.stocks.research.mkvapi.exceptions.IonPatternSubscribeFailedException;
import com.stocks.research.mkvapi.exceptions.IonRecordUnsubscribeFailedException;
import com.stocks.research.mkvapi.main.IonRecordListener;
import com.stocks.research.mkvapi.main.IonSubscriber;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DummyLoopbackSubscriber<T> implements IonSubscriber {
    private static final Logger log = LoggerFactory.getLogger(DummyFileBasedSubscriber.class);
    private Class<T> clazz;
    private IonRecordListener<T> ionRecordListener;

    public DummyLoopbackSubscriber(Class<T> clazz, IonRecordListener<T> ionRecordListener) {
        this.clazz = clazz;
        this.ionRecordListener = ionRecordListener;
    }

    public void subscribeChain(String chainName, @Nullable List<String> fieldNames) throws IonChainAlreadySubscribedException {
        DummyPublisher publisher = (DummyPublisher)DummyPublisher.allPublishers.get(this.clazz);
        if (publisher == null) {
            log.error("no publisher for [{}] that we can subscribe to", this.clazz.getName());
        }

        publisher.addChainrecordListener(this.ionRecordListener, chainName);
    }

    public void subscribeRecords(List<String> recordNames, @Nullable List<String> fieldNames) {
        DummyPublisher publisher = (DummyPublisher)DummyPublisher.allPublishers.get(this.clazz);
        if (publisher == null) {
            log.error("no publisher for [{}] that we can subscribe to", this.clazz.getName());
        }

        publisher.addRecordListener(this.ionRecordListener, recordNames);
    }

    public void unsubscribeAllRecords() {
        DummyPublisher publisher = (DummyPublisher)DummyPublisher.allPublishers.get(this.clazz);
        if (publisher == null) {
            log.error("no publisher for [{}] that we can subscribe to", this.clazz.getName());
        }

    }

    public void unsubscribeAllChain() {
        DummyPublisher publisher = (DummyPublisher)DummyPublisher.allPublishers.get(this.clazz);
        if (publisher == null) {
            log.error("no publisher for [{}] that we can subscribe to", this.clazz.getName());
        }

    }

    public boolean subscribePattern(String recordPrefix) throws IonPatternSubscribeFailedException {
        return false;
    }

    public boolean subscribePattern(String recordPrefix, List<String> fields) throws IonPatternSubscribeFailedException {
        return false;
    }

    public void unsubscribeChain(String chainName) {
    }

    public void unsubscribeRecords(List<String> recordNames, @Nullable List<String> fieldNames) throws IonRecordUnsubscribeFailedException {
    }
}
