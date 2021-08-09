package com.stocks.research.mkvapi.main;

import com.iontrading.mkv.MkvCallerInfo;
import com.iontrading.mkv.MkvFunction;
import com.iontrading.mkv.MkvSupply;
import com.iontrading.mkv.enums.MkvFieldType;
import com.iontrading.mkv.events.MkvFunctionCallEvent;
import com.iontrading.mkv.events.MkvFunctionListener;
import com.iontrading.mkv.exceptions.MkvException;
import com.iontrading.mkv.helper.MkvSupplyFactory;
import com.stocks.research.mkvapi.factory.Ion;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class IonFunctionPublisherImpl implements IonFunctionPublisher {
    private static final Logger logger = LoggerFactory.getLogger(IonFunctionPublisherImpl.class);

    static class MyMkvFunctionListener implements MkvFunctionListener {
        private IonFunctionCallHandler ionFunctionCallHandler;

        private IonFunctionCallHandlerAsync ionFunctionCallHandlerAsync;

        private String name;

        public MyMkvFunctionListener(IonFunctionCallHandler ionFunctionCallHandler, String name) {
            this.ionFunctionCallHandler = ionFunctionCallHandler;
            this.name = name;
        }

        public MyMkvFunctionListener(IonFunctionCallHandlerAsync ionFunctionCallHandlerAsync, String name) {
            this.ionFunctionCallHandlerAsync = ionFunctionCallHandlerAsync;
            this.name = name;
        }

        public void onCall(MkvFunctionCallEvent mkvFunctionCallEvent) {
            try {
                IonFunctionPublisherImpl.logger.info("Function Call Received [" + this.name + "]");
                String caller = mkvFunctionCallEvent.getCaller();
                MkvCallerInfo callerInfo = mkvFunctionCallEvent.getCallerInfo();
                int id = mkvFunctionCallEvent.getId();
                IonFunctionPublisherImpl.logger.info("Function Call [{}] caller=[{}], callerInfo={} id=[{}]", new Object[] { this.name, caller, callerInfo, Integer.valueOf(id) });
                MkvSupply supply = mkvFunctionCallEvent.getArgs();
                List<Object> args = new ArrayList();
                int start = supply.firstIndex();
                int end = supply.lastIndex();
                for (int i = start; i <= end; i++) {
                    MkvFieldType type = supply.getType(i);
                    Object arg = supply.getObject(i);
                    IonFunctionPublisherImpl.logger.info("got function arg [{}] type=[{}] value=[{}] class=[{}]", new Object[] { Integer.valueOf(i), type, arg, arg.getClass().getName() });
                    args.add(arg);
                }
                IonFunctionResult r = null;
                if (this.ionFunctionCallHandlerAsync != null) {
                    this.ionFunctionCallHandlerAsync.onFunctionCalled(caller, id, args, mkvFunctionCallEvent);
                    IonFunctionPublisherImpl.logger.info("function [{}] call is aync, result pending", this.name);
                } else {
                    r = this.ionFunctionCallHandler.onFunctionCalled(caller, args);
                    if (r.returnValues == null) {
                        String errorText = (r.errorText == null) ? ("ERROR:" + r.returnCode) : r.errorText;
                        IonFunctionPublisherImpl.logger.info("function [{}] return error code [{}] [{}]", new Object[] { this.name, Byte.valueOf(r.returnCode), errorText });
                        mkvFunctionCallEvent.setError(r.returnCode, errorText);
                    } else {
                        Object[] returnValues = r.returnValues.toArray(new Object[r.returnValues.size()]);
                        IonFunctionPublisherImpl.logger.info("function [{}] return values {}", this.name, r.returnValues);
                        MkvSupply returnSupply = MkvSupplyFactory.create(returnValues);
                        mkvFunctionCallEvent.setResult(returnSupply);
                    }
                }
            } catch (Exception e) {
                IonFunctionPublisherImpl.logger.error("error handling ion function call [" + this.name + "][" + this.ionFunctionCallHandler + "]", e);
                try {
                    mkvFunctionCallEvent.setError((byte)-1, "INTERNAl ERROR");
                } catch (Exception x) {
                    IonFunctionPublisherImpl.logger.error("additional exception trying to respon to function call [" + this.name + "]", e);
                }
            }
        }
    }

    public void publishFunction(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, IonFunctionCallHandler ionFunctionCallListener) {
        MyMkvFunctionListener mkvFuncListener = new MyMkvFunctionListener(ionFunctionCallListener, functionName);
        doPublishFunction(functionName, returnType, argNames, argTypes, mkvFuncListener, true);
    }

    public void publishFunctionAsync(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, IonFunctionCallHandlerAsync ionFunctionCallListenerAsync) {
        MyMkvFunctionListener mkvFuncListener = new MyMkvFunctionListener(ionFunctionCallListenerAsync, functionName);
        doPublishFunction(functionName, returnType, argNames, argTypes, mkvFuncListener, true);
    }

    public void publishFunctionForTest(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, IonFunctionCallHandlerAsync ionFunctionCallListenerAsync) {
        MyMkvFunctionListener mkvFuncListener = new MyMkvFunctionListener(ionFunctionCallListenerAsync, functionName);
        doPublishFunction(functionName, returnType, argNames, argTypes, mkvFuncListener, false);
    }

    private void doPublishFunction(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, MyMkvFunctionListener mkvFuncListener, boolean useComponentName) {
        if (useComponentName)
            functionName = Ion.getSession().getComponentName() + "_" + functionName;
        doPublishFunction(functionName, returnType, argNames, argTypes, mkvFuncListener);
    }

    private void doPublishFunction(String functionName, Class returnType, List<String> argNames, List<Class> argTypes, MyMkvFunctionListener mkvFuncListener) {
        try {
            logger.info("publish function [{}]", functionName);
            MkvFieldType mkvReturnType = IonHelper.getMkvFieldType(returnType);
            MkvFieldType[] mkvArgTypes = new MkvFieldType[argTypes.size()];
            for (int i = 0; i < mkvArgTypes.length; i++)
                mkvArgTypes[i] = IonHelper.getMkvFieldType(argTypes.get(i));
            String[] mkvArgNames = argNames.<String>toArray(new String[argNames.size()]);
            MkvFunction mkvFunction = new MkvFunction(functionName, mkvReturnType, mkvArgNames, mkvArgTypes, "func: " + functionName, mkvFuncListener);
            mkvFunction.publish();
            logger.info("function is published [{}]", functionName);
        } catch (MkvException e) {
            logger.error("failed to publish function [{}]", functionName, e);
            throw new RuntimeException(e);
        }
    }

    static List<Object> singleArgReturn(Object o) {
        ArrayList<Object> a = new ArrayList(1);
        a.add(o);
        return a;
    }
}
