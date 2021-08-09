//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.main.IonTransactionCaller;
import com.stocks.research.mkvapi.main.IonTransactionResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DummyLoopbackTransactionCaller implements IonTransactionCaller {
    private static final Logger log = LoggerFactory.getLogger(DummyLoopbackTransactionCaller.class);

    public DummyLoopbackTransactionCaller() {
    }

    public boolean doesRecordExist(String recordName) {
        return this.getRecrodFromPublisher(recordName) != null;
    }

    public void apply(String recordName, Map<String, Object> fields) {
        this.apply(recordName, fields, (IonTransactionResultListener)null);
    }

    public void apply(String recordName, Map<String, Object> fields, IonTransactionResultListener transactionResultListener) {
        DummyPublisher publisher = this.getPublisher(recordName);
        Object object = this.getRecrodFromPublisher(recordName);

        try {
            ObjectUpdater objectUpdater = new ObjectUpdater(publisher.getPublishedClass());
            Iterator var7 = fields.entrySet().iterator();

            while(var7.hasNext()) {
                Entry<String, Object> e = (Entry)var7.next();
                objectUpdater.updateField(object, (String)e.getKey(), e.getValue().toString());
            }
        } catch (Exception var9) {
            log.error("Exception Applying Transaction Updates In Dummy recordName [{]]", recordName, var9);
        }

        if (transactionResultListener != null) {
            transactionResultListener.onTransactionCallResult(recordName, 0, "OK");
        }

        publisher.republish(recordName, object);
    }

    Object getRecrodFromPublisher(String recordName) {
        Set<Entry<Class, DummyPublisher>> set = DummyPublisher.allPublishers.entrySet();
        Iterator var3 = set.iterator();

        Object object;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            Entry<Class, DummyPublisher> e = (Entry)var3.next();
            DummyPublisher publisher = (DummyPublisher)e.getValue();
            object = publisher.lookupPublishedRecord(recordName);
        } while(object == null);

        return object;
    }

    DummyPublisher getPublisher(String recordName) {
        Set<Entry<Class, DummyPublisher>> set = DummyPublisher.allPublishers.entrySet();
        Iterator var3 = set.iterator();

        DummyPublisher publisher;
        Object object;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            Entry<Class, DummyPublisher> e = (Entry)var3.next();
            publisher = (DummyPublisher)e.getValue();
            object = publisher.lookupPublishedRecord(recordName);
        } while(object == null);

        return publisher;
    }
}
