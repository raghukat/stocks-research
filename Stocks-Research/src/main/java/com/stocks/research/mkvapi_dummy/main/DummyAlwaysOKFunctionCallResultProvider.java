package com.stocks.research.mkvapi_dummy.main;

import java.util.ArrayList;
import java.util.List;

public class DummyAlwaysOKFunctionCallResultProvider implements DummyFunctionCallResultProvider {
    public void onFunctionCalled(String functionName, List<Object> argValues) {}

    public String getErrorText() {
        return null;
    }

    public int getErrorCode() {
        return 0;
    }

    public List<Object> getReturnValues() {
        ArrayList<Object> results = new ArrayList();
        results.add("0");
        return results;
    }

    public boolean isError() {
        return false;
    }
}
