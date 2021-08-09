package com.stocks.research.mkvapi.main;

import com.iontrading.mkv.MkvFunction;
import com.iontrading.mkv.MkvSupply;
import com.iontrading.mkv.enums.MkvFieldType;
import com.iontrading.mkv.events.MkvFunctionCallEvent;
import com.iontrading.mkv.events.MkvFunctionCallListener;
import com.iontrading.mkv.exceptions.MkvObjectNotAvailableException;
import com.iontrading.mkv.helper.MkvSupplyFactory;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class IonFunctionCallerImpl implements IonFunctionCaller {
    private static final Logger logger = LoggerFactory.getLogger(IonFunctionCallerImpl.class);

    static class MyMkvFunctionCallListener implements MkvFunctionCallListener {
        private IonFunctionReturnListener ionFunctionReturnListener;

        private String functionName;

        public MyMkvFunctionCallListener(IonFunctionReturnListener ionFunctionReturnListener, String functionName) {
            this.ionFunctionReturnListener = ionFunctionReturnListener;
            this.functionName = functionName;
        }

        public void onResult(MkvFunctionCallEvent mkvFunctionCallEvent, MkvSupply mkvSupply) {
            IonFunctionCallerImpl.logger.info("function returned result [" + this.functionName + "]");
            try {
                List<Object> returnValues = new ArrayList();
                int start = mkvSupply.firstIndex();
                int end = mkvSupply.lastIndex();
                for (int i = start; i <= end; i++) {
                    MkvFieldType type = mkvSupply.getType(i);
                    Object arg = mkvSupply.getObject(i);
                    IonFunctionCallerImpl.logger.info("got result value [{}] type=[{}] value=[{}] class=[{}]", new Object[] { Integer.valueOf(i), type, arg, arg.getClass().getName() });
                    returnValues.add(arg);
                }
                this.ionFunctionReturnListener.onFunctionReturnOk(this.functionName, returnValues);
                if (RecordRecorder.isRecording())
                    RecordRecorder.recordFunctionResult(this.functionName, returnValues);
            } catch (Exception e) {
                IonFunctionCallerImpl.logger.error("exception while processing function call result", e);
            }
        }

        public void onError(MkvFunctionCallEvent mkvFunctionCallEvent, byte code, String errorText) {
            IonFunctionCallerImpl.logger.info("function returned error [" + this.functionName + "] [{}] [{}]", Byte.valueOf(code), errorText);
            try {
                this.ionFunctionReturnListener.onFunctionReturnError(this.functionName, code, errorText);
                if (RecordRecorder.isRecording())
                    RecordRecorder.recordFunctionResult(this.functionName, code, errorText);
            } catch (Exception e) {
                IonFunctionCallerImpl.logger.error("exception while processing function call error result", e);
            }
        }
    }

    public boolean doesFunctionExist(String functionName) {
        MkvFunction mkvFunc = IonHelper.getMkvPublishManagerIfMkvSessionReady().getMkvFunction(functionName);
        if (mkvFunc == null) {
            logger.info("function " + functionName + " not available");
            return false;
        }
        return true;
    }

    public void call(String functionName, List<Object> argValues, IonFunctionReturnListener ionFunctionReturnListener) {
        try {
            logger.info("call function [{}] argc-count={}", functionName, Integer.valueOf(argValues.size()));
            MkvFunction mkvFunc = IonHelper.getMkvPublishManagerIfMkvSessionReady().getMkvFunction(functionName);
            if (mkvFunc == null) {
                logger.info("function " + functionName + " not available");
                throw new RuntimeException("Function [" + functionName + "] does not exist on the platform");
            }
            if (RecordRecorder.isRecording())
                RecordRecorder.recordFunctionCall(functionName, mkvFunc, argValues);
            Object[] oargs = new Object[argValues.size()];
            oargs = argValues.toArray(oargs);
            MkvSupply supplyArgs = MkvSupplyFactory.create(oargs);
            MyMkvFunctionCallListener functionCallListener = new MyMkvFunctionCallListener(ionFunctionReturnListener, functionName);
            mkvFunc.call(supplyArgs, functionCallListener);
            logger.info("call function [{}]...call done", functionName);
        } catch (MkvObjectNotAvailableException|com.iontrading.mkv.exceptions.MkvInvalidSupplyException|com.iontrading.mkv.exceptions.MkvConnectionException e) {
            logger.error("Exception from Ion Function Call [{}]", functionName, e);
            throw new RuntimeException("Exception from Ion Function Call " + functionName, e);
        }
    }
}
