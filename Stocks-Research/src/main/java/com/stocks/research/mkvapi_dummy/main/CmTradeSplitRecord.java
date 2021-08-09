//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_dummy.main;


public class CmTradeSplitRecord {
    private Integer AllocNum;
    private String CustDealAccountId;
    private String CurrencyStr;
    private String Id;
    private String IndividualAllocId;
    private String IRC;
    private Double QtyNominal;
    private String RFQId;
    private String RFQTypeStr;
    private String SplitRef;
    private String SplitStatusStr;
    private Integer Synthetic;
    private Integer Editing;
    private String AccountId;
    private Integer SeqNo;

    public CmTradeSplitRecord() {
    }

    public Integer getAllocNum() {
        return this.getCommonAllocNum();
    }

    public void setAllocNum(Integer allocNum) {
        this.AllocNum = allocNum;
    }

    public String getCustDealAccountId() {
        return this.CustDealAccountId;
    }

    public void setCustDealAccountId(String custDealAccountId) {
        this.CustDealAccountId = custDealAccountId;
    }

    public String getCurrencyStr() {
        return this.CurrencyStr;
    }

    public void setCurrencyStr(String currencyStr) {
        this.CurrencyStr = currencyStr;
    }

    public String getId() {
        return this.Id;
    }

    public void setId(String id) {
        this.Id = id;
    }

    public String getIndividualAllocId() {
        return this.getCommonAllocId();
    }

    public void setIndividualAllocId(String individualAllocId) {
        this.IndividualAllocId = individualAllocId;
    }

    public String getIRC() {
        return this.IRC;
    }

    public void setIRC(String IRC) {
        this.IRC = IRC;
    }

    public Double getQtyNominal() {
        return this.QtyNominal;
    }

    public void setQtyNominal(Double qtyNominal) {
        this.QtyNominal = qtyNominal;
    }

    public String getRFQId() {
        return this.RFQId;
    }

    public void setRFQId(String RFQId) {
        this.RFQId = RFQId;
    }

    public String getSplitRef() {
        return this.SplitRef;
    }

    public void setSplitRef(String splitRef) {
        this.SplitRef = splitRef;
    }

    public String getSplitStatusStr() {
        return this.SplitStatusStr;
    }

    public void setSplitStatusStr(String splitStatusStr) {
        this.SplitStatusStr = splitStatusStr;
    }

    public Integer getSynthetic() {
        return this.Synthetic;
    }

    public void setSynthetic(Integer synthetic) {
        this.Synthetic = synthetic;
    }

    public Integer getEditing() {
        return this.Editing;
    }

    public void setEditing(Integer editing) {
        this.Editing = editing;
    }

    public String getRFQTypeStr() {
        return this.RFQTypeStr;
    }

    public void setRFQTypeStr(String RFQTypeStr) {
        this.RFQTypeStr = RFQTypeStr;
    }

    public String getAccountId() {
        return this.AccountId;
    }

    public void setAccountId(String accountId) {
        this.AccountId = accountId;
    }

    public Integer getSeqNo() {
        return this.SeqNo;
    }

    public void setSeqNo(Integer seqNo) {
        this.SeqNo = seqNo;
    }

    public String getCommonAllocId() {
        return this.IndividualAllocId;
    }

    public Integer getCommonAllocNum() {
        return this.AllocNum;
    }

    public String toString() {
        return "CmTradeSplitRecord{AllocNum=" + this.AllocNum + ", CustDealAccountId='" + this.CustDealAccountId + "', CurrencyStr='" + this.CurrencyStr + "', Id='" + this.Id + "', IndividualAllocId='" + this.IndividualAllocId + "', IRC='" + this.IRC + "', QtyNominal=" + this.QtyNominal + ", RFQId='" + this.RFQId + "', RFQTypeStr='" + this.RFQTypeStr + "', SplitRef='" + this.SplitRef + "', SplitStatusStr='" + this.SplitStatusStr + "', Synthetic=" + this.Synthetic + ", Editing=" + this.Editing + ", AccountId='" + this.AccountId + "', SeqNo=" + this.SeqNo + "}";
    }
}
