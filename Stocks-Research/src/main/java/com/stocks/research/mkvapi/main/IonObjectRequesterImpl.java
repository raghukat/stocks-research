package com.stocks.research.mkvapi.main;

import com.google.common.base.Preconditions;
import com.iontrading.mkv.MkvPublishManager;
import com.iontrading.mkv.MkvRecord;
import com.iontrading.mkv.MkvSupply;
import com.iontrading.mkv.MkvType;
import com.iontrading.mkv.exceptions.MkvException;
import com.iontrading.mkv.helper.MkvSubscribeProxy;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IonObjectRequesterImpl implements IonObjectRequester {
    private static final Logger log = LoggerFactory.getLogger(IonObjectRequesterImpl.class);

    public <T> List<T> getRecords(List<String> recordNames, List<String> fieldNames, Class<T> clazz) {
        Preconditions.checkNotNull(recordNames);
        MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
        MkvSubscribeProxy proxy = getMkvSubscribeProxyOrNull(clazz);
        List<MkvRecord> mkvRecords = (List<MkvRecord>)recordNames.stream().map(recordName -> {
            MkvRecord mkvRecord = publishManager.getMkvRecord(recordName);
            if (mkvRecord == null)
                log.error("No record found for id: [{}]. Will skip this record.", mkvRecord);
            return mkvRecord;
        }).collect(Collectors.toList());
        List<T> newRecords = (List<T>)mkvRecords.stream().filter(mkvRecord -> (mkvRecord != null)).map(mkvRecord -> {
            T newRecordObj = null;
            if (clazz.equals(FieldMap.class)) {
                newRecordObj = getObj(clazz);
                MkvType type = mkvRecord.getMkvType();
                MkvSupply fullSupply = mkvRecord.getSupply();
                IonHelper.fillFromSupply(fullSupply, type, (FieldMap)newRecordObj);
            } else {
                newRecordObj = getObj(clazz);
                try {
                    proxy.update(mkvRecord, mkvRecord.getSupply(), newRecordObj);
                } catch (MkvException e) {
                    log.error("Found error. Just logging and taking no further action. ", (Throwable)e);
                }
            }
            return (Function)newRecordObj;
        }).collect(Collectors.toList());
        log.debug("##getRecords: returning {} record(s)", Integer.valueOf(newRecords.size()));
        return newRecords;
    }

    private <T> MkvSubscribeProxy getMkvSubscribeProxyOrNull(Class<T> clazz) {
        if (!clazz.equals(FieldMap.class))
            return new MkvSubscribeProxy(clazz);
        return null;
    }

    private <T> T getObj(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException|IllegalAccessException e) {
            log.error("Found error. Just logging and taking no further action. ", e);
            return null;
        }
    }
}
