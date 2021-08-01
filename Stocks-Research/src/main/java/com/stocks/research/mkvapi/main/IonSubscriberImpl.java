package com.stocks.research.mkvapi.main;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.iontrading.mkv.*;
import com.iontrading.mkv.events.MkvRecordListener;
import com.iontrading.mkv.exceptions.MkvException;
import com.stocks.research.mkvapi.exceptions.IonChainAlreadySubscribedException;
import com.stocks.research.mkvapi.exceptions.IonPatternSubscribeFailedException;
import com.stocks.research.mkvapi.exceptions.IonRecordUnsubscribeFailedException;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class IonSubscriberImpl<T> extends IonAbstractIonSubscriber<T> {
    private static final Logger logger = LoggerFactory.getLogger(IonSubscriberImpl.class);

    Map<String, MkvPersistentChain> persistentChainMap = new ConcurrentHashMap<>();

    Map<IonPersistentRecordSubscriptionKey, MkvPersistentRecordSet> persistentRecordSubscriptionMap = new ConcurrentHashMap<>();

    Boolean isSubscribePattern;

    public IonSubscriberImpl(Class<T> clazz, IonRecordListener<T> ionRecordListener, IonChainListener ionChainListener) {
        super(clazz, ionRecordListener, ionChainListener);
    }

    public void subscribeRecords(List<String> recordNames, List<String> fieldNames) {
        Preconditions.checkNotNull(recordNames);
        for (String recordName : recordNames) {
            Preconditions.checkArgument(IonHelper.isValidRecordName(recordName));
            MkvPersistentRecordSet persistentRecordSet = lookupOrCreateMkvPersistentRecordSet(fieldNames);
            persistentRecordSet.add(recordName);
            logger.debug("Subscribed to record : ['{}']", recordName);
        }
    }

    public synchronized void unsubscribeAllRecords() {
        for (MkvPersistentRecordSet mkvPersistentRecordSet : this.persistentRecordSubscriptionMap.values()) {
            if (mkvPersistentRecordSet.isSubscriptionActive())
                mkvPersistentRecordSet.unsubscribe();
            logger.debug("Unsubscribed all records from [{}]", mkvPersistentRecordSet);
        }
        this.persistentRecordSubscriptionMap.clear();
    }

    public void unsubscribeRecords(List<String> recordNames, List<String> fieldNames) throws IonRecordUnsubscribeFailedException {
        Preconditions.checkNotNull(recordNames);
        IonPersistentRecordSubscriptionKey subscriptionKey = new IonPersistentRecordSubscriptionKey(this.mkvRecordListener, fieldNames);
        MkvPersistentRecordSet persistentRecordSet = this.persistentRecordSubscriptionMap.get(subscriptionKey);
        if (persistentRecordSet == null) {
            logger.error("No persistent subscription found for given key [{}]", subscriptionKey);
            throw new IonRecordUnsubscribeFailedException(String.format("No persistent subscription found for given key [%s]", new Object[] { subscriptionKey }));
        }
        for (String recordName : recordNames) {
            if (IonHelper.isValidRecordName(recordName)) {
                if (persistentRecordSet.isSubscribed(recordName)) {
                    persistentRecordSet.remove(recordName);
                    logger.debug("Successfully unsubscribed record {}", recordNames);
                    continue;
                }
                logger.warn("Record ['{}'] was not subscribed, so ignoring unsubscribe request.", recordName);
                continue;
            }
            logger.error("Invalid record name ['{}']. Skipping unsubscribe request for this record.", recordName);
        }
    }

    public void subscribeChain(String chainName, List<String> fieldNames) throws IonChainAlreadySubscribedException {
        Preconditions.checkArgument((chainName != null && IonHelper.isValidChainName(chainName)));
        this.fieldNames = fieldNames;
        MkvPersistentChain persistentChain = lookupOrCreateMkvPersistentChain(chainName);
        logger.info("Successfully created persistent chain : ['{}'] for chain : ['{}'] and fields : ['{}']", new Object[] { persistentChain, chainName, fieldNames });
    }

    public void unsubscribeChain(String chainName) {
        MkvPersistentChain persistentChain = this.persistentChainMap.get(chainName);
        if (persistentChain == null) {
            logger.warn("Unsubscribe chain request failed. Chain ['{}'] not found.", chainName);
        } else {
            persistentChain.close();
            this.persistentChainMap.remove(chainName);
            logger.debug("Chain ['{}'] successfully unsubscribed.", chainName);
        }
    }

    public void unsubscribeAllChain() {
        for (MkvPersistentChain persistentChain : this.persistentChainMap.values()) {
            persistentChain.close();
            logger.debug("Closed persistentChain [{}]", persistentChain);
        }
        this.persistentChainMap.clear();
    }

    public boolean subscribePattern(String recordPrefix) throws IonPatternSubscribeFailedException {
        MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
        MkvPattern pattern = publishManager.getMkvPattern(recordPrefix);
        if (pattern != null) {
            try {
                pattern.subscribe(this.mkvRecordListener);
            } catch (MkvException e) {
                logger.error("Found error while subscribing to Pattern '{}'.", recordPrefix, e);
                throw new IonPatternSubscribeFailedException(String.format("Found error while subscribing to Pattern '%s'.", new Object[] { recordPrefix }), e);
            }
            logger.debug("Subscribed to pattern : '{}'", recordPrefix);
            return true;
        }
        logger.warn("Pattern '{}' not found so couldn't subscribe to it.", recordPrefix);
        return false;
    }

    public boolean subscribePattern(String recordPrefix, List<String> fields) throws IonPatternSubscribeFailedException {
        MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
        String[] arrayAttr = new String[fields.size()];
        String[] fieldArray = fields.<String>toArray(arrayAttr);
        MkvPattern pattern = publishManager.getMkvPattern(recordPrefix);
        if (pattern != null) {
            try {
                pattern.subscribe(fieldArray, this.mkvRecordListener);
            } catch (MkvException e) {
                logger.error("Found error while subscribing to Pattern '{}'.", recordPrefix, e);
                throw new IonPatternSubscribeFailedException(String.format("Found error while subscribing to Pattern '%s'.", new Object[] { recordPrefix }), e);
            }
            logger.debug("Subscribed to pattern : '{}' for fields ['{}]", recordPrefix, fields);
            return true;
        }
        logger.warn("Pattern '{}' not found so couldn't subscribe to it.", recordPrefix);
        return false;
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("fieldNames", this.fieldNames)
                .add("isSubscribePattern", this.isSubscribePattern)
                .add("mkvDefaultChainListener", this.mkvDefaultChainListener)
                .toString();
    }

    private MkvPersistentRecordSet lookupOrCreateMkvPersistentRecordSet(List<String> fieldNames) {
        IonPersistentRecordSubscriptionKey subscriptionKey = new IonPersistentRecordSubscriptionKey(this.mkvRecordListener, fieldNames);
        MkvPersistentRecordSet persistentRecordSet = this.persistentRecordSubscriptionMap.get(subscriptionKey);
        if (persistentRecordSet == null) {
            MkvSubscribeManager subscribeManager = IonHelper.getMkvSubscribeManagerIfMkvSessionReady();
            if (fieldNames == null) {
                persistentRecordSet = subscribeManager.persistentSubscribe(this.mkvRecordListener);
            } else {
                persistentRecordSet = subscribeManager.persistentSubscribe(this.mkvRecordListener, fieldNames
                        .<String>toArray(new String[0]));
            }
            MkvPersistentRecordSet persistentRecordSetPrevious = this.persistentRecordSubscriptionMap.putIfAbsent(subscriptionKey, persistentRecordSet);
            if (persistentRecordSetPrevious != null)
                persistentRecordSet = persistentRecordSetPrevious;
        }
        return persistentRecordSet;
    }

    private MkvPersistentChain lookupOrCreateMkvPersistentChain(String chainName) {
        MkvPersistentChain persistentChain = this.persistentChainMap.get(chainName);
        if (persistentChain == null) {
            MkvSubscribeManager subscribeManager = IonHelper.getMkvSubscribeManagerIfMkvSessionReady();
            if (this.fieldNames == null) {
                persistentChain = subscribeManager.persistentSubscribe(chainName, this.mkvDefaultChainListener, this.mkvRecordListener);
            } else {
                persistentChain = subscribeManager.persistentSubscribe(chainName, this.mkvDefaultChainListener, this.mkvRecordListener, this.fieldNames.<String>toArray(new String[0]));
            }
            MkvPersistentChain persistentChainPrevious = this.persistentChainMap.putIfAbsent(chainName, persistentChain);
            if (persistentChainPrevious != null)
                persistentChain = persistentChainPrevious;
        }
        return persistentChain;
    }

    private static class IonPersistentRecordSubscriptionKey {
        MkvRecordListener mkvRecordListener = null;

        List<String> fieldNames = null;

        public IonPersistentRecordSubscriptionKey(MkvRecordListener mkvRecordListener, List<String> fieldNames) {
            this.mkvRecordListener = mkvRecordListener;
            this.fieldNames = fieldNames;
        }

        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (obj == this)
                return true;
            if (!(obj instanceof IonPersistentRecordSubscriptionKey))
                return false;
            return (Objects.equals(this.mkvRecordListener, this.mkvRecordListener) &&
                    Objects.equals(this.fieldNames, this.fieldNames));
        }

        public int hashCode() {
            return Objects.hash(new Object[] { this.mkvRecordListener, this.fieldNames });
        }

        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("mkvRecordListener", this.mkvRecordListener)
                    .add("fieldNames", this.fieldNames)
                    .toString();
        }
    }
}
