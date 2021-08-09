//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.helper.IonPublishListener;
import com.stocks.research.mkvapi.main.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DummySessionImpl implements IonSession {
    private static final Logger log = LoggerFactory.getLogger(DummySessionImpl.class);
    private volatile EventQueue eventQueue;
    private StopStateListener stopStateListener;
    private String componentName = "TEST_COMPONENT";
    private IonPublishListener ionPublishListener;

    public DummySessionImpl() {
    }

    public void postEvent(IonRecordListener listener, Object object, String recordName) {
        this.eventQueue.postEvent(listener, object, recordName);
    }

    public void start(IonSessionListener ionSessionListener) {
        log.info("START ionSessionListener={}", ionSessionListener);
        if (ionSessionListener == null) {
            log.error("null ionSessionListener");
        }

        this.eventQueue = new EventQueue();
        this.eventQueue.start();
        ionSessionListener.onIonConnected();
    }

    public void start(IonSessionListener ionSessionListener, IonDataDictionaryListener ionDataDictionaryListener) {
        if (ionDataDictionaryListener != null) {
            String dataDirectory = System.getProperty("mkvapi.factory.data-directory");

            try {
                Files.list(Paths.get(dataDirectory)).forEach((p) -> {
                    String chain = p.getFileName().toString();
                    ionDataDictionaryListener.onChainAvailable(chain);
                });
            } catch (IOException var5) {
                var5.printStackTrace();
            }
        }

        this.start(ionSessionListener);
    }

    public boolean isMkvSessionReady() {
        return this.eventQueue != null;
    }

    public boolean isStopped() {
        return false;
    }

    public void registerStopStateListener(StopStateListener stopStateListener) {
        this.stopStateListener = stopStateListener;
    }

    public String getComponentName() {
        return this.componentName;
    }
}
