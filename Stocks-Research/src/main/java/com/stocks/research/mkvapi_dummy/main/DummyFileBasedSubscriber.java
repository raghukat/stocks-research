//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.exceptions.IonChainAlreadySubscribedException;
import com.stocks.research.mkvapi.exceptions.IonPatternSubscribeFailedException;
import com.stocks.research.mkvapi.exceptions.IonRecordUnsubscribeFailedException;
import com.stocks.research.mkvapi.factory.Ion;
import com.stocks.research.mkvapi.main.IonChainListener;
import com.stocks.research.mkvapi.main.IonRecordListener;
import com.stocks.research.mkvapi.main.IonSubscriber;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

public class DummyFileBasedSubscriber<T> implements IonSubscriber {
    private static final Logger log = LoggerFactory.getLogger(DummyFileBasedSubscriber.class);
    static ConcurrentMap<Class, DummyFileBasedSubscriber> allSubscribers = new ConcurrentHashMap();
    private Class<T> clazz;
    private IonRecordListener<T> ionRecordListener;
    private ConcurrentMap<String, T> recordsReceived = new ConcurrentHashMap();
    private static CountDownLatch latch;
    private static String dataDirectory;
    private static String defaultExtension1 = ".csv";
    private static String defaultExtension2 = ".rcd";
    private static TxFuncLock txLock;
    HashMap<String, TxFuncLock> functionLocks = new HashMap();
    private volatile boolean eof;
    private volatile int recordCount;
    private String component;
    private IonChainListener ionChainListener;

    public static void setDataDirectory(String dir) {
        dataDirectory = dir;
    }

    public static void setStartupLatch(CountDownLatch startupLatch) {
        latch = startupLatch;
    }

    public static void setDefaultExtension(String ext) {
        defaultExtension1 = ext;
    }

    public DummyFileBasedSubscriber(Class<T> clazz, IonRecordListener<T> ionRecordListener, IonChainListener ionChainListener) {
        this.clazz = clazz;
        this.ionRecordListener = ionRecordListener;
        this.ionChainListener = ionChainListener;
        allSubscribers.put(clazz, this);
    }

    public Class<T> getSubscribedClass() {
        return this.clazz;
    }

    public String getComponent() {
        return this.component;
    }

    public T lookupRecord(String recordName) {
        return this.recordsReceived.get(recordName);
    }

    public void notifyTransaction(String recordName, T object) {
        log.info("TX NOTIFIED [{}] {}", recordName, object);
        this.signalTxlock(txLock);
    }

    private TxFuncLock getOrCreateFunctionLock(String functionName) {
        synchronized(this.functionLocks) {
            TxFuncLock lock = (TxFuncLock)this.functionLocks.get(functionName);
            if (lock == null) {
                log.info("create function lock [{}]", functionName);
                lock = new TxFuncLock(functionName);
                this.functionLocks.put(functionName, lock);
            }

            return lock;
        }
    }

    public void notifyFunctionCalled(String functionName) {
        log.info("FUNC NOTIFIED [{}] ", functionName);
        TxFuncLock lock = this.getOrCreateFunctionLock(functionName);
        this.signalTxlock(lock);
    }

    public void subscribeChain(String chainName, @Nullable List<String> fieldNames) throws IonChainAlreadySubscribedException {
        log.info("subscribeChain [{}]", chainName);
        this.dispatchRecords(chainName);
    }

    public void subscribeRecords(List<String> recordNames, @Nullable List<String> fieldNames) {
        log.info("subscribeRecords [{}]", recordNames);
        HashSet<String> recordFiles = new HashSet();
        Iterator var4 = recordNames.iterator();

        String recordfile;
        while(var4.hasNext()) {
            recordfile = (String)var4.next();
            int n = recordfile.lastIndexOf(".");
            String recordBase = recordfile.substring(0, n);
            recordFiles.add(recordBase);
        }

        var4 = recordFiles.iterator();

        while(var4.hasNext()) {
            recordfile = (String)var4.next();
            this.dispatchRecords(recordfile);
        }

    }

    public void unsubscribeAllRecords() {
    }

    public void unsubscribeAllChain() {
    }

    public boolean subscribePattern(String recordPrefix) throws IonPatternSubscribeFailedException {
        log.info("subscribePattern [{}]", recordPrefix);
        this.dispatchRecords(recordPrefix);
        return true;
    }

    public boolean subscribePattern(String recordPrefix, List<String> fields) throws IonPatternSubscribeFailedException {
        return false;
    }

    public void unsubscribeChain(String chainName) {
    }

    public void unsubscribeRecords(List<String> recordNames, @Nullable List<String> fieldNames) throws IonRecordUnsubscribeFailedException {
    }

    private void dispatchRecords(String recordFile) {
        Thread t = new Thread(() -> {
            this.doDispatchRecords(recordFile);
        });
        t.setName("RecordFileLoader");
        t.setDaemon(true);
        t.start();
    }

    private String workoutPathname(String recordFile) {
        String pathName = dataDirectory == null ? recordFile : dataDirectory + File.separator + recordFile;
        File f = new File(pathName);
        if (f.exists()) {
            return pathName;
        } else {
            log.info("no file [{}] will try wth {}", pathName, defaultExtension1);
            String altPath = pathName + defaultExtension1;
            f = new File(altPath);
            if (f.exists()) {
                return altPath;
            } else {
                log.info("no file [{}] will try wth {}", altPath, defaultExtension2);
                altPath = pathName + defaultExtension2;
                f = new File(altPath);
                if (f.exists()) {
                    return altPath;
                } else {
                    log.info("no file [{}] nothing else to try", altPath);
                    return pathName;
                }
            }
        }
    }

    private void doDispatchRecords(String recordFile) {
        try {
            if (latch != null) {
                latch.await();
            }

            String pathName = this.workoutPathname(recordFile);
            File file = new File(pathName);
            if (!file.exists()) {
                log.error("unable to open file {}", recordFile);
                return;
            }

            String recordPrefix = file.getName();
            int lineNum;
            if (recordPrefix != null) {
                lineNum = recordPrefix.lastIndexOf(".");
                recordPrefix = recordPrefix.substring(0, lineNum);
            }

            lineNum = 0;
            log.info("will subscribe records from file [{}] recordPrefix=[{}]", pathName, recordPrefix);
            log.info("current directory [{}]", System.getProperty("user.dir"));
            ObjectCreator<T> objectCreator = null;
            long sleepMs = 0L;
            CsvHeader csvHeader = new CsvHeader();
            BufferedReader rd = new BufferedReader(new FileReader(pathName));
            String readAheadLine = null;
            long lastTimestamp = 0L;
            String chainName = null;

            while(true) {
                String line;
                if (readAheadLine != null) {
                    line = readAheadLine;
                    readAheadLine = null;
                    ++lineNum;
                } else {
                    line = rd.readLine();
                    ++lineNum;
                }

                if (line == null) {
                    log.info("EOF in subscriber file, recordCount={}", this.recordCount);
                    this.eof = true;
                    this.ionChainListener.onChainIdle(chainName, this.recordsReceived.size());
                    break;
                }

                line = line.trim();
                if (line.length() != 0 && !line.startsWith("#")) {
                    if (line.startsWith("@fieldtype")) {
                        line = rd.readLine();
                        log.info("skipping @fieldtype definition [{}]", line);
                        ++lineNum;
                    } else if (line.startsWith("@sleep")) {
                        sleepMs = this.getSleepValue(line);
                        log.info("update sleepMs [{}]", sleepMs);
                    } else if (line.startsWith("@record-prefix")) {
                        recordPrefix = this.getRecordPrefix(line);
                        log.info("update recordPrefix [{}]", recordPrefix);
                    } else if (line.startsWith("@chain-name")) {
                        chainName = this.getRecordPrefix(line);
                        log.info("update chainName [{}]", chainName);
                    } else if (line.startsWith("@component")) {
                        this.component = this.getComponent(line);
                        log.info("update component [{}]", this.component);
                    } else if (line.startsWith("@header")) {
                        if (csvHeader.isInitialized()) {
                            log.info("## detected an appended file, will reset time-stamp and sleep 1s before continuing");
                            rd.readLine();
                            lastTimestamp = 0L;
                            Thread.sleep(1000L);
                            ++lineNum;
                        }
                    } else if (line.startsWith("@timestamp")) {
                        long newTimestamp = this.getTimestamp(line);
                        if (lastTimestamp > 0L) {
                            long pauseTimeMs = newTimestamp - lastTimestamp;
                            Thread.sleep(pauseTimeMs);
                        }

                        lastTimestamp = newTimestamp;
                    } else if (!csvHeader.isInitialized()) {
                        csvHeader.init(line);
                        objectCreator = new ObjectCreator(this.clazz, csvHeader);
                    } else {
                        if (sleepMs > 0L) {
                            Thread.sleep(sleepMs);
                        }

                        readAheadLine = rd.readLine();
                        boolean shouldAwaitTx = false;
                        boolean shouldAwaitFunc = false;
                        TxFuncLock funcLock = null;
                        if (readAheadLine != null && readAheadLine.startsWith("@await-tx")) {
                            shouldAwaitTx = true;
                            readAheadLine = null;
                            this.primeTxlock(txLock);
                        } else if (readAheadLine != null && readAheadLine.startsWith("@await-func")) {
                            shouldAwaitFunc = true;
                            readAheadLine = null;
                            String funcName = this.getFunctionName(readAheadLine);
                            funcLock = this.getOrCreateFunctionLock(funcName);
                            this.primeTxlock(funcLock);
                        }

                        T object = objectCreator.createFromCsvRow(line);
                        if (object == null) {
                            log.warn("got null object from lineNum=[{}] text=[{}], will ignore line", lineNum, line);
                        } else {
                            DummySessionImpl dummySession = (DummySessionImpl) Ion.getSession();
                            if (dummySession == null) {
                                log.error("internal error: unexpected null dummySession");
                            }

                            String id = objectCreator.getObjectId();
                            String recordId = recordPrefix + "." + id;
                            String[] split = id.split("[.]");
                            if (split.length == 4) {
                                recordId = id;
                            }

                            this.recordsReceived.put(recordId, object);
                            ++this.recordCount;
                            log.info("posting (dummy) record event lineNum=[{}] id={} recordId={}", new Object[]{lineNum, id, recordId});
                            dummySession.postEvent(this.ionRecordListener, object, recordId);
                            if (shouldAwaitTx) {
                                this.awaitTxLock(txLock);
                            }

                            if (shouldAwaitFunc) {
                                this.awaitTxLock(funcLock);
                            }
                        }
                    }
                }
            }
        } catch (Throwable var24) {
            log.error("error from dispatch thread, thread will exit", var24);
        }

    }

    private String getFunctionName(String readAheadLine) {
        String[] ss = readAheadLine.trim().split("\\s* \\s*");
        return ss[1];
    }

    public static Logger getLog() {
        return log;
    }

    public boolean isEof() {
        return this.eof;
    }

    public int getRecordCount() {
        return this.recordCount;
    }

    private long getSleepValue(String sleepText) {
        String[] ss = sleepText.split("\\s* \\s*");
        return (long)Integer.parseInt(ss[1]);
    }

    private long getTimestamp(String timestampText) {
        String[] ss = timestampText.split("\\s* \\s*");
        return Long.parseLong(ss[1]);
    }

    private String getRecordPrefix(String prefixText) {
        String[] ss = prefixText.split("\\s* \\s*");
        return ss[1];
    }

    private String getChainName(String prefixText) {
        String[] ss = prefixText.split("\\s* \\s*");
        return ss[1];
    }

    private String getComponent(String componentText) {
        String[] ss = componentText.split("\\s* \\s*");
        return ss[1];
    }

    private void primeTxlock(TxFuncLock theLock) {
        log.info(theLock.name + ": LOCK prime");
        synchronized(txLock) {
            txLock.waiting = true;
        }
    }

    private void signalTxlock(TxFuncLock theLock) {
        log.info(theLock.name + ": LOCK signal");
        synchronized(theLock) {
            theLock.waiting = false;
            theLock.notifyAll();
        }
    }

    private void awaitTxLock(TxFuncLock theLock) {
        try {
            log.info(theLock.name + ": LOCK waiting");
            synchronized(txLock) {
                while(true) {
                    if (!txLock.waiting) {
                        synchronized(this.functionLocks) {
                            this.functionLocks.remove(txLock.name);
                            break;
                        }
                    }

                    txLock.wait();
                }
            }

            log.info(theLock.name + ": LOCK waiting done");
        } catch (InterruptedException var8) {
            log.warn("awaitTxlock InterruptedException");
        }

    }

    static {
        String s = System.getProperty("mkvapi.factory.data-directory");
        if (s != null) {
            dataDirectory = s.trim();
            log.info("set dataDirectory [{}] from system property [{}]", dataDirectory, "mkvapi.factory.data-directory");
        }

        txLock = new TxFuncLock("TX");
    }

    static class TxFuncLock {
        String name;
        boolean waiting;

        public TxFuncLock(String name) {
            this.name = name;
        }
    }
}
