package com.stocks.research.mkvapi.main;

import com.iontrading.mkv.MkvPublishManager;
import com.iontrading.mkv.MkvRecord;
import com.iontrading.mkv.MkvType;
import com.iontrading.mkv.enums.MkvFieldType;
import com.iontrading.mkv.exceptions.MkvException;
import com.stocks.research.mkvapi.exceptions.IonChainNotFoundException;
import com.stocks.research.mkvapi.exceptions.IonInvalidRecordOrChainNameException;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IonFieldMapPublisherImpl extends IonPublisherImpl<FieldMap> {
    private static final Logger logger = LoggerFactory.getLogger(IonAbstractIonPublisher.class);

    boolean publishedType;

    HashSet<String> publishedChains = new HashSet<>();

    public IonFieldMapPublisherImpl(Class<FieldMap> clazz) throws MkvException {
        super(clazz);
    }

    public void publishData(String recordName, FieldMap recordData, List<String> chains) throws MkvException, IonChainNotFoundException, IonInvalidRecordOrChainNameException {
        if (!this.publishedType) {
            publishType(recordData);
            this.publishedType = true;
        }
        if (chains != null)
            for (String chain : chains) {
                if (!this.publishedChains.contains(chain)) {
                    publishChain(chain);
                    this.publishedChains.add(chain);
                }
            }
        MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
        String[] fields = null;
        Object[] values = null;
        try {
            Set<Map.Entry<String, Value>> set = recordData.valueMap.entrySet();
            fields = new String[set.size()];
            values = new Object[set.size()];
            int n = 0;
            for (Map.Entry<String, Value> e : set) {
                fields[n] = e.getKey();
                if (fields[n] == null) {
                    logger.error("null key in field map at [{}]", Integer.valueOf(n));
                    logger.error("FieldMap Dump: {}", recordData);
                    throw new MkvException("failed to publish data: null key in field map");
                }
                values[n] = getTypeConvertedValue(e.getValue());
                if (fields[n] == null) {
                    logger.error("null value in field map at [{}]", Integer.valueOf(n));
                    logger.error("FieldMap Dump: {}", recordData);
                    throw new MkvException("failed to publish data: null value in field map");
                }
                n++;
            }
            MkvRecord record = publishManager.getMkvRecord(recordName);
            if (record == null) {
                record = new MkvRecord(recordName, this.typeName);
                record.publish();
                addRecordToChain(recordName, chains);
            }
            record.supply(fields, values);
            logger.debug("Published record ['{}'] with record data [{}]", recordName, recordData);
        } catch (MkvException e) {
            logger.error("Found error while publishing record data. Record name: ['{}'], record object: {}", recordName, recordData);
            for (int i = 0; i < fields.length; i++)
                logger.info("[" + i + "] [" + fields[i] + "]=[" + values[i] + "]");
            throw e;
        }
    }

    private Object getTypeConvertedValue(Value value) {
        if (value.getType().getTypeName().equals("java.lang.Double"))
            return Double.valueOf(value.getValue().toString());
        if (value.getType().getTypeName().equals("java.lang.Integer"))
            return Integer.valueOf(value.getStringValue());
        if (value.getType().getTypeName().equals("java.time.LocalDate"))
            return Integer.valueOf(value.getStringValue());
        if (value.getType().getTypeName().equals("java.time.LocalDateTime"))
            return Integer.valueOf(value.getStringValue());
        if (value.getType().getTypeName().equals("java.lang.String"))
            return value.getStringValue();
        return value.getValue();
    }

    MkvType publishType(FieldMap recordData) throws MkvException {
        logger.info("publish field map type [{}]", this.typeName);
        Set<Map.Entry<String, Value>> set = recordData.valueMap.entrySet();
        String[] fields = new String[set.size()];
        MkvFieldType[] types = new MkvFieldType[set.size()];
        int n = 0;
        for (Map.Entry<String, Value> e : set) {
            fields[n] = e.getKey();
            if (fields[n] == null) {
                logger.error("null key in field map at [{}]", Integer.valueOf(n));
                logger.error("FieldMap Dump: {}", recordData);
                throw new MkvException("failed to publish type: null key in field map");
            }
            types[n] = IonHelper.getMkvFieldType(((Value)e.getValue()).getType().getTypeName());
            if (types[n] == null) {
                logger.error("null value in field map at [{}]", Integer.valueOf(n));
                logger.error("FieldMap Dump: {}", recordData);
                throw new MkvException("failed to publish type: null value in field map");
            }
            logger.info("[" + n + "] publish type [" + fields[n] + "]->[" + types[n] + "]");
            n++;
        }
        MkvType mkvType = new MkvType(this.typeName, fields, types);
        mkvType.publish();
        return mkvType;
    }
}
