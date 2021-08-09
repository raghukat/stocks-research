package com.stocks.research.mkvapi.main;

import com.iontrading.mkv.*;
import com.iontrading.mkv.enums.MkvChainAction;
import com.iontrading.mkv.events.MkvChainListener;
import com.iontrading.mkv.events.MkvRecordListener;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ManualSubscriptionManagerImpl implements ManualSubscriptionManager {
    private static final Logger logger = LoggerFactory.getLogger(ManualSubscriptionManagerImpl.class);

    private class RecordListener implements MkvRecordListener {
        private RecordListener() {}

        public void onPartialUpdate(MkvRecord mkvRecord, MkvSupply mkvSupply, boolean b) {}

        public void onFullUpdate(MkvRecord mkvRecord, MkvSupply mkvSupply, boolean b) {
            String rname = mkvRecord.getName();
            ManualSubscriptionManagerImpl.logger.debug("onFullUpdate [{}]", rname);
            ManualSubscriptionManagerImpl.Item item = (ManualSubscriptionManagerImpl.Item)ManualSubscriptionManagerImpl.this.records.get(rname);
            if (item != null && item.listener != null) {
                MkvType type = mkvRecord.getMkvType();
                MkvSupply fullSupply = mkvRecord.getSupply();
                FieldMap fmap = new FieldMap();
                IonHelper.fillFromSupply(fullSupply, type, fmap);
                item.listener.onRecordUpdate(rname, fmap);
            }
        }
    }

    private RecordListener recordListener = new RecordListener();

    private class ChainListener implements MkvChainListener {
        String name;

        IonRecordListener<FieldMap> ionRecordListener;

        public ChainListener(String name, IonRecordListener<FieldMap> ionRecordListener) {
            this.name = name;
            this.ionRecordListener = ionRecordListener;
        }

        public void onSupply(MkvChain mkvChain, String recordName, int position, MkvChainAction mkvChainAction) {
            if (mkvChainAction == MkvChainAction.APPEND || mkvChainAction == MkvChainAction.INSERT) {
                ManualSubscriptionManagerImpl.logger.info("record [{}] added to chain [{}]", recordName, mkvChain.getName());
                ManualSubscriptionManagerImpl.this.addRecordSubscriptionEnty(recordName, this.ionRecordListener);
                ManualSubscriptionManagerImpl.this.doSubscribeRecord(recordName);
            } else if (mkvChainAction == MkvChainAction.IDLE) {
                ManualSubscriptionManagerImpl.logger.info("chain is idle [{}], subscribing to all records", mkvChain.getName());
                for (int i = 0; i < mkvChain.size(); i++) {
                    String rname = (String)mkvChain.get(i);
                    ManualSubscriptionManagerImpl.this.addRecordSubscriptionEnty(rname, this.ionRecordListener);
                    ManualSubscriptionManagerImpl.this.doSubscribeRecord(rname);
                }
            }
        }
    }

    private static class Item {
        String name;

        IonRecordListener<FieldMap> listener;

        private Item() {}
    }

    private HashMap<String, Item> chains = new HashMap<>();

    private HashMap<String, Item> records = new HashMap<>();

    private void doSubscribeRecord(String recordName) {
        MkvRecord mkvRecord = Mkv.getInstance().getPublishManager().getMkvRecord(recordName);
        if (mkvRecord == null)
            logger.error("record does not exist [{}]", recordName);
        if (!mkvRecord.isSubscribed()) {
            logger.info("subscribing: [{}]", recordName);
            try {
                mkvRecord.subscribe(this.recordListener);
            } catch (Exception e) {
                logger.error("error subscribing record [{}]", recordName, e);
            }
        }
    }

    public void subscribeChain(String chain, IonRecordListener<FieldMap> ionRecordListener) {
        logger.info("subscribeChain [{}]", chain);
        MkvPublishManager pubMan = Mkv.getInstance().getPublishManager();
        MkvChain mkvChain = pubMan.getMkvChain(chain);
        Item item = new Item();
        item.name = chain;
        item.listener = ionRecordListener;
        this.chains.put(chain, item);
        if (mkvChain != null) {
            try {
                if (!mkvChain.isSubscribed()) {
                    mkvChain.subscribe(new ChainListener(chain, ionRecordListener));
                    logger.info("subscribeChain [{}] chain now subscribed", chain);
                } else {
                    logger.info("subscribeChain [{}] already subscribed", chain);
                }
            } catch (Exception e) {
                logger.error("chain subscription error", e);
            }
        } else {
            logger.info("subscribeChain [{}] chain not yet available", chain);
        }
    }

    void addRecordSubscriptionEnty(String record, IonRecordListener<FieldMap> ionRecordListener) {
        Item item = this.records.get(record);
        if (item == null) {
            item = new Item();
            item.name = record;
            item.listener = ionRecordListener;
            this.records.put(record, item);
        }
    }

    public void subscribeRecord(String record, IonRecordListener<FieldMap> ionRecordListener) {
        addRecordSubscriptionEnty(record, ionRecordListener);
        doSubscribeRecord(record);
    }

    public void onRecordAvailable(String record) {
        Item item = this.records.get(record);
        if (item != null)
            synchronized (item) {
                doSubscribeRecord(record);
            }
    }

    public void onFunctionAvailable(String name) {}

    public void onChainAvailable(String name) {
        Item item = this.chains.get(name);
        if (item != null) {
            logger.info("onChainAvailable [{}]", name);
            synchronized (item) {
                MkvChain mkvChain = Mkv.getInstance().getPublishManager().getMkvChain(name);
                if (!mkvChain.isSubscribed())
                    try {
                        if (!mkvChain.isSubscribed()) {
                            mkvChain.subscribe(new ChainListener(name, item.listener));
                            logger.info("subscribeChain [{}] chain now subscribed", name);
                        } else {
                            logger.info("subscribeChain [{}] already subscribed", name);
                        }
                    } catch (Exception e) {
                        logger.error("chain subscription error", e);
                    }
            }
        }
    }

    public void onPatternAvailable(String name) {}

    public void onRecordRemoved(String name) {}

    public void onFunctionRemoved(String name) {}

    public void onChainRemoved(String name) {}

    public void onPatternRemoved(String name) {}
}
