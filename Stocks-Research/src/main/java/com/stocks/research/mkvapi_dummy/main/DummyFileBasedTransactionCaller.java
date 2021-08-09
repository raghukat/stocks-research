//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.main.IonTransactionCaller;
import com.stocks.research.mkvapi.main.IonTransactionResultListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class DummyFileBasedTransactionCaller implements IonTransactionCaller {
    private static final Logger log = LoggerFactory.getLogger(DummyFileBasedTransactionCaller.class);

    public DummyFileBasedTransactionCaller() {
    }

    public boolean doesRecordExist(String recordName) {
        Object object = this.getRecrodFromSubscriber(recordName);
        return object != null;
    }

    public void apply(String recordName, Map<String, Object> fields) {
        this.apply(recordName, fields, (IonTransactionResultListener)null);
    }

    public void apply(String recordName, Map<String, Object> fields, IonTransactionResultListener transactionResultListener) {
        Object object = this.getRecrodFromSubscriber(recordName);
        DummyFileBasedSubscriber subscriber = this.getSubscriber(recordName);

        try {
            ObjectUpdater objectUpdater = new ObjectUpdater(subscriber.getSubscribedClass());
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

        subscriber.notifyTransaction(recordName, object);
    }

    Object getRecrodFromSubscriber(String recordName) {
        Collection<DummyFileBasedSubscriber> subscribers = DummyFileBasedSubscriber.allSubscribers.values();
        Iterator var3 = subscribers.iterator();

        Object object;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            DummyFileBasedSubscriber subscriber = (DummyFileBasedSubscriber)var3.next();
            object = subscriber.lookupRecord(recordName);
        } while(object == null);

        return object;
    }

    DummyFileBasedSubscriber getSubscriber(String recordName) {
        Collection<DummyFileBasedSubscriber> subscribers = DummyFileBasedSubscriber.allSubscribers.values();
        Iterator var3 = subscribers.iterator();

        DummyFileBasedSubscriber subscriber;
        Object object;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            subscriber = (DummyFileBasedSubscriber)var3.next();
            object = subscriber.lookupRecord(recordName);
        } while(object == null);

        return subscriber;
    }
}
