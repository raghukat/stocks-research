//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;

import java.util.List;

public interface DummyFunctionCallResultProvider {
    void onFunctionCalled(String var1, List<Object> var2);

    String getErrorText();

    int getErrorCode();

    List<Object> getReturnValues();

    boolean isError();

    default boolean doesFunctionExist(String functionName) {
        return true;
    }
}
