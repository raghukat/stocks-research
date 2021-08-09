package com.stocks.research.mkvapi_dummy.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DummyFileBasedFunctionCallResultProvider implements DummyFunctionCallResultProvider {
    private static final Logger log = LoggerFactory.getLogger(DummyFileBasedTransactionCaller.class);

    private BufferedReader rd;

    private List<Object> returnValues;

    private String errorText;

    private int errorCode;

    public DummyFileBasedFunctionCallResultProvider() throws Exception {
        String recordingDir = System.getProperty("mkvapi.recordingDir");
        String fileName = "FUNCTION_CALLS.txt";
        if (recordingDir != null)
            fileName = recordingDir + File.separator + fileName;
        log.info("DummyFileBasedFunctionCallResultProvider, open  function-record-file [{}]", fileName);
        File fileNameFile = new File(fileName);
        if (!fileNameFile.exists()) {
            log.error("unable to open file {}", fileName);
        } else {
            this.rd = new BufferedReader(new FileReader(fileName));
        }
    }

    DummyFileBasedFunctionCallResultProvider(boolean testing) throws Exception {}

    public synchronized void onFunctionCalled(String functionName, List<Object> argValues) {
        log.info("onFunctionCalled [{]]", functionName);
        this.returnValues = null;
        this.errorText = null;
        this.errorCode = 0;
        try {
            if (this.rd == null) {
                log.warn("EOF met function-record-file, no more entries to process");
                log.warn("can't find function results for [{}]", functionName);
                return;
            }
            String s;
            while ((s = this.rd.readLine()) != null) {
                String callPrefix = "CALL: " + functionName;
                String returnprefix = "RETURN: " + functionName;
                if (s.startsWith(callPrefix)) {
                    log.info("found call entry: " + s);
                    continue;
                }
                if (s.startsWith(returnprefix)) {
                    log.info("found return entry: " + s);
                    captureRetrunValues(s);
                    notifySubscribers(functionName);
                    return;
                }
            }
            log.warn("EOF in function-record-file");
            this.rd.close();
            this.rd = null;
        } catch (Exception e) {
            log.error("error reading through function-record-file", e);
        }
        log.warn("can't find function results for [{}]", functionName);
    }

    private void notifySubscribers(String functionName) {
        Collection<DummyFileBasedSubscriber> subscribers = DummyFileBasedSubscriber.allSubscribers.values();
        for (DummyFileBasedSubscriber subscriber : subscribers)
            subscriber.notifyFunctionCalled(functionName);
    }

    void captureRetrunValues(String s) {
        int n = s.lastIndexOf("RESULT:");
        if (n > -1) {
            n += "RETURN:".length();
            setGoodResult(s.substring(n));
            return;
        }
        n = s.lastIndexOf("ERROR-CODE:");
        if (n > -1) {
            setErrorResult(s.substring(n));
            return;
        }
        log.error("unknown return-value string [{}]", s);
    }

    private void setErrorResult(String resultText) {
        int n = resultText.lastIndexOf("ERROR-CODE:");
        n += "ERROR-CODE:".length();
        int m = resultText.lastIndexOf("ERROR-TEXT:");
        String errorCodeStr = resultText.substring(n + 1, m).trim();
        this.errorCode = Integer.parseInt(errorCodeStr);
        m += "ERROR-TEXT:".length();
        this.errorText = resultText.substring(m).trim();
    }

    private void setGoodResult(String resultText) {
        resultText = resultText.replace("[", " ");
        resultText = resultText.replace("]", " ").trim();
        String[] ss = resultText.split("\\s*,\\s*");
        this.returnValues = new ArrayList();
        for (String s : ss)
            this.returnValues.add(s);
    }

    public String getErrorText() {
        return this.errorText;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public List<Object> getReturnValues() {
        return this.returnValues;
    }

    public boolean isError() {
        return (this.returnValues == null);
    }
}
