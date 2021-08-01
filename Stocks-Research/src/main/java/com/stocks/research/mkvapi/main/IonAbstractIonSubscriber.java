package com.stocks.research.mkvapi.main;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.iontrading.mkv.MkvChain;
import com.iontrading.mkv.MkvRecord;
import com.iontrading.mkv.MkvSupply;
import com.iontrading.mkv.MkvType;
import com.iontrading.mkv.enums.MkvChainAction;
import com.iontrading.mkv.events.MkvChainListener;
import com.iontrading.mkv.events.MkvRecordListener;
import com.iontrading.mkv.exceptions.MkvException;
import com.iontrading.mkv.helper.MkvSubscribeProxy;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class IonAbstractIonSubscriber<T> implements IonSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(IonAbstractIonSubscriber.class);

    protected final Class<T> clazz;

    protected final MkvChainListenerImpl mkvDefaultChainListener;

    protected final MkvRecordListener mkvRecordListener;

    private final MkvSubscribeProxy proxy;

    protected List<String> fieldNames;

    RecordRecorder rd;

    public IonAbstractIonSubscriber(Class<T> clazz, IonRecordListener<T> ionRecordListener, IonChainListener ionChainListener) {
        Preconditions.checkNotNull(clazz);
        Preconditions.checkNotNull(ionRecordListener);
        Preconditions.checkNotNull(ionChainListener);
        this.mkvDefaultChainListener = new MkvChainListenerImpl(ionChainListener);
        this.clazz = clazz;
        if (clazz.equals(FieldMap.class)) {
            logger.info("detected field map subscription");
            this.mkvRecordListener = new MkvFieldMapRecordListenerImpl(ionRecordListener);
        } else {
            this.mkvRecordListener = new MkvRecordListenerImpl(ionRecordListener);
        }
        this.proxy = new MkvSubscribeProxy(clazz);
        logger.info("Initialized subscriber : " + this);
    }

    private void recordRecord(MkvRecord mkvRecord, List<String> fieldNames, boolean isSnapshot) {
        try {
            if (this.rd == null)
                this.rd = new RecordRecorder(mkvRecord, fieldNames);
            this.rd.recordRecordArrival(mkvRecord, isSnapshot);
        } catch (Exception e) {
            logger.error("failed to record record [{}]", mkvRecord.getName(), e);
        }
    }

    abstract class AbstractRecordListener implements MkvRecordListener {
        IonRecordListener ionRecordListener = null;

        public AbstractRecordListener(IonRecordListener<T> ionRecordListener) {
            this.ionRecordListener = ionRecordListener;
        }

        abstract void onFullRecordUpdate(MkvRecord param1MkvRecord, MkvSupply param1MkvSupply, boolean param1Boolean);

        public void onPartialUpdate(MkvRecord mkvRecord, MkvSupply mkvSupply, boolean b) {}

        public void onFullUpdate(MkvRecord mkvRecord, MkvSupply mkvSupply, boolean b) {
            IonAbstractIonSubscriber.logger.trace("onFullUpdate() called. Record [{}] ", mkvRecord.getName());
            if (RecordRecorder.isRecording())
                IonAbstractIonSubscriber.this.recordRecord(mkvRecord, IonAbstractIonSubscriber.this.fieldNames, b);
            onFullRecordUpdate(mkvRecord, mkvSupply, b);
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("ionRecordListener", this.ionRecordListener)
                    .toString();
        }
    }

    protected class MkvRecordListenerImpl extends AbstractRecordListener {
        public MkvRecordListenerImpl(IonRecordListener<T> ionRecordListener) {
            super(ionRecordListener);
        }

        public void onFullRecordUpdate(MkvRecord mkvRecord, MkvSupply mkvSupply, boolean b) {
            T genericRecordObj = null;
            try {
                genericRecordObj = IonAbstractIonSubscriber.this.clazz.newInstance();
                IonAbstractIonSubscriber.this.proxy.update(mkvRecord, mkvRecord.getSupply(), genericRecordObj);
            } catch (MkvException|InstantiationException|IllegalAccessException e) {
                IonAbstractIonSubscriber.logger.error("Found error. Just logging and taking no further action. ", e);
            }
            this.ionRecordListener.onRecordUpdate(mkvRecord.getName(), genericRecordObj);
        }
    }

    protected class MkvFieldMapRecordListenerImpl extends MkvRecordListenerImpl {
        public MkvFieldMapRecordListenerImpl(IonRecordListener<T> ionRecordListener) {
            super(ionRecordListener);
        }

        public void onFullRecordUpdate(MkvRecord mkvRecord, MkvSupply mkvSupply, boolean b) {
            if (mkvSupply == null) {
                IonAbstractIonSubscriber.logger.error("onFullUpdate got null supply!");
                return;
            }
            FieldMap fmap = new FieldMap();
            FieldMap partialFmap = new FieldMap();
            try {
                MkvType type = mkvRecord.getMkvType();
                MkvSupply fullSupply = mkvRecord.getSupply();
                IonHelper.fillFromSupply(fullSupply, type, fmap);
                if (b) {
                    fmap.setSnapshot(true);
                } else {
                    fmap.setSnapshot(false);
                    IonHelper.fillFromSupply(mkvSupply, type, fmap);
                    IonHelper.fillFromSupply(mkvSupply, type, partialFmap);
                }
                fmap.setPartialFieldMap(partialFmap);
                this.ionRecordListener.onRecordUpdate(mkvRecord.getName(), fmap);
            } catch (Exception e) {
                IonAbstractIonSubscriber.logger.error("failed to process record into FieldMap", e);
            }
        }
    }

    protected class MkvChainListenerImpl implements MkvChainListener {
        private final IonChainListener ionChainListener;

        MkvChainListenerImpl(IonChainListener ionChainListener) {
            this.ionChainListener = ionChainListener;
        }

        public void onSupply(MkvChain mkvChain, String recordName, int position, MkvChainAction mkvChainAction) {
            IonAbstractIonSubscriber.logger.debug("MkvChainListenerImpl -> onSupply() called. MkvChain ['{}'], Record ['{}']. Just logging this event.", mkvChain, recordName);
            try {
                switch (mkvChainAction.intValue()) {
                    case 5:
                        this.ionChainListener.onChainIdle(mkvChain.getName(), Integer.valueOf(mkvChain.size()));
                        break;
                }
            } catch (Exception e) {
                IonAbstractIonSubscriber.logger.error("exception in MkvChainListener", e);
            }
        }
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("clazz", this.clazz)
                .add("proxy", this.proxy)
                .toString();
    }
}
