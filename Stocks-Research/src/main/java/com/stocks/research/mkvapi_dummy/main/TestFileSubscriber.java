package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.factory.Ion;
import com.stocks.research.mkvapi.main.IonSubscriber;

import static com.stocks.research.mkvapi.helper.IonHelper.getFieldsAsString;

public class TestFileSubscriber {
    public TestFileSubscriber(){
        subscribeToCmTradeSplitChain("USD.CM_TRADESPLIT.BBG_STP.TRADESPLIT");
    }


    private void subscribeToCmTradeSplitChain(String chain) {
        try {
            IonSubscriber subscriber = Ion.getSubscriber(CmTradeSplitRecord.class,
                    (recordName, T) -> {
                        try {

                            System.out.println("recordName = " + recordName);
                            System.out.println("CmTradeSplitRecord = " + T);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
            subscriber.subscribeChain(chain, getFieldsAsString(CmTradeSplitRecord.class));
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
