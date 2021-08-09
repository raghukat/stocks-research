package com.stocks.research.mkvapi_dummy.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CsvHeader {
    private static final Logger log = LoggerFactory.getLogger(CsvHeader.class);

    private HashMap<String, Integer> titles = new HashMap<>();

    private boolean initialized;

    public boolean isInitialized() {
        return this.initialized;
    }

    public boolean init(String s) {
        if (s == null) {
            log.error("csv file seems to be empty, null row read");
            return false;
        }
        String[] ss = split(s);
        int n = 0;
        for (String name : ss) {
            this.titles.put(name, Integer.valueOf(n));
            n++;
        }
        log.info("save-file column titles loaded: {}", this.titles);
        this.initialized = true;
        return true;
    }

    public static boolean isEmptyLine(String s) {
        return (s.length() == 0 || s.startsWith("#"));
    }

    public String[] split(String csvLine) {
        return csvLine.trim().split("\\s*,\\s*", -1);
    }

    public String getField(String fieldName, String[] fields) {
        int i = getPos(fieldName);
        if (i >= fields.length) {
            log.warn("input fields-array is not long enough to contain field-name [{}] at pos={} (fields.length={})", new Object[] { fieldName, Integer.valueOf(i), Integer.valueOf(fields.length) });
            log.warn("input fields {} ", Arrays.asList(fields));
        }
        return fields[i];
    }

    public boolean hasField(String column) {
        Integer i = this.titles.get(column);
        return !(i == null);
    }

    public double getFieldDbl(String fieldName, String[] fields) {
        String s = getField(fieldName, fields);
        if (s == null || s.length() == 0)
            return 0.0D;
        return Double.parseDouble(s);
    }

    public int getFieldInt(String fieldName, String[] fields) {
        String s = getField(fieldName, fields);
        if (s == null || s.length() == 0)
            return 0;
        return Integer.parseInt(s);
    }

    public int getPos(String column) {
        Integer i = this.titles.get(column);
        if (i == null) {
            log.error("No column [{}] in csv file, columns are ", column, this.titles.keySet());
            throw new RuntimeException("No column [" + column + "] in csv file");
        }
        return i.intValue();
    }

    public String getField(String fieldName, String csvLine) {
        String[] fields = split(csvLine);
        int i = getPos(fieldName);
        return fields[i];
    }

    public Set<Map.Entry<String, Integer>> getFields() {
        return this.titles.entrySet();
    }
}
