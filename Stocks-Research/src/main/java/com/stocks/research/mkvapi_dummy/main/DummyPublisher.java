//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.iontrading.mkv.exceptions.MkvException;
import com.stocks.research.mkvapi.exceptions.IonChainNotFoundException;
import com.stocks.research.mkvapi.exceptions.IonInvalidRecordOrChainNameException;
import com.stocks.research.mkvapi.exceptions.IonRecordNotFoundException;
import com.stocks.research.mkvapi.main.IonPublisher;
import com.stocks.research.mkvapi.main.IonRecordListener;
import com.stocks.research.mkvapi.main.IonTransactionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DummyPublisher<T> implements IonPublisher<T> {
    private static final Logger log = LoggerFactory.getLogger(DummyPublisher.class);
    static Map<Class, DummyPublisher> allPublishers = new ConcurrentHashMap();
    Map<String, HashSet<String>> patterns = new ConcurrentHashMap();
    Map<String, HashSet<String>> chains = new ConcurrentHashMap();
    Map<String, T> dataObects = new ConcurrentHashMap();
    Map<String, Map<String, Object>> dataMaps = new ConcurrentHashMap();
    IonTransactionHandler ionTransactionhandler;
    private Class clazz;
    private List<RecordSubscriptionItem> recordSubscriptions = new ArrayList();
    private List<ChainSubscriptionItem> chainSubscriptions = new ArrayList();

    public DummyPublisher(Class clazz) {
        this.clazz = clazz;
        allPublishers.put(clazz, this);
    }

    public Class getPublishedClass() {
        return this.clazz;
    }

    public void publishPattern(String recordPrefix) throws MkvException {
        this.patterns.put(recordPrefix, new HashSet());
    }

    public void publishChain(String chainName) throws MkvException {
        this.chains.put(chainName, new HashSet());
    }

    public void publishData(String recordName, T recordData, List<String> chains) throws MkvException, IonChainNotFoundException, IonInvalidRecordOrChainNameException {
        this.dataObects.put(recordName, recordData);
        this.dispatchToLoopbackRecordListeners(recordName, recordData);
        if (chains != null) {
            Iterator var4 = chains.iterator();

            while(var4.hasNext()) {
                String chain = (String)var4.next();
                Set<String> recordsOnChain = (Set)this.chains.get(chain);
                if (recordsOnChain == null) {
                    log.error("CHAIN NAME HAS NOT BEEN PUBLISHED [{}]", recordsOnChain);
                }

                recordsOnChain.add(recordName);
                this.dispatchToLoopbackChainListeners(chain, recordName, recordData);
            }
        }

    }

    public void publishData(String recordName, Map<String, Object> recordDataAsMap, List<String> chains) throws MkvException, IonChainNotFoundException, IonInvalidRecordOrChainNameException {
        this.dataMaps.put(recordName, recordDataAsMap);

        Set recordsOnChain;
        for(Iterator var4 = chains.iterator(); var4.hasNext(); recordsOnChain.add(recordName)) {
            String chain = (String)var4.next();
            recordsOnChain = (Set)this.chains.get(chain);
            if (recordsOnChain == null) {
                log.error("CHAIN NAME HAS NOT BEEN PUBLISHED [{}]", recordsOnChain);
            }
        }

    }

    public boolean unpublishRecord(String recordName) throws IonRecordNotFoundException {
        if (this.dataObects.remove(recordName) != null) {
            return true;
        } else {
            return this.dataMaps.remove(recordName) != null;
        }
    }

    public String getTypeName() {
        return this.clazz.getName();
    }

    public void setTransactionHandler(IonTransactionHandler ionTransactionhandler) {
        this.ionTransactionhandler = ionTransactionhandler;
    }

    public T lookupPublishedRecord(String recordName) {
        return this.dataObects.get(recordName);
    }

    public T lookupRecordOnChain(String recordName, String chainName) {
        HashSet<String> records = (HashSet)this.chains.get(chainName);
        if (records == null) {
            return null;
        } else {
            return !records.contains(recordName) ? null : this.dataObects.get(recordName);
        }
    }

    private void dispatchToLoopbackRecordListeners(String recordName, T recordData) {
        Iterator var3 = this.recordSubscriptions.iterator();

        while(var3.hasNext()) {
            RecordSubscriptionItem item = (RecordSubscriptionItem)var3.next();
            if (item.recordNames.contains(recordName)) {
                item.recordListsner.onRecordUpdate(recordName, recordData);
            }
        }

    }

    private void dispatchToLoopbackChainListeners(String chainName, String recordName, T recordData) {
        Iterator var4 = this.chainSubscriptions.iterator();

        while(var4.hasNext()) {
            ChainSubscriptionItem item = (ChainSubscriptionItem)var4.next();
            if (item.chainName.equals(chainName)) {
                item.recordListsner.onRecordUpdate(recordName, recordData);
            }
        }

    }

    public void addRecordListener(IonRecordListener<T> recordListsner, List<String> recordNames) {
        this.recordSubscriptions.add(new RecordSubscriptionItem(recordListsner, recordNames));
        HashSet<String> records = new HashSet();
        records.addAll(recordNames);
        Iterator var4 = this.dataObects.entrySet().iterator();

        while(var4.hasNext()) {
            Entry<String, T> e = (Entry)var4.next();
            if (records.contains(e.getKey())) {
                recordListsner.onRecordUpdate((String)e.getKey(), e.getValue());
            }
        }

    }

    public void addChainrecordListener(IonRecordListener<T> recordListsner, String chainName) {
        this.chainSubscriptions.add(new ChainSubscriptionItem(recordListsner, chainName));
        HashSet<String> records = (HashSet)this.chains.get(chainName);
        if (records != null) {
            Iterator var4 = records.iterator();

            while(var4.hasNext()) {
                String recordName = (String)var4.next();
                T dataObject = this.dataObects.get(recordName);
                if (dataObject == null) {
                    log.error("found null record for [{}] on chain [{}]", recordName, chainName);
                } else {
                    recordListsner.onRecordUpdate(recordName, dataObject);
                }
            }
        }

    }

    void republish(String recordName, T object) {
        log.info("republish record [{}] ", recordName);
        this.dataObects.put(recordName, object);
        this.dispatchToLoopbackRecordListeners(recordName, object);
        Set<Entry<String, HashSet<String>>> chainSet = this.chains.entrySet();
        Iterator var4 = chainSet.iterator();

        while(var4.hasNext()) {
            Entry<String, HashSet<String>> e = (Entry)var4.next();
            String chainName = (String)e.getKey();
            HashSet<String> recordsInChain = (HashSet)e.getValue();
            if (recordsInChain.contains(recordName)) {
                this.dispatchToLoopbackChainListeners(chainName, recordName, object);
            }
        }

    }

    public void addRecordToChain(String recordName, String chainName) {
        log.info("addRecordToChain recordName=[{}] chainName=[{}]", recordName, chainName);
    }

    public void removeRecordFromChain(String recordName, String chainName) {
        log.info("removeRecordFromChain recordName=[{}] chainName=[{}]", recordName, chainName);
    }

    private static class ChainSubscriptionItem<T> {
        IonRecordListener<T> recordListsner;
        String chainName;

        public ChainSubscriptionItem(IonRecordListener<T> recordListsner, String chainName) {
            this.recordListsner = recordListsner;
            this.chainName = chainName;
        }
    }

    private static class RecordSubscriptionItem<T> {
        IonRecordListener<T> recordListsner;
        HashSet<String> recordNames = new HashSet();

        public RecordSubscriptionItem(IonRecordListener<T> recordListsner, Collection<String> recordNames) {
            this.recordListsner = recordListsner;
            this.recordNames.addAll(recordNames);
        }
    }
}
