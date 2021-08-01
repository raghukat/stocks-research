package com.stocks.research.mkvapi.helper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.iontrading.mkv.Mkv;
import com.iontrading.mkv.MkvChain;
import com.iontrading.mkv.MkvPublishManager;
import com.iontrading.mkv.MkvRecord;
import com.iontrading.mkv.MkvSubscribeManager;
import com.iontrading.mkv.MkvSupply;
import com.iontrading.mkv.MkvType;
import com.iontrading.mkv.enums.MkvFieldType;
import com.iontrading.mkv.helper.MkvSubscribeProxy;
import com.stocks.research.mkvapi.factory.Ion;
import com.stocks.research.mkvapi.main.FieldMap;
import com.stocks.research.mkvapi.main.IonDataDictionaryListener;
import com.stocks.research.mkvapi.main.IonSession;
import com.stocks.research.mkvapi.main.IonSessionImpl;
import com.stocks.research.mkvapi.main.Value;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IonHelper {
    private static final Logger logger = LoggerFactory.getLogger(IonHelper.class);

    public static MkvFieldType getMkvFieldType(Class clazz) {
        String typeName = clazz.getName();
        return getMkvFieldType(typeName);
    }

    public static List<String> getFieldsAsString(Class<?> clazz) {
        Class<?> currentClazz = clazz;
        Set<String> allFields = Sets.newHashSet();
        while (currentClazz.getSuperclass() != null) {
            Set<? extends String> moreFields = (Set)Arrays.<Field>asList(currentClazz.getDeclaredFields()).stream().map(f -> f.getName()).collect(Collectors.toSet());
            allFields.addAll(moreFields);
            currentClazz = currentClazz.getSuperclass();
        }
        return Lists.newArrayList(allFields);
    }

    public static MkvFieldType getMkvFieldType(String typeName) {
        if (!Strings.isNullOrEmpty(typeName)) {
            switch (typeName) {
                case "java.lang.Integer":
                    return MkvFieldType.INT;
                case "int":
                    return MkvFieldType.INT;
                case "java.lang.Double":
                    return MkvFieldType.REAL;
                case "double":
                    return MkvFieldType.REAL;
                case "java.lang.String":
                    return MkvFieldType.STR;
                case "char":
                    return MkvFieldType.STR;
                case "java.lang.Boolean":
                    return MkvFieldType.INT;
                case "boolean":
                    return MkvFieldType.INT;
                case "java.time.LocalDate":
                    return MkvFieldType.DATE;
                case "java.time.LocalDateTime":
                    return MkvFieldType.TIME;
                case "java.time.Instant":
                    return MkvFieldType.TIME;
                case "java.util.Date":
                    return MkvFieldType.DATE;
            }
            logger.error("Field type '{}' can't be mapped to an mkv type. Defaulting it to MkvFieldType.STR", typeName);
            return MkvFieldType.STR;
        }
        logger.error("Can't find MkvFieldType. Supplied typeName was null or empty.");
        throw new RuntimeException("Can't find MkvFieldType. Supplied typeName was null or empty.");
    }

    public static boolean isValidChainName(String chainName) {
        boolean isMatched = Pattern.matches("^[^.]+\\.[^.]+\\.[^.]+\\.[^.]+$", chainName);
        if (isMatched)
            return true;
        logger.error("Invalid chain name : ['{}'] ", chainName);
        throw new RuntimeException("Invalid chain name : ['" + chainName + "'] ");
    }

    public static boolean isValidRecordName(String recordName) {
        return isValidChainName(recordName);
    }

    public static String getSourceFromRecordPrefix(String recordPrefix) {
        Pattern p = Pattern.compile("^([^.]+)\\.([^.]+)\\.([^.]+)\\.$");
        Matcher m = p.matcher(recordPrefix);
        if (m.find())
            return m.group(3);
        logger.error("Found error while parsing record prefix '{}' to fetch source. Expected format: 'CURRENCY.TABLE.SOURCE.'", recordPrefix);
        throw new RuntimeException("Found error while parsing record full path to fetch source. Expected format: 'CURRENCY.TABLE.SOURCE.'. Record : " + recordPrefix);
    }

    public static String getTableFromChainPath(String chain) {
        Pattern p = Pattern.compile("^\\s*([^.]+)\\.([^.]+)\\.([^.]+)\\.([^.]+)$");
        Matcher m = p.matcher(chain);
        if (m.find())
            return m.group(2);
        logger.error("Found error while parsing chain path '{}' to fetch table name. Expected format: 'CURRENCY.TABLE.SOURCE.CHAIN'", chain);
        throw new RuntimeException("Found error while parsing chain path " + chain + " to fetch table name. Expected format: 'CURRENCY.TABLE.SOURCE.CHAIN'.");
    }

    public static void sleep(long delayInMillis) {
        try {
            Thread.sleep(delayInMillis);
        } catch (InterruptedException e) {
            logger.error("Found error while trying Thread.sleep(delay)", e);
        }
    }

    public static boolean isAtleastOneNonEmpty(String firstString, String... strings) {
        if (!Strings.isNullOrEmpty(firstString))
            return true;
        if (strings != null)
            for (String str : strings) {
                if (!Strings.isNullOrEmpty(str))
                    return true;
            }
        return false;
    }

    public static <T> MkvSubscribeManager getMkvSubscribeManagerIfMkvSessionReady() {
        Preconditions.checkState(isMkvSessionReady(), String.format("Failed to fetch MkvSubscribeManager. Mkv session not yet initialized!", new Object[0]));
        return Mkv.getInstance().getSubscribeManager();
    }

    public static MkvPublishManager getMkvPublishManagerIfMkvSessionReady() {
        Preconditions.checkState(isMkvSessionReady(), String.format("Failed to fetch MkvPublishManager. Mkv session not yet initialized!", new Object[0]));
        Mkv mkv = Mkv.getInstance();
        if (mkv == null) {
            mkv = IonSessionImpl.mkvSaved;
            logger.warn("Mkv Instance is null! will use saved instance {}", mkv);
        }
        return mkv.getPublishManager();
    }

    public static boolean isMkvSessionReady() {
        IonSession ionSession = Ion.getSession();
        return ionSession.isMkvSessionReady();
    }

    public static String getChainFullPath(String recordPrefix, String chainName) {
        return recordPrefix + chainName;
    }

    public static String getMkvComponentName() {
        Preconditions.checkState(isMkvSessionReady(), String.format("Failed to fetch Mkv instance. Mkv session not yet initialized!", new Object[0]));
        return Mkv.getInstance().getProperties().getComponentName();
    }

    public static String getMkvProperty(String propertyName) {
        Preconditions.checkState(isMkvSessionReady(), String.format("Failed to fetch Mkv instance. Mkv session not yet initialized!", new Object[0]));
        return Mkv.getInstance().getProperties().getProperty(propertyName);
    }

    public static boolean isRecordNameValidForChain(String chainName, String recordName) {
        if (Strings.isNullOrEmpty(chainName) || Strings.isNullOrEmpty(recordName))
            return false;
        String delimRegex = "\\.";
        String[] chainNameParts = chainName.split(delimRegex);
        String[] recordNameParts = recordName.split(delimRegex);
        int comparePartCount = 3;
        if (chainNameParts.length < comparePartCount || recordNameParts.length < comparePartCount)
            return false;
        for (int i = 0; i < comparePartCount; i++) {
            if (!chainNameParts[i].equals(recordNameParts[i]))
                return false;
        }
        return true;
    }

    public static void startMkvAndAwaitReady() {
        startMkvAndAwaitReady(null);
    }

    public static void startMkvAndAwaitReady(IonDataDictionaryListener ionDataDictionaryListener) {
        IonSession ionSession = Ion.getSession();
        if (ionSession.isMkvSessionReady())
            return;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        if (ionDataDictionaryListener != null) {
            ionSession.start(() -> {
                logger.debug("ionSessionListener -> onIonConnected() called with IonDataDictionaryListener: {}", ionDataDictionaryListener.getClass().getName());
                countDownLatch.countDown();
            }, ionDataDictionaryListener);
        } else {
            ionSession.start(() -> {
                logger.debug("ionSessionListener -> onIonConnected() called.");
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            logger.error("Found error.", e);
        }
    }

    public static MkvRecord getMkvRecord(String recordName) {
        return getMkvPublishManagerIfMkvSessionReady().getMkvRecord(recordName);
    }

    public static MkvChain getMkvChain(String chainName) {
        return getMkvPublishManagerIfMkvSessionReady().getMkvChain(chainName);
    }

    public static <T> T getObjectRecord(String recordName, Class<T> clazz) {
        try {
            MkvRecord mkvRecord = getMkvPublishManagerIfMkvSessionReady().getMkvRecord(recordName);
            MkvSubscribeProxy proxy = new MkvSubscribeProxy(clazz);
            T object = clazz.newInstance();
            proxy.update(mkvRecord, mkvRecord.getSupply(), object);
            return object;
        } catch (Exception e) {
            logger.error("getObjectRecord(()) failed", recordName, e);
            return null;
        }
    }

    public static Date ionDataToJavaDate(int ionDate) {
        int year = ionDate / 10000;
        int month = (ionDate - year * 10000) / 100;
        int day = ionDate - year * 10000 - month * 100;
        Calendar cal = Calendar.getInstance();
        cal.set(1, year);
        cal.set(2, month - 1);
        cal.set(5, day);
        cal.set(11, 0);
        cal.set(12, 0);
        cal.set(13, 0);
        return cal.getTime();
    }

    public static Date ionTimeToJavaDate(int ionTime) {
        int hour = ionTime / 10000;
        int minute = (ionTime - hour * 10000) / 100;
        int second = ionTime - hour * 10000 - minute * 100;
        Calendar cal = Calendar.getInstance();
        cal.set(11, hour);
        cal.set(12, minute);
        cal.set(13, second);
        return cal.getTime();
    }

    public static boolean approxEquals(double d1, double d2) {
        return approxEquals(d1, d2, 1.0E-5D);
    }

    public static boolean approxEquals(double d1, double d2, double allowedDiff) {
        double diff = Math.abs(d1 - d2);
        return (diff <= allowedDiff);
    }

    public static void fillFromSupply(MkvSupply supply, MkvType type, FieldMap fmap) {
        int cursor = supply.firstIndex();
        while (cursor != -1) {
            String fname = type.getFieldName(cursor);
            try {
                Object object = supply.getObject(cursor);
                fmap.putValue(fname, getTypeConvertedObject(object, type.getFieldType(fname)));
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("Field not set")) {
                    logger.error("failed to get value for field [{}], cursor={} will continue to process other fields ({})", new Object[] { fname, Integer.valueOf(cursor), msg });
                } else {
                    logger.error("failed to get value for field [{}], cursor={} will continue to process other fields", new Object[] { fname, Integer.valueOf(cursor), e });
                }
            }
            cursor = supply.nextIndex(cursor);
        }
    }

    private static Value getTypeConvertedObject(Object object, MkvFieldType fieldType) {
        switch (fieldType.intValue()) {
            case 0:
                return Value.of(String.class, object.toString());
            case 1:
                return Value.of(Double.class, object);
            case 2:
                return Value.of(Integer.class, object);
            case 3:
                return Value.of(LocalDate.class, object);
            case 4:
                return Value.of(LocalDateTime.class, object);
        }
        return Value.of(String.class, object.toString());
    }

    public boolean isIonStopped() {
        return Ion.getSession().isStopped();
    }
}
