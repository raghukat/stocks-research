//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.main.IonFunctionCallHandler;
import com.stocks.research.mkvapi.main.IonFunctionCallHandlerAsync;
import com.stocks.research.mkvapi.main.IonFunctionPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DummyFunctionPublisher implements IonFunctionPublisher {
    private static final Logger log = LoggerFactory.getLogger(DummyFileBasedTransactionCaller.class);
    private static ConcurrentMap<String, FunctionDetails> functions = new ConcurrentHashMap();

    public DummyFunctionPublisher() {
    }

    public void publishFunction(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, IonFunctionCallHandler ionFunctionCallHandler) {
        log.info("DummyFunctionPublisher publish [{}] returnType [{}] argNames [{}] callHandler [{}]", new Object[]{functionName, returnType, argNames, argTypes, ionFunctionCallHandler});
        functions.put(functionName, new FunctionDetails(returnType, ionFunctionCallHandler));
    }

    public void publishFunctionAsync(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, IonFunctionCallHandlerAsync ionFunctionCallHandler) {
        log.info("DummyFunctionPublisher publish [{}] returnType [{}] argNames [{}] callHandler [{}]", new Object[]{functionName, returnType, argNames, argTypes, ionFunctionCallHandler});
        functions.put(functionName, new FunctionDetails(returnType, ionFunctionCallHandler));
    }

    public void publishFunctionForTest(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, IonFunctionCallHandlerAsync ionFunctionCallListenerAsync) {
    }

    public static FunctionDetails lookupFunctionDetails(String functionName) {
        return (FunctionDetails)functions.get(functionName);
    }

    public static class FunctionDetails {
        private Class returnType;
        private IonFunctionCallHandler ionFunctionCallHandler;
        private IonFunctionCallHandlerAsync ionFunctionCallHandlerAsync;

        public FunctionDetails(Class returnType, IonFunctionCallHandler ionFunctionCallHandler) {
            this.returnType = returnType;
            this.ionFunctionCallHandler = ionFunctionCallHandler;
        }

        public FunctionDetails(Class returnType, IonFunctionCallHandlerAsync ionFunctionCallHandlerAsync) {
            this.returnType = returnType;
            this.ionFunctionCallHandlerAsync = ionFunctionCallHandlerAsync;
        }

        public Class getReturnType() {
            return this.returnType;
        }

        public IonFunctionCallHandler getIonFunctionCallHandler() {
            return this.ionFunctionCallHandler;
        }

        public IonFunctionCallHandlerAsync getIonFunctionCallHandlerAsync() {
            return this.ionFunctionCallHandlerAsync;
        }
    }
}
