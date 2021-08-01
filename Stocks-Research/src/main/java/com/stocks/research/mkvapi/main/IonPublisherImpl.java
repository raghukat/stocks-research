package com.stocks.research.mkvapi.main;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.iontrading.mkv.*;
import com.iontrading.mkv.exceptions.MkvException;
import com.iontrading.mkv.exceptions.MkvObjectNotAvailableException;
import com.stocks.research.mkvapi.exceptions.IonChainNotFoundException;
import com.stocks.research.mkvapi.exceptions.IonInvalidRecordOrChainNameException;
import com.stocks.research.mkvapi.exceptions.IonRecordNotFoundException;
import com.stocks.research.mkvapi.factory.Ion;
import com.stocks.research.mkvapi.helper.IonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class IonPublisherImpl<T> extends IonAbstractIonPublisher<T> {
    private static final Logger logger = LoggerFactory.getLogger(IonPublisherImpl.class);

    private Lock lock = new ReentrantLock();

    public IonPublisherImpl(Class<T> clazz) throws MkvException {
        super(clazz);
    }

    public void publishPattern(String recordPrefix) throws MkvException {
        try {
            MkvPattern pattern = new MkvPattern(recordPrefix, this.typeName);
            pattern.publish();
            this.isPublishPattern.compareAndSet(false, true);
        } catch (MkvException e) {
            logger.error("Found error while publishing pattern for record prefix ['{}']", recordPrefix, e);
            throw e;
        }
    }

    public void publishChain(String chainName) throws MkvException {
        if (this.typeName == null && this.clazz == FieldMap.class)
            throw new MkvException("Chain should not be published first for FieldMap class, pass chain on publishData call");
        MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
        if (!Strings.isNullOrEmpty(chainName)) {
            MkvChain mkvChain = publishManager.getMkvChain(chainName);
            if (mkvChain == null) {
                try {
                    MkvChain chain = new MkvChain(chainName, this.typeName);
                    chain.publish();
                    logger.debug("Published chain '{}'", chainName);
                } catch (MkvException e) {
                    logger.error("Found error while publishing chain ['{}']", chainName, e);
                    throw e;
                }
            } else {
                logger.warn("Chain: {} already exists", chainName);
            }
        }
    }

    public void publishData(String recordName, T recordData, List<String> chains) throws MkvException, IonChainNotFoundException, IonInvalidRecordOrChainNameException {
        MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
        try {
            MkvRecord record = publishManager.getMkvRecord(recordName);
            if (record == null) {
                record = new MkvRecord(recordName, this.typeName);
                record.publish();
                addRecordToChain(recordName, chains);
            }
            try {
                this.lock.lock();
                this.proxy.set(recordData);
                record.supply((MkvSupply)this.proxy);
            } finally {
                this.lock.unlock();
            }
            logger.debug("Published record ['{}'] with record data [{}]", recordName, recordData);
        } catch (MkvException e) {
            logger.error("Found error while publishing record data. Record name: ['{}'], record object: {}", new Object[] { recordName, recordData, e });
            throw e;
        }
    }

    public void publishData(String recordName, Map<String, Object> recordDataAsMap, List<String> chains) throws MkvException, IonChainNotFoundException, IonInvalidRecordOrChainNameException {
        try {
            MkvRecord record = IonHelper.getMkvPublishManagerIfMkvSessionReady().getMkvRecord(recordName);
            if (record == null) {
                record = new MkvRecord(recordName, this.typeName);
                record.publish();
            }
            record.supply((String[])recordDataAsMap.keySet().toArray((Object[])new String[0]), recordDataAsMap.values().toArray());
            addRecordToChain(recordName, chains);
        } catch (MkvException e) {
            logger.error("Found error while publishing data for record [{}]", recordName, e);
            throw e;
        }
    }

    public boolean unpublishRecord(String recordName) throws IonRecordNotFoundException {
        MkvRecord record = IonHelper.getMkvPublishManagerIfMkvSessionReady().getMkvRecord(recordName);
        if (record == null)
            throw new IonRecordNotFoundException(String.format("Record unpublish failed. No record found for recordName ['%s']", new Object[] { recordName }));
        try {
            record.unpublish();
        } catch (MkvObjectNotAvailableException e) {
            throw new IonRecordNotFoundException(String.format("Record unpublish failed. No record found for recordName ['%s']", new Object[] { recordName }), e);
        }
        logger.debug("Record '{}' successfully unpublished.", record.getName());
        return true;
    }

    public String getTypeName() {
        return this.typeName;
    }

    protected void addRecordToChain(String recordName, List<String> chains) throws IonChainNotFoundException, IonInvalidRecordOrChainNameException {
        if (chains != null) {
            MkvPublishManager publishManager = IonHelper.getMkvPublishManagerIfMkvSessionReady();
            for (String chainName : chains) {
                if (!IonHelper.isRecordNameValidForChain(chainName, recordName)) {
                    logger.error("Record Name: {} not valid for given chain name: {}", recordName, chainName);
                    throw new IonInvalidRecordOrChainNameException(String.format("Record Name: %s not valid for given chain name: %s", new Object[] { recordName, chainName }));
                }
                MkvChain mkvChain = publishManager.getMkvChain(chainName);
                if (mkvChain == null) {
                    logger.error("Chain ['{}'] not found, so can't add record to it ", chainName);
                    throw new IonChainNotFoundException(String.format("Chain ['%s'] not found, so can't add record to it ", new Object[] { chainName }));
                }
                mkvChain.add(recordName);
                logger.debug("Added record ['{}'] to chain ['{}']", recordName, chainName);
            }
        }
    }

    public void addRecordToChain(String recordName, String chainName) {
        logger.info("addRecordToChain recordName=[{}] chainName=[{}]", recordName, chainName);
        MkvPublishManager publishManager = Mkv.getInstance().getPublishManager();
        MkvChain mkvChain = publishManager.getMkvChain(chainName);
        mkvChain.add(recordName);
    }

    public void removeRecordFromChain(String recordName, String chainName) {
        logger.info("removeRecordFromChain recordName=[{}] chainName=[{}]", recordName, chainName);
        MkvPublishManager publishManager = Mkv.getInstance().getPublishManager();
        MkvChain mkvChain = publishManager.getMkvChain(chainName);
        mkvChain.remove(recordName);
    }

    public void setTransactionHandler(IonTransactionHandler ionTransactionHandler) {
        IonSession session = Ion.getSession();
        ((IonSessionImpl)session).addTransactionHandler(ionTransactionHandler, this, this.typeName, this.clazz);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("clazz", this.clazz)
                .add("typeName", this.typeName)
                .add("isPublishPattern", this.isPublishPattern)
                .toString();
    }
}
