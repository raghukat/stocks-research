/*     */ package com.stocks.research.mkvapi.main;
/*     */ 
/*     */ import com.iontrading.mkv.MkvFunction;
import com.iontrading.mkv.MkvRecord;
import com.iontrading.mkv.MkvSupply;
import com.iontrading.mkv.MkvType;
import com.iontrading.mkv.enums.MkvFieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class RecordRecorder
/*     */ {
/*     */   public static final String RECORD_RECORDER_PROPERTY = "mkvapi.recordingMode";
/*     */   public static final String RECORD_RECORDER_DIRECTORY = "mkvapi.recordingDir";
/*     */   public static final String RECORD_RECORDER_FILEFORMAT = "mkvapi.recordingFileFormat";
/*     */   public static final String FUNC_FILE_NAME = "FUNCTION_CALLS.txt";
/*  33 */   private static final Logger logger = LoggerFactory.getLogger(RecordRecorder.class);
/*     */   
/*  35 */   private static ConcurrentMap<String, RecordRecorder> allRecordRecorders = new ConcurrentHashMap<>();
/*     */   
/*     */   private static String recording;
/*     */   
/*     */   private static String recordingDir;
/*     */   
/*     */   private static String fileFormat;
/*     */   private static BufferedWriter funcWriter;
/*     */   private BufferedWriter wr;
/*     */   private String recordPrefix;
/*     */   private int[] fieldOrder;
/*     */   
/*     */   public static boolean isRecording() {
/*  48 */     return !recording.equals("false");
/*     */   }
/*     */ 
/*     */   
/*     */   public static void initializeRecording() {
/*  53 */     recording = System.getProperty("mkvapi.recordingMode", "false").trim();
/*  54 */     fileFormat = System.getProperty("mkvapi.recordingFileFormat", ".rcd").trim();
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  59 */     if (isRecording()) {
/*  60 */       logger.info("recording-mode is enabled [{}]", recording);
/*  61 */       recordingDir = System.getProperty("mkvapi.recordingDir");
/*  62 */       if (recordingDir != null) {
/*  63 */         recordingDir = recordingDir.trim();
/*  64 */         logger.info("recording into directory [{}]", recordingDir);
/*     */       } 
/*     */     } 
/*     */   }
/*     */   
/*     */   public static RecordRecorder lookupRecordRecorder(String recordName) {
/*  70 */     int n = recordName.lastIndexOf(".");
/*  71 */     if (n == -1) {
/*  72 */       logger.error("no dot in record name [{]]", recordName);
/*  73 */       return null;
/*     */     } 
/*  75 */     String recordPrefix = recordName.substring(0, n);
/*  76 */     RecordRecorder rd = allRecordRecorders.get(recordPrefix);
/*  77 */     logger.info("lookupRecordRecorder [{}] -> [{]] found [{]]", new Object[] { recordName, recordPrefix, rd });
/*  78 */     return rd;
/*     */   }
/*     */   
/*  81 */   private List<String> subscribedFieldNames = null;
/*     */ 
/*     */   
/*     */   public RecordRecorder(MkvRecord mkvRecord, List<String> fieldNames) throws Exception {
/*  85 */     this.subscribedFieldNames = fieldNames;
/*     */     
/*  87 */     String name = mkvRecord.getName();
/*  88 */     int n = name.lastIndexOf(".");
/*  89 */     if (n == -1) {
/*  90 */       logger.error("no dot in record name [{]]", name);
/*     */     }
/*  92 */     this.recordPrefix = name.substring(0, n);
/*  93 */     String fileName = this.recordPrefix + fileFormat;
/*  94 */     if (recordingDir != null) {
/*  95 */       fileName = recordingDir + File.separator + fileName;
/*     */     }
/*  97 */     logger.info("open recording-file [{}] for recordPrefix=[{}]", fileName, this.recordPrefix);
/*  98 */     this.wr = new BufferedWriter(new FileWriter(fileName, isAppending()));
/*  99 */     this.wr.write("@record-prefix " + this.recordPrefix);
/* 100 */     this.wr.newLine();
/*     */     
/* 102 */     this.wr.write("@component " + mkvRecord.getOrig());
/* 103 */     this.wr.newLine();
/*     */     
/* 105 */     MkvType mkvType = mkvRecord.getMkvType();
/*     */     
/* 107 */     int numOfFields = 0;
/*     */     
/* 109 */     StringBuilder sb = new StringBuilder();
/* 110 */     StringBuilder typeSb = new StringBuilder();
/*     */     
/* 112 */     if (this.subscribedFieldNames == null) {
/* 113 */       numOfFields = mkvType.size();
/* 114 */       this.fieldOrder = new int[numOfFields];
/*     */       
/* 116 */       for (int i = 0; i < numOfFields; i++) {
/* 117 */         String fieldName = mkvType.getFieldName(i);
/* 118 */         this.fieldOrder[i] = mkvType.getFieldIndex(fieldName);
/* 119 */         sb.append(fieldName);
/* 120 */         sb.append(",");
/* 121 */         typeSb.append(getJavaTypeFromMkvType(mkvType.getFieldType(this.fieldOrder[i])));
/* 122 */         typeSb.append(",");
/*     */       } 
/*     */     } else {
/*     */       
/* 126 */       numOfFields = this.subscribedFieldNames.size();
/* 127 */       this.fieldOrder = new int[numOfFields];
/*     */       
/* 129 */       for (int i = 0; i < numOfFields; i++) {
/* 130 */         String fieldName = this.subscribedFieldNames.get(i);
/* 131 */         this.fieldOrder[i] = mkvType.getFieldIndex(fieldName);
/* 132 */         sb.append(fieldName);
/* 133 */         sb.append(",");
/* 134 */         if (this.fieldOrder[i] < 0) {
/* 135 */           typeSb.append("UNKNOWN");
/*     */         } else {
/* 137 */           typeSb.append(getJavaTypeFromMkvType(mkvType.getFieldType(this.fieldOrder[i])));
/*     */         } 
/* 139 */         typeSb.append(",");
/*     */       } 
/*     */     } 
/*     */     
/* 143 */     sb.append("snapshot");
/* 144 */     sb.append(",");
/* 145 */     sb.append("recordid");
/* 146 */     this.wr.write("@header");
/* 147 */     this.wr.newLine();
/* 148 */     this.wr.write(sb.toString());
/* 149 */     this.wr.newLine();
/* 150 */     this.wr.write("@fieldtype");
/* 151 */     this.wr.newLine();
/* 152 */     this.wr.write(typeSb.toString());
/* 153 */     this.wr.newLine();
/*     */     
/* 155 */     allRecordRecorders.put(this.recordPrefix, this);
/*     */     
/* 157 */     logger.info("initialize recording for [{}] numOfFields={} file=[{}]", new Object[] { name, Integer.valueOf(numOfFields), fileName });
/*     */   }
/*     */   
/*     */   private static boolean isAppending() {
/* 161 */     return recording.startsWith("append");
/*     */   }
/*     */   
/*     */   public synchronized void recordTransaction(Map<String, Object> fields) throws Exception {
/* 165 */     this.wr.write("@await-tx ");
/* 166 */     this.wr.write(fields.toString());
/* 167 */     this.wr.newLine();
/* 168 */     this.wr.flush();
/*     */   }
/*     */   
/*     */   static synchronized void recordFunctionCall(String functionName, MkvFunction mkvFunc, List<Object> argValues) {
/*     */     try {
/* 173 */       String fileName = "FUNCTION_CALLS.txt";
/* 174 */       if (recordingDir != null) {
/* 175 */         fileName = recordingDir + File.separator + fileName;
/*     */       }
/* 177 */       if (funcWriter == null) {
/* 178 */         funcWriter = new BufferedWriter(new FileWriter(fileName, isAppending()));
/*     */       }
/* 180 */       funcWriter.write("@timestamp ");
/* 181 */       String tsStr = Long.toString(System.currentTimeMillis());
/* 182 */       funcWriter.write(tsStr);
/* 183 */       funcWriter.newLine();
/* 184 */       funcWriter.write("CALL: ");
/* 185 */       funcWriter.write(functionName);
/* 186 */       funcWriter.write(" ");
/* 187 */       funcWriter.write(mkvFunc.getOrig());
/* 188 */       funcWriter.write(" ");
/* 189 */       funcWriter.write(argValues.toString());
/* 190 */       funcWriter.newLine();
/*     */       
/* 192 */       funcWriter.flush();
/* 193 */       for (RecordRecorder rd : allRecordRecorders.values()) {
/* 194 */         rd.recordFuctuionCallWithSubscribers(functionName, tsStr);
/*     */       }
/*     */     }
/* 197 */     catch (Exception e) {
/* 198 */       logger.error("Exception recording function call", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   public static synchronized void recordFunctionResult(String functionName, List<Object> returnValues) {
/*     */     try {
/* 204 */       String fileName = "FUNCTION_CALLS.txt";
/* 205 */       if (recordingDir != null) {
/* 206 */         fileName = recordingDir + File.separator + fileName;
/*     */       }
/* 208 */       if (funcWriter == null) {
/* 209 */         funcWriter = new BufferedWriter(new FileWriter(fileName, isAppending()));
/*     */       }
/* 211 */       funcWriter.write("@timestamp ");
/* 212 */       String tsStr = Long.toString(System.currentTimeMillis());
/* 213 */       funcWriter.write(tsStr);
/* 214 */       funcWriter.newLine();
/* 215 */       funcWriter.write("RETURN: ");
/* 216 */       funcWriter.write(functionName);
/* 217 */       funcWriter.write(" RESULT: ");
/* 218 */       funcWriter.write(returnValues.toString());
/* 219 */       funcWriter.newLine();
/*     */       
/* 221 */       funcWriter.flush();
/*     */     }
/* 223 */     catch (Exception e) {
/* 224 */       logger.error("Exception recording function call", e);
/*     */     } 
/*     */   }
/*     */   public static synchronized void recordFunctionResult(String functionName, int errorCode, String errorText) {
/*     */     try {
/* 229 */       String fileName = "FUNCTION_CALLS.txt";
/* 230 */       if (recordingDir != null) {
/* 231 */         fileName = recordingDir + File.separator + fileName;
/*     */       }
/* 233 */       if (funcWriter == null) {
/* 234 */         funcWriter = new BufferedWriter(new FileWriter(fileName, isAppending()));
/*     */       }
/* 236 */       funcWriter.write("@timestamp ");
/* 237 */       String tsStr = Long.toString(System.currentTimeMillis());
/* 238 */       funcWriter.write(tsStr);
/* 239 */       funcWriter.newLine();
/* 240 */       funcWriter.write("RETURN: ");
/* 241 */       funcWriter.write(functionName);
/* 242 */       funcWriter.write(" ERROR-CODE: ");
/* 243 */       funcWriter.write(Integer.toString(errorCode));
/* 244 */       funcWriter.write(" ERROR-TEXT: ");
/* 245 */       funcWriter.write(errorText);
/* 246 */       funcWriter.newLine();
/*     */       
/* 248 */       funcWriter.flush();
/*     */     }
/* 250 */     catch (Exception e) {
/* 251 */       logger.error("Exception recording function call", e);
/*     */     } 
/*     */   }
/*     */   
/*     */   private synchronized void recordFuctuionCallWithSubscribers(String functionName, String tsStr) throws Exception {
/* 256 */     this.wr.write("@timestamp ");
/* 257 */     this.wr.write(tsStr);
/* 258 */     this.wr.newLine();
/* 259 */     this.wr.write("@await-func ");
/* 260 */     this.wr.write(functionName);
/* 261 */     this.wr.newLine();
/* 262 */     this.wr.flush();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized void recordRecordArrival(MkvRecord mkvRecord, boolean isSnapshot) throws Exception {
/* 268 */     this.wr.write("@timestamp " + System.currentTimeMillis());
/* 269 */     this.wr.newLine();
/*     */     
/* 271 */     MkvSupply mkvSupply = mkvRecord.getSupply();
/* 272 */     MkvType mkvType = mkvRecord.getMkvType();
/*     */     
/* 274 */     StringBuilder sb = new StringBuilder();
/* 275 */     for (int i = 0; i < this.fieldOrder.length; i++) {
/* 276 */       if (this.fieldOrder[i] != -1 && 
/* 277 */         mkvSupply.isSet(this.fieldOrder[i])) {
/* 278 */         Object obj = mkvSupply.getObject(this.fieldOrder[i]);
/* 279 */         MkvFieldType fieldType = mkvType.getFieldType(this.fieldOrder[i]);
/* 280 */         if (obj != null) {
/* 281 */           String converted = obj.toString().replaceAll(",", "-");
/* 282 */           sb.append(converted);
/*     */         } 
/*     */       } 
/*     */ 
/*     */       
/* 287 */       sb.append(",");
/*     */     } 
/* 289 */     sb.append(String.valueOf(isSnapshot));
/* 290 */     sb.append(",");
/* 291 */     sb.append(mkvRecord.getName());
/* 292 */     this.wr.write(sb.toString());
/* 293 */     this.wr.newLine();
/* 294 */     this.wr.flush();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public synchronized void close() throws Exception {
/* 300 */     this.wr.close();
/* 301 */     allRecordRecorders.remove(this.recordPrefix);
/*     */   }
/*     */ 
/*     */   
/*     */   private static String getJavaTypeFromMkvType(MkvFieldType fieldType) {
/* 306 */     switch (fieldType.intValue()) {
/*     */       case 0:
/* 308 */         return "String";
/*     */       case 1:
/* 310 */         return "Double";
/*     */       case 2:
/* 312 */         return "Integer";
/*     */       case 3:
/* 314 */         return "LocalDate";
/*     */       case 4:
/* 316 */         return "LocalDateTime";
/*     */     } 
/* 318 */     return "String";
/*     */   }
/*     */ }
