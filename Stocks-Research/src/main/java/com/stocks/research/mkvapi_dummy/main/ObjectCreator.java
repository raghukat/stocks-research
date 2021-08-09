package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.main.FieldMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ObjectCreator<T> extends ObjectUpdater<T> {
    private static final Logger log = LoggerFactory.getLogger(ObjectUpdater.class);

    private CsvHeader csvHeader;

    public ObjectCreator(Class<T> clazz, CsvHeader header) {
        super(clazz);
        this.csvHeader = header;
    }

    public T createFromCsvRow(String csvValues) {
        String[] values = this.csvHeader.split(csvValues);
        return createFromCsvRow(values);
    }

    public T createFromCsvRow(String[] values) {
        if (values.length == 0) {
            log.warn("no value fields found, will return null object values.length={}", Integer.valueOf(values.length));
            return null;
        }
        String fieldName = null;
        String valueStr = null;
        int pos = -1;
        try {
            this.objectId = null;
            Set<Map.Entry<String, Integer>> csvFields = this.csvHeader.getFields();
            T instance = this.clazz.newInstance();
            boolean snapshot = true;
            for (Map.Entry<String, Integer> e : csvFields) {
                fieldName = e.getKey();
                pos = ((Integer)e.getValue()).intValue();
                if (pos > values.length - 1) {
                    log.warn("detected short line values.length={} nothing at pos={} field={}", new Object[] { Integer.valueOf(values.length), Integer.valueOf(pos), fieldName });
                    valueStr = "";
                } else {
                    valueStr = values[pos];
                }
                if ("snapshot".equals(fieldName)) {
                    snapshot = Boolean.valueOf(valueStr).booleanValue();
                } else {
                    updateField(instance, fieldName, valueStr);
                }
                if ("recordid".equals(fieldName))
                    this.objectId = valueStr;
            }
            if (this.objectId == null)
                this.objectId = "ID_" + UUID.randomUUID().toString();
            if (instance instanceof FieldMap) {
                FieldMap map = (FieldMap)instance;
                map.setSnapshot(snapshot);
                if (snapshot)
                    map.setPartialFieldMap(map);
            }
            return instance;
        } catch (Exception e) {
            log.error("error processing object, field=[{}] value=[{}] pos=[{}]", new Object[] { fieldName, valueStr, Integer.valueOf(pos), e });
            return null;
        }
    }
}
