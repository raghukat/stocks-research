/*    */ package com.stocks.research.mkvapi.main;

import com.google.common.base.Preconditions;
import com.iontrading.mkv.MkvPublishManager;
import com.iontrading.mkv.MkvType;
import com.iontrading.mkv.exceptions.MkvException;
import com.iontrading.mkv.helper.MkvSupplyFactory;
import com.iontrading.mkv.helper.MkvSupplyProxy;
import com.stocks.research.mkvapi.helper.IonHelper;
import com.stocks.research.mkvapi.helper.IonRecordMetaDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class IonAbstractIonPublisher<T> implements IonPublisher<T> {
    protected final Class<T> clazz;

    protected String typeName;

    protected MkvSupplyProxy proxy;

    protected AtomicBoolean isPublishPattern = new AtomicBoolean();

    static AtomicInteger fieldMapTypeCounter = new AtomicInteger(0);

    private static final Logger logger = LoggerFactory.getLogger(IonAbstractIonPublisher.class);

    public IonAbstractIonPublisher(Class<T> clazz) throws MkvException {
        Preconditions.checkNotNull(clazz);
        this.clazz = clazz;
        publishType();
        logger.info("Initialized publisher : " + this);
    }

    protected MkvType publishType() throws MkvException {
        if (this.clazz.equals(FieldMap.class)) {
            this.typeName = IonHelper.getMkvComponentName() + "_FLDMAP_" + fieldMapTypeCounter.getAndIncrement();
            return null;
        }
        this.typeName = IonHelper.getMkvComponentName() + "_" + this.clazz.getSimpleName().toUpperCase();
        MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
        MkvType mkvType = publishManager.getMkvType(this.typeName);
        if (mkvType == null) {
            mkvType = new MkvType(this.typeName, IonRecordMetaDataRepository.INSTANCE.getFieldNames(this.clazz), IonRecordMetaDataRepository.INSTANCE.getFieldTypes(this.clazz));
            mkvType.publish();
        } else {
            logger.warn("MkvType ['{}'] already published. Ignoring publication request.", mkvType);
        }
        this.proxy = MkvSupplyFactory.create(mkvType, this.clazz);
        return mkvType;
    }

    public AtomicBoolean isPublishPattern() {
        return this.isPublishPattern;
    }
}
