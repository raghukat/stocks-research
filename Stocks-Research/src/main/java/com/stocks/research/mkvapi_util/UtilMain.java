//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_util;

import com.stocks.research.mkvapi.factory.Ion;
import com.stocks.research.mkvapi.helper.IonHelper;
import com.stocks.research.mkvapi.main.FieldMap;
import com.stocks.research.mkvapi.main.IonSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilMain {
    private static final Logger log = LoggerFactory.getLogger(UtilMain.class);

    public UtilMain() {
    }

    public static void main(String[] args) throws Exception {
        log.info("starting ION...");
        IonHelper.startMkvAndAwaitReady();
        log.info("starting ION...READY");
        SubscribeSpec spec = new SubscribeSpec("src/test/resources/bond_price_comparison.txt");
        ComparativeFieldMapRecordListener recordListener = new ComparativeFieldMapRecordListener(spec.chain1, spec.chain2, spec.fields, spec.reportName);
        log.info("COMPARE [{}] [{}]", spec.chain1, spec.chain2);
        log.info("FIELDS [{}] REPORT [{}]", spec.fields, spec.reportName);
        IonSubscriber subscriber1 = Ion.getSubscriber(FieldMap.class, recordListener);
        Thread.sleep(10000L);
        IonSubscriber subscriber2 = Ion.getSubscriber(FieldMap.class, recordListener);
        subscriber1.subscribeChain(spec.chain1, spec.fields);
        subscriber2.subscribeChain(spec.chain2, spec.fields);
    }
}
