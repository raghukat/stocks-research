//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.stocks.research.mkvapi_util;

import com.stocks.research.mkvapi.main.FieldMap;
import com.stocks.research.mkvapi.main.IonRecordListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class ComparativeFieldMapRecordListener implements IonRecordListener<FieldMap> {
    private static final Logger log = LoggerFactory.getLogger(ComparativeFieldMapRecordListener.class);
    private String component1;
    private String component2;
    HashMap<String, FieldMap> data1 = new HashMap();
    HashMap<String, FieldMap> data2 = new HashMap();
    HashMap<String, Boolean> matchState = new HashMap();
    HashSet<String> checkFields = new HashSet();
    BufferedWriter wr;

    ComparativeFieldMapRecordListener(String chain1, String chain2, List<String> fieldsToCheck, String reportFile) throws Exception {
        this.component1 = this.getComponent(chain1);
        this.component2 = this.getComponent(chain2);
        this.checkFields.addAll(fieldsToCheck);
        this.wr = new BufferedWriter(new FileWriter(reportFile));
        this.report("Comparing chains [" + chain1 + "] [" + chain2 + "] ");
        this.report("Fields to compare: " + this.checkFields);
    }

    public void onRecordUpdate(String recordName, FieldMap recordData) {
        String component = this.getComponent(recordName);
        Object oid = recordData.getValue("Id");
        if (oid == null) {
            log.warn("no Id field in record [{}], will ignore record FieldMap={}", recordName, recordData);
        } else {
            String id = oid.toString();
            this.report("update [" + id + "] component=" + component);
            log.info("onRecordUpdate: DATA [{}] {}", recordName, recordData.getData());
            HashMap<String, FieldMap> myDataStore = this.getMyDataStore(component);
            myDataStore.put(id, recordData);
            Boolean dataMatch = this.compareValues(id);
            Boolean prevMatchState = (Boolean)this.matchState.get(id);
            this.matchState.put(id, dataMatch);
            if (prevMatchState == null) {
                log.info("both records not yet available [{}] this-record={{}]", id, recordName);
            } else if (!dataMatch) {
                if (!prevMatchState) {
                    this.reportDataDifferences(id);
                }
            } else {
                this.report("data match [" + id + "]");
                log.info("full data match [{}]", id);
            }

        }
    }

    Boolean compareValues(String id) {
        FieldMap f1 = (FieldMap)this.data1.get(id);
        FieldMap f2 = (FieldMap)this.data2.get(id);
        if (f1 != null && f2 != null) {
            Iterator var4 = this.checkFields.iterator();

            while(var4.hasNext()) {
                String fname = (String)var4.next();
                Object v1 = f1.getValue(fname);
                Object v2 = f2.getValue(fname);
                if (v1 == null || v2 == null) {
                    log.warn("bad-match: one item found to be null [{}].[{}] == [{}] [{}]", new Object[]{id, fname, v1, v2});
                    return Boolean.FALSE;
                }

                if (!v1.equals(v2)) {
                    String s1 = v1.toString().trim();
                    String s2 = v2.toString().trim();
                    if (!s1.equalsIgnoreCase(s2)) {
                        log.warn("bad-match: items do not match [{}].[{}] == [{}] [{}]", new Object[]{id, fname, s1, s2});
                        return Boolean.FALSE;
                    }
                }
            }

            return Boolean.TRUE;
        } else {
            log.warn("bad-match: field-maps are null [{}] f1={} f2={}", new Object[]{id, f1, f2});
            return Boolean.FALSE;
        }
    }

    void reportDataDifferences(String id) {
        log.info("======== difference report for [{}] ========", id);
        FieldMap f1 = (FieldMap)this.data1.get(id);
        FieldMap f2 = (FieldMap)this.data2.get(id);
        int diffCount = 0;
        if (f1 != null && f2 != null) {
            Iterator var5 = this.checkFields.iterator();

            while(true) {
                while(var5.hasNext()) {
                    String fname = (String)var5.next();
                    Object v1 = f1.getValue(fname);
                    Object v2 = f2.getValue(fname);
                    if (v1 != null && v2 != null) {
                        if (!v1.equals(v2)) {
                            String s1 = v1.toString().trim();
                            String s2 = v2.toString().trim();
                            if (!s1.equalsIgnoreCase(s2)) {
                                this.report("items do not match [" + id + "].[" + fname + "] == [" + s1 + "] [" + s2 + "]");
                                ++diffCount;
                            }
                        }
                    } else {
                        this.report("one or more item(s) found to be null [" + id + "].[" + fname + "] == [" + v1 + "] [" + v2 + "]");
                        ++diffCount;
                    }
                }

                return;
            }
        } else {
            log.warn("one of other entries is nulll [{}] ... [{}] [{}] ", new Object[]{id, f1, f2});
        }
    }

    String getComponent(String recordName) {
        int n = recordName.lastIndexOf(".");
        return recordName.substring(0, n);
    }

    HashMap<String, FieldMap> getMyDataStore(String component) {
        return component.equals(this.component1) ? this.data1 : this.data2;
    }

    HashMap<String, FieldMap> getAltDataStore(String component) {
        return component.equals(this.component1) ? this.data2 : this.data1;
    }

    void report(String s) {
        Date date = new Date();

        try {
            this.wr.write(date + ": ");
            this.wr.write(s);
            this.wr.newLine();
            this.wr.flush();
            log.info("#RPT: {}", s);
        } catch (Exception var4) {
            log.error(date + ": failed to record report text [{}]", s, var4);
        }

    }
}
