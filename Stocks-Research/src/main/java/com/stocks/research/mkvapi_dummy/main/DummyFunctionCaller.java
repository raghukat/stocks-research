//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import com.stocks.research.mkvapi.main.IonFunctionCaller;
import com.stocks.research.mkvapi.main.IonFunctionReturnListener;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DummyFunctionCaller implements IonFunctionCaller {
    private static final Logger log = LoggerFactory.getLogger(DummyFunctionCaller.class);
    DummyFunctionCallResultProvider dummtResultProvider;

    public DummyFunctionCaller(DummyFunctionCallResultProvider resultProvider) {
        this.dummtResultProvider = resultProvider;
    }

    public boolean doesFunctionExist(String functionName) {
        boolean exists = this.dummtResultProvider.doesFunctionExist(functionName);
        log.info("doesFunctionExist [{}] returns [{}]", functionName, exists);
        return true;
    }

    public void call(String functionName, @Nullable List<Object> argValues, IonFunctionReturnListener ionFunctionReturnListener) {
        log.info("call-function [{]] argValues=[{]] ionFunctionReturnListener=", new Object[]{functionName, argValues, ionFunctionReturnListener});
        log.info("using dummyResultProvider=[{}] for function [{}]", this.dummtResultProvider, functionName);
        this.dummtResultProvider.onFunctionCalled(functionName, argValues);
        if (this.dummtResultProvider.isError()) {
            log.info("dummyResultProvider returned error [{}] [{}] for function [{}]", new Object[]{this.dummtResultProvider.getErrorCode(), this.dummtResultProvider.getErrorText(), functionName});
            ionFunctionReturnListener.onFunctionReturnError(functionName, this.dummtResultProvider.getErrorCode(), this.dummtResultProvider.getErrorText());
        } else {
            log.info("dummyResultProvider returned sucess [{}] for function [{}]", this.dummtResultProvider.getReturnValues(), functionName);
            ionFunctionReturnListener.onFunctionReturnOk(functionName, this.dummtResultProvider.getReturnValues());
        }

    }
}
