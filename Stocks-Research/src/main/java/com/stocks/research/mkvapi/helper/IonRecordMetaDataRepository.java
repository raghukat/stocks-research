package com.stocks.research.mkvapi.helper;

import com.iontrading.mkv.enums.MkvFieldType;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum IonRecordMetaDataRepository {
    INSTANCE;

    Map<String, IonRecordObjectMetaData> ionRecordObjectMetaDataMap;

    IonRecordMetaDataRepository() {
        this.ionRecordObjectMetaDataMap = new ConcurrentHashMap<>();
    }

    public String[] getFieldNames(Class clazz) {
        IonRecordObjectMetaData ionRecordObjectMetaData = this.ionRecordObjectMetaDataMap.get(clazz.getSimpleName());
        if (ionRecordObjectMetaData == null)
            ionRecordObjectMetaData = initialiseFieldsFromObject(clazz);
        return ionRecordObjectMetaData.getFieldNameList();
    }

    public MkvFieldType[] getFieldTypes(Class clazz) {
        IonRecordObjectMetaData ionRecordObjectMetaData = this.ionRecordObjectMetaDataMap.get(clazz.getSimpleName());
        if (ionRecordObjectMetaData == null)
            ionRecordObjectMetaData = initialiseFieldsFromObject(clazz);
        return ionRecordObjectMetaData.getFieldTypeList();
    }

    private IonRecordObjectMetaData initialiseFieldsFromObject(Class clazz) {
        IonRecordObjectMetaData ionRecordObjectMetaData = new IonRecordObjectMetaData(clazz.getSimpleName());
        for (Field field : clazz.getDeclaredFields())
            ionRecordObjectMetaData.setFieldNameAndType(field.getName(),
                    IonHelper.getMkvFieldType(field.getType().getTypeName()));
        this.ionRecordObjectMetaDataMap.put(ionRecordObjectMetaData.getRecordName(), ionRecordObjectMetaData);
        return ionRecordObjectMetaData;
    }

    private class IonRecordObjectMetaData {
        private String recordName;

        private Map<String, MkvFieldType> fieldTypeMap = new HashMap<>();

        public IonRecordObjectMetaData(String recordName) {
            this.recordName = recordName;
        }

        public String getRecordName() {
            return this.recordName;
        }

        public void setRecordName(String recordName) {
            this.recordName = recordName;
        }

        public Map<String, MkvFieldType> getFieldTypeMap() {
            return this.fieldTypeMap;
        }

        public void setFieldTypeMap(Map<String, MkvFieldType> fieldTypeMap) {
            this.fieldTypeMap = fieldTypeMap;
        }

        public void setFieldNameAndType(String fieldName, MkvFieldType mkvFieldType) {
            this.fieldTypeMap.put(fieldName, mkvFieldType);
        }

        public String[] getFieldNameList() {
            return (String[])this.fieldTypeMap.keySet().toArray((Object[])new String[0]);
        }

        public MkvFieldType[] getFieldTypeList() {
            return (MkvFieldType[])this.fieldTypeMap.values().toArray((Object[])new MkvFieldType[0]);
        }
    }
}
