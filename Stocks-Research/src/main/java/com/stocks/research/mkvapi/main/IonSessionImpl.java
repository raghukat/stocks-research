package com.stocks.research.mkvapi.main;

import com.iontrading.mkv.Mkv;
import com.iontrading.mkv.MkvComponent;
import com.iontrading.mkv.MkvProperties;
import com.iontrading.mkv.enums.MkvPlatformEvent;
import com.iontrading.mkv.events.MkvPlatformListener;
import com.iontrading.mkv.events.MkvPublishListener;
import com.iontrading.mkv.events.MkvTransactionListener;
import com.iontrading.mkv.exceptions.MkvException;
import com.iontrading.mkv.qos.MkvQoS;
import com.stocks.research.mkvapi.helper.IonPublishListener;
import com.stocks.research.mkvapi.helper.IonTransactionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class IonSessionImpl implements IonSession {
    private static final Logger logger = LoggerFactory.getLogger(IonSessionImpl.class);

    private AtomicBoolean isComponentProperlyRegistered = new AtomicBoolean(false);

    private IonTransactionListener ionTransactionListener = new IonTransactionListener();

    private IonPublishListener ionPublishListener;

    public static volatile Mkv mkvSaved;

    public boolean isMkvSessionReady() {
        return this.isComponentProperlyRegistered.get();
    }

    volatile boolean stopped = false;

    public StopStateListener stopStateListener;

    public String getComponentName() {
        return Mkv.getInstance().getProperties().getComponentName();
    }

    public void start(IonSessionListener ionSessionListener, IonDataDictionaryListener ionDataDictionaryListener) {
        this.ionPublishListener = new IonPublishListener(ionDataDictionaryListener);
        start(ionSessionListener);
    }

    public void start(IonSessionListener ionSessionListener) {
        RecordRecorder.initializeRecording();
        logger.info("ION SESSION: START recordingMode={} ()", Boolean.valueOf(RecordRecorder.isRecording()));
        logger.debug("ION SESSION DEBUG LOGGING ACTIVE");
        MkvQoS qos = new MkvQoS();
        MkvPlatformListener mkvPlatformListener = new MkvPlatformListenerImpl(ionSessionListener);
        qos.setPlatformListeners(new MkvPlatformListener[] { mkvPlatformListener });
        this.ionTransactionListener = new IonTransactionListener();
        qos.setTransactionListeners(new MkvTransactionListener[] { (MkvTransactionListener)this.ionTransactionListener });
        if (this.ionPublishListener != null)
            qos.setPublishListeners(new MkvPublishListener[] { (MkvPublishListener)this.ionPublishListener });
        String overrideComponentName = System.getProperty("mkv.overrideComponentName");
        if (overrideComponentName != null)
            logger.warn("mkv.overrideComponentName is no longer supported. Use -Dmkv.component={}", overrideComponentName);
        setComponentVersion(qos);
        try {
            Mkv mkv = Mkv.start(qos);
            MkvProperties properties = mkv.getProperties();
            logger.info("MkvProperty: ComponentName  [{}]", properties.getComponentName());
            logger.info("MkvProperty: mkv.cshost  [{}]", properties.getProperty("cshost"));
            logger.info("MkvProperty: mkv.csport  [{}]", properties.getProperty("csport"));
            logger.info("MkvProperty: mkv.user  [{}]", properties.getProperty("user"));
            mkvSaved = mkv;
            logger.info("saved mkv instance for latter use {}", mkv);
        } catch (MkvException e) {
            logger.error("Found error while connecting with ION platform", (Throwable)e);
        }
    }

    public void addTransactionHandler(IonTransactionHandler ionTransactionHandler, IonPublisher ionPublisher, String typeName, Class clazz) {
        this.ionTransactionListener.addTransactionHandler(ionTransactionHandler, ionPublisher, typeName, clazz);
    }

    private class MkvPlatformListenerImpl implements MkvPlatformListener {
        private final Logger logger = LoggerFactory.getLogger(MkvPlatformListenerImpl.class);

        private IonSessionListener ionSessionListener;

        private MkvPlatformListenerImpl(IonSessionListener ionSessionListener) {
            this.ionSessionListener = ionSessionListener;
        }

        public void onMain(MkvPlatformEvent mkvPlatformEvent) {
            switch (mkvPlatformEvent.intValue()) {
                case 0:
                    this.logger.info("MkvPlatformEvent: START");
                    return;
                case 3:
                    this.logger.info("MkvPlatformEvent: STOP");
                    IonSessionImpl.this.stopped = true;
                    if (IonSessionImpl.this.stopStateListener != null)
                        try {
                            IonSessionImpl.this.stopStateListener.onMkvStopped();
                        } catch (Exception e) {
                            this.logger.error("Exception from stopStateListener", e);
                        }
                    return;
                case 1:
                    this.logger.info("MkvPlatformEvent: REGISTER");
                    return;
                case 5:
                    this.logger.info("MkvPlatformEvent: IDLE_REGISTER will call ionSessionListener");
                    IonSessionImpl.this.isComponentProperlyRegistered.compareAndSet(false, true);
                    this.logger.info("isComponentProperlyRegistered set to 'true' now");
                    this.logger.info("Calling ionSessionListener->onIonConnected() for any additional tasks post mkv start.");
                    this.ionSessionListener.onIonConnected();
                    return;
                case 2:
                    this.logger.warn("MkvPlatformEvent: UNREGISTER");
                    this.logger.warn("(maybe bad user/password, licence problems or another component with the same name)");
                    return;
                case 4:
                    this.logger.info("MkvPlatformEvent: REGISTER_SWITCH");
                    return;
                case 6:
                    this.logger.info("MkvPlatformEvent: SHUTDOWN_REQUEST");
                    return;
            }
            this.logger.error("Unexpected mkvPlatformEvent found " + mkvPlatformEvent.intValue());
        }

        public void onComponent(MkvComponent mkvComponent, boolean b) {}

        public void onConnect(String s, boolean b) {}
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public void registerStopStateListener(StopStateListener stopStateListener) {
        this.stopStateListener = stopStateListener;
    }

    private static void setComponentVersion(MkvQoS qos) {
        String componentVersion = System.getProperty("VERSION");
        if (componentVersion == null || componentVersion.isEmpty()) {
            logger.info("VERSION system property not set. Will not set ION component version");
            return;
        }
        String[] parts = componentVersion.split("\\.");
        try {
            List<Integer> versionParts = (List<Integer>)Arrays.<String>stream(parts).map(x -> Integer.valueOf(x)).collect(Collectors.toList());
            int maj = (versionParts.size() > 0) ? ((Integer)versionParts.get(0)).intValue() : 0;
            int med = (versionParts.size() > 1) ? ((Integer)versionParts.get(1)).intValue() : 0;
            int min = (versionParts.size() > 2) ? ((Integer)versionParts.get(2)).intValue() : 0;
            int patch = (versionParts.size() > 3) ? ((Integer)versionParts.get(3)).intValue() : 0;
            qos.setComponentVersion(maj, med, min, patch);
            logger.info("Set ION component version from system VERSION property {} to {}.{}.{}.{}", new Object[] { componentVersion, Integer.valueOf(maj), Integer.valueOf(med), Integer.valueOf(min), Integer.valueOf(patch) });
        } catch (NumberFormatException exc) {
            logger.info("Unable to derive component version parts from {}. {}", componentVersion, exc.getMessage());
        }
    }
}
