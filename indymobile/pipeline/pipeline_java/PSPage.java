package com.indymobile.app.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.ads.mediation.inmobi.InMobiNetworkValues;
import com.google.gson.C6399a;
import com.indymobile.app.util.PSException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import p000.fu4;
import p000.gd2;
import p000.iv3;
import p000.iy3;
import p000.kl5;
import p000.n72;
import p000.pv3;
import p000.rv3;
import p000.s46;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSPage implements Parcelable {
    public static final Parcelable.Creator<PSPage> CREATOR = new C66621();
    public static final String TABLE_NAME = "page";
    public static int columnCount = 23;

    /* JADX INFO: renamed from: a */
    public PSDocumentProcessInfo f31387a;

    /* JADX INFO: renamed from: b */
    public PSDocumentFilterInfo f31388b;

    @fu4("cOpt")
    public HashMap cOpt;

    @fu4("created")
    public Date dateCreate;

    @fu4("modified")
    public Date dateModify;

    @fu4("dateNote")
    public Date dateNote;

    @fu4("dateOCR")
    public Date dateOCR;

    @fu4("dateTrash")
    public Date dateTrash;

    @fu4("docId")
    public int documentID;

    @fu4("fileType")
    public pv3 fileType;

    @fu4("iOpt")
    public int iOpt;

    @fu4("done")
    public boolean isProcessCompleted;

    @fu4("jpgQ")
    public int jpgQuality;

    @fu4("note")
    public String note;

    @fu4("ocr")
    public String ocr;

    @fu4("raw")
    public String originalFileName;

    @fu4("id")
    public int pageID;

    @fu4("pageIndex")
    public int pageIndex;

    @fu4("size")
    public long resultFileSize;

    @fu4(InMobiNetworkValues.HEIGHT)
    public int resultImageHeight;

    @fu4(InMobiNetworkValues.WIDTH)
    public int resultImageWidth;

    @fu4("keyword")
    public String searchKeyword;

    @fu4("status")
    public iv3 status;

    @fu4("path")
    public String storagePath;

    @fu4("name")
    public String title;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSPage$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66621 implements Parcelable.Creator<PSPage> {
        @Override // android.os.Parcelable.Creator
        public final PSPage createFromParcel(Parcel parcel) {
            PSPage pSPage = new PSPage();
            pSPage.searchKeyword = "";
            pSPage.isProcessCompleted = false;
            pSPage.note = "";
            pSPage.ocr = "";
            pSPage.title = "";
            pSPage.cOpt = new HashMap();
            pSPage.pageID = parcel.readInt();
            pSPage.storagePath = parcel.readString();
            long j = parcel.readLong();
            pSPage.dateCreate = j == -1 ? null : new Date(j);
            long j2 = parcel.readLong();
            pSPage.dateModify = j2 == -1 ? null : new Date(j2);
            pSPage.searchKeyword = parcel.readString();
            pSPage.pageIndex = parcel.readInt();
            pSPage.documentID = parcel.readInt();
            pSPage.isProcessCompleted = parcel.readByte() != 0;
            pSPage.originalFileName = parcel.readString();
            pSPage.resultFileSize = parcel.readLong();
            pSPage.resultImageWidth = parcel.readInt();
            pSPage.resultImageHeight = parcel.readInt();
            pSPage.note = parcel.readString();
            pSPage.ocr = parcel.readString();
            long j3 = parcel.readLong();
            pSPage.dateOCR = j3 == -1 ? null : new Date(j3);
            long j4 = parcel.readLong();
            pSPage.dateNote = j4 == -1 ? null : new Date(j4);
            pSPage.title = parcel.readString();
            pSPage.jpgQuality = parcel.readInt();
            int i = parcel.readInt();
            pSPage.fileType = i == -1 ? null : pv3.values()[i];
            long j5 = parcel.readLong();
            pSPage.dateTrash = j5 == -1 ? null : new Date(j5);
            pSPage.iOpt = parcel.readInt();
            pSPage.cOpt = (HashMap) parcel.readSerializable();
            int i2 = parcel.readInt();
            pSPage.status = i2 != -1 ? iv3.values()[i2] : null;
            return pSPage;
        }

        @Override // android.os.Parcelable.Creator
        public final PSPage[] newArray(int i) {
            return new PSPage[i];
        }
    }

    public PSPage(Cursor cursor, int i) {
        Exception e;
        int i2;
        HashMap map;
        this.searchKeyword = "";
        this.isProcessCompleted = false;
        this.note = "";
        this.ocr = "";
        this.title = "";
        this.cOpt = new HashMap();
        this.pageID = cursor.getInt(i);
        this.storagePath = cursor.getString(i + 1);
        this.dateCreate = new Date(cursor.getLong(i + 2));
        this.dateModify = new Date(cursor.getLong(i + 3));
        this.searchKeyword = cursor.getString(i + 4);
        this.pageIndex = cursor.getInt(i + 5);
        int i3 = i + 7;
        this.isProcessCompleted = cursor.getInt(i + 6) != 0;
        this.documentID = cursor.getInt(i3);
        this.originalFileName = cursor.getString(i + 8);
        this.resultFileSize = cursor.getLong(i + 9);
        this.resultImageWidth = cursor.getInt(i + 10);
        this.resultImageHeight = cursor.getInt(i + 11);
        this.ocr = cursor.getString(i + 12);
        int i4 = i + 14;
        this.note = cursor.getString(i + 13);
        if (!cursor.isNull(i4)) {
            this.dateOCR = new Date(cursor.getLong(i4));
        }
        int i5 = i + 15;
        if (!cursor.isNull(i5)) {
            this.dateNote = new Date(cursor.getLong(i5));
        }
        this.title = cursor.getString(i + 16);
        int i6 = i + 18;
        this.jpgQuality = cursor.getInt(i + 17);
        this.fileType = pv3.JPG;
        try {
            i2 = i + 19;
            try {
                this.fileType = pv3.values()[cursor.getInt(i6)];
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            e = e3;
            i2 = i6;
        }
        this.dateTrash = new Date(cursor.getLong(i2));
        int i7 = i2 + 2;
        this.iOpt = cursor.getInt(i2 + 1);
        int i8 = i2 + 3;
        try {
            try {
                HashMap map2 = (HashMap) new n72().m32870a().m14251e(cursor.getString(i7), new kl5(HashMap.class));
                this.cOpt = map2;
                if (map2 == null) {
                    map = new HashMap();
                    this.cOpt = map;
                }
            } catch (Throwable th) {
                if (this.cOpt == null) {
                    this.cOpt = new HashMap();
                }
                throw th;
            }
        } catch (Exception e4) {
            e4.printStackTrace();
            map = this.cOpt == null ? new HashMap() : map;
        }
        this.status = iv3.kStatusNormal;
        try {
            this.status = iv3.values()[cursor.getInt(i8)];
        } catch (Exception e5) {
            e5.printStackTrace();
        }
    }

    /* JADX INFO: renamed from: a */
    public final File m15841a() {
        return new File(m15845e(), "markup");
    }

    /* JADX INFO: renamed from: b */
    public final File m15842b() {
        return new File(m15841a(), "markup_raw.jpg");
    }

    /* JADX INFO: renamed from: c */
    public final File m15843c() {
        if (this.originalFileName.startsWith("/")) {
            return new File(this.originalFileName);
        }
        File file = iy3.m31217b().f51568b;
        if (!file.exists()) {
            file.mkdirs();
        }
        return new File(file, this.originalFileName);
    }

    /* JADX INFO: renamed from: d */
    public final File m15844d() {
        return new File(m15845e(), "result.jpg");
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    /* JADX INFO: renamed from: e */
    public final File m15845e() {
        return new File(iy3.m31217b().f51567a, this.storagePath);
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof PSPage) && this.pageID == ((PSPage) obj).pageID;
    }

    /* JADX INFO: renamed from: f */
    public final File m15846f() {
        return new File(m15845e(), "thumbnail.jpg");
    }

    /* JADX INFO: renamed from: g */
    public final boolean m15847g() {
        rv3 rv3Var;
        if (this.cOpt.containsKey("src")) {
            try {
                rv3Var = rv3.values()[Integer.parseInt((String) this.cOpt.get("src"))];
            } catch (Exception e) {
                e.printStackTrace();
                rv3Var = rv3.kSourceFromUndefined;
            }
        } else {
            rv3Var = rv3.kSourceFromUndefined;
        }
        return (rv3Var == rv3.kSourceFromBlankPage || rv3Var == rv3.kSourceFromCollage) ? false : true;
    }

    /* JADX INFO: renamed from: h, reason: merged with bridge method [inline-methods] */
    public final PSPage clone() {
        PSPage pSPage = new PSPage();
        pSPage.pageID = this.pageID;
        pSPage.storagePath = this.storagePath;
        pSPage.dateCreate = this.dateCreate;
        pSPage.dateModify = this.dateModify;
        pSPage.searchKeyword = this.searchKeyword;
        pSPage.pageIndex = this.pageIndex;
        pSPage.documentID = this.documentID;
        pSPage.isProcessCompleted = this.isProcessCompleted;
        pSPage.originalFileName = this.originalFileName;
        pSPage.resultFileSize = this.resultFileSize;
        pSPage.resultImageWidth = this.resultImageWidth;
        pSPage.resultImageHeight = this.resultImageHeight;
        pSPage.note = this.note;
        pSPage.ocr = this.ocr;
        pSPage.dateOCR = this.dateOCR;
        pSPage.dateNote = this.dateNote;
        pSPage.title = this.title;
        pSPage.jpgQuality = this.jpgQuality;
        pSPage.fileType = this.fileType;
        pSPage.dateTrash = this.dateTrash;
        pSPage.iOpt = this.iOpt;
        pSPage.cOpt = this.cOpt;
        pSPage.status = this.status;
        return pSPage;
    }

    public final int hashCode() {
        return this.pageID;
    }

    /* JADX INFO: renamed from: i */
    public final ContentValues m15849i() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("i_page_id", Integer.valueOf(this.pageID));
        contentValues.put("c_storage_path", this.storagePath);
        contentValues.put("i_date_create", Long.valueOf(this.dateCreate.getTime()));
        contentValues.put("i_date_modify", Long.valueOf(this.dateModify.getTime()));
        contentValues.put("c_search_keyword", this.searchKeyword);
        contentValues.put("i_page_index", Integer.valueOf(this.pageIndex));
        contentValues.put("b_is_process_completed", Boolean.valueOf(this.isProcessCompleted));
        contentValues.put("i_document_id", Integer.valueOf(this.documentID));
        contentValues.put("c_original_file_name", this.originalFileName);
        contentValues.put("i_result_file_size", Long.valueOf(this.resultFileSize));
        contentValues.put("i_result_image_width", Integer.valueOf(this.resultImageWidth));
        contentValues.put("i_result_image_height", Integer.valueOf(this.resultImageHeight));
        contentValues.put("c_note", this.note);
        contentValues.put("c_ocr", this.ocr);
        Date date = this.dateOCR;
        if (date != null) {
            contentValues.put("i_date_ocr", Long.valueOf(date.getTime()));
        }
        Date date2 = this.dateNote;
        if (date2 != null) {
            contentValues.put("i_date_note", Long.valueOf(date2.getTime()));
        }
        contentValues.put("c_title", this.title);
        contentValues.put("i_jpg_quality", Integer.valueOf(this.jpgQuality));
        contentValues.put("i_file_type", Integer.valueOf(this.fileType.ordinal()));
        Date date3 = this.dateTrash;
        if (date3 != null) {
            contentValues.put("i_date_trash", Long.valueOf(date3.getTime()));
        }
        contentValues.put("i_opt", Integer.valueOf(this.iOpt));
        HashMap map = this.cOpt;
        contentValues.put("c_opt", (map == null || map.keySet().size() == 0) ? null : new n72().m32870a().m14257k(this.cOpt));
        contentValues.put("i_status", Integer.valueOf(this.status.ordinal()));
        return contentValues;
    }

    /* JADX INFO: renamed from: j */
    public final PSDocumentFilterInfo m15850j() throws Throwable {
        if (this.f31388b == null && new File(m15845e(), "filter.info").exists()) {
            try {
                m15855o();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this.f31388b == null) {
            this.f31388b = new PSDocumentFilterInfo();
        }
        return this.f31388b;
    }

    /* JADX INFO: renamed from: k */
    public final PSDocumentProcessInfo m15851k() {
        if (this.f31387a == null && new File(m15845e(), "process.info").exists()) {
            try {
                m15856q();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return this.f31387a;
    }

    /* JADX WARN: Code duplicated, block: B:26:0x0054  */
    /* JADX WARN: Code duplicated, block: B:39:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v0, types: [com.indymobile.app.model.PSPage] */
    /* JADX WARN: Type inference failed for: r6v10, types: [java.io.Closeable, java.io.FileInputStream, java.io.InputStream] */
    /* JADX WARN: Type inference failed for: r6v11 */
    /* JADX WARN: Type inference failed for: r6v12 */
    /* JADX WARN: Type inference failed for: r6v2 */
    /* JADX WARN: Type inference failed for: r6v4 */
    /* JADX WARN: Type inference failed for: r6v5 */
    /* JADX WARN: Type inference failed for: r6v6, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r6v7, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r6v8 */
    /* JADX INFO: renamed from: l */
    public final PSMarkupRawImageInfo m15852l() throws Throwable {
        InputStreamReader inputStreamReader;
        Throwable th;
        Exception e;
        PSMarkupRawImageInfo pSMarkupRawImageInfo = null;
        try {
            try {
                try {
                    try {
                        this = new FileInputStream(new File(m15841a(), ".markup_raw.info"));
                        try {
                            inputStreamReader = new InputStreamReader(this);
                            try {
                                PSMarkupRawImageInfo pSMarkupRawImageInfo2 = (PSMarkupRawImageInfo) new C6399a().m14250d(inputStreamReader, new kl5(PSMarkupRawImageInfo.class));
                                gd2.m30020a(this);
                                gd2.m30020a(inputStreamReader);
                                pSMarkupRawImageInfo = pSMarkupRawImageInfo2;
                                this = this;
                            } catch (Exception e2) {
                                e = e2;
                                e.printStackTrace();
                                gd2.m30020a(this);
                                gd2.m30020a(inputStreamReader);
                                this = this;
                            }
                        } catch (Exception e3) {
                            e = e3;
                            inputStreamReader = null;
                        } catch (Throwable th2) {
                            inputStreamReader = null;
                            th = th2;
                            gd2.m30020a(this);
                            gd2.m30020a(inputStreamReader);
                            throw th;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        this = 0;
                        inputStreamReader = null;
                        e.printStackTrace();
                        gd2.m30020a(this);
                        gd2.m30020a(inputStreamReader);
                        this = this;
                        if (pSMarkupRawImageInfo == null) {
                            return new PSMarkupRawImageInfo();
                        }
                        return pSMarkupRawImageInfo;
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            } catch (Exception e5) {
                e = e5;
            }
            if (pSMarkupRawImageInfo == null) {
                return new PSMarkupRawImageInfo();
            }
            return pSMarkupRawImageInfo;
        } catch (Throwable th4) {
            inputStreamReader = null;
            th = th4;
            this = 0;
        }
    }

    /* JADX WARN: Code duplicated, block: B:26:0x0054  */
    /* JADX WARN: Code duplicated, block: B:39:? A[RETURN, SYNTHETIC] */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r6v0, types: [com.indymobile.app.model.PSPage] */
    /* JADX WARN: Type inference failed for: r6v10, types: [java.io.Closeable, java.io.FileInputStream, java.io.InputStream] */
    /* JADX WARN: Type inference failed for: r6v11 */
    /* JADX WARN: Type inference failed for: r6v12 */
    /* JADX WARN: Type inference failed for: r6v2 */
    /* JADX WARN: Type inference failed for: r6v4 */
    /* JADX WARN: Type inference failed for: r6v5 */
    /* JADX WARN: Type inference failed for: r6v6, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r6v7, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r6v8 */
    /* JADX INFO: renamed from: m */
    public final PSTempRotateInfo m15853m() throws Throwable {
        InputStreamReader inputStreamReader;
        Throwable th;
        Exception e;
        PSTempRotateInfo pSTempRotateInfo = null;
        try {
            try {
                try {
                    try {
                        this = new FileInputStream(new File(m15845e(), ".temp_rotate.info"));
                        try {
                            inputStreamReader = new InputStreamReader(this);
                            try {
                                PSTempRotateInfo pSTempRotateInfo2 = (PSTempRotateInfo) new C6399a().m14250d(inputStreamReader, new kl5(PSTempRotateInfo.class));
                                gd2.m30020a(this);
                                gd2.m30020a(inputStreamReader);
                                pSTempRotateInfo = pSTempRotateInfo2;
                                this = this;
                            } catch (Exception e2) {
                                e = e2;
                                e.printStackTrace();
                                gd2.m30020a(this);
                                gd2.m30020a(inputStreamReader);
                                this = this;
                            }
                        } catch (Exception e3) {
                            e = e3;
                            inputStreamReader = null;
                        } catch (Throwable th2) {
                            inputStreamReader = null;
                            th = th2;
                            gd2.m30020a(this);
                            gd2.m30020a(inputStreamReader);
                            throw th;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        this = 0;
                        inputStreamReader = null;
                        e.printStackTrace();
                        gd2.m30020a(this);
                        gd2.m30020a(inputStreamReader);
                        this = this;
                        if (pSTempRotateInfo == null) {
                            return new PSTempRotateInfo();
                        }
                        return pSTempRotateInfo;
                    }
                } catch (Throwable th3) {
                    th = th3;
                }
            } catch (Exception e5) {
                e = e5;
            }
            if (pSTempRotateInfo == null) {
                return new PSTempRotateInfo();
            }
            return pSTempRotateInfo;
        } catch (Throwable th4) {
            inputStreamReader = null;
            th = th4;
            this = 0;
        }
    }

    /* JADX INFO: renamed from: n */
    public final boolean m15854n() {
        return m15843c().exists() && m15843c().canRead() && s46.m35298z(m15843c());
    }

    /* JADX INFO: renamed from: o */
    public final void m15855o() throws Throwable {
        InputStreamReader inputStreamReader;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(new File(m15845e(), "filter.info"));
            try {
                inputStreamReader = new InputStreamReader(fileInputStream2);
                try {
                    this.f31388b = (PSDocumentFilterInfo) new C6399a().m14250d(inputStreamReader, new kl5(PSDocumentFilterInfo.class));
                    gd2.m30020a(fileInputStream2);
                    gd2.m30020a(inputStreamReader);
                } catch (Exception e) {
                    e = e;
                    fileInputStream = fileInputStream2;
                    try {
                        throw new PSException(e);
                    } catch (Throwable th) {
                        th = th;
                        gd2.m30020a(fileInputStream);
                        gd2.m30020a(inputStreamReader);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = fileInputStream2;
                    gd2.m30020a(fileInputStream);
                    gd2.m30020a(inputStreamReader);
                    throw th;
                }
            } catch (Exception e2) {
                e = e2;
                inputStreamReader = null;
            } catch (Throwable th3) {
                th = th3;
                inputStreamReader = null;
            }
        } catch (Exception e3) {
            e = e3;
            inputStreamReader = null;
        } catch (Throwable th4) {
            th = th4;
            inputStreamReader = null;
        }
    }

    /* JADX INFO: renamed from: q */
    public final void m15856q() throws Throwable {
        InputStreamReader inputStreamReader;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(new File(m15845e(), "process.info"));
            try {
                inputStreamReader = new InputStreamReader(fileInputStream2);
                try {
                    this.f31387a = (PSDocumentProcessInfo) new C6399a().m14250d(inputStreamReader, new kl5(PSDocumentProcessInfo.class));
                    gd2.m30020a(fileInputStream2);
                    gd2.m30020a(inputStreamReader);
                } catch (Exception e) {
                    e = e;
                    fileInputStream = fileInputStream2;
                    try {
                        throw new PSException(e);
                    } catch (Throwable th) {
                        th = th;
                        gd2.m30020a(fileInputStream);
                        gd2.m30020a(inputStreamReader);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    fileInputStream = fileInputStream2;
                    gd2.m30020a(fileInputStream);
                    gd2.m30020a(inputStreamReader);
                    throw th;
                }
            } catch (Exception e2) {
                e = e2;
                inputStreamReader = null;
            } catch (Throwable th3) {
                th = th3;
                inputStreamReader = null;
            }
        } catch (Exception e3) {
            e = e3;
            inputStreamReader = null;
        } catch (Throwable th4) {
            th = th4;
            inputStreamReader = null;
        }
    }

    /* JADX INFO: renamed from: r */
    public final void m15857r(PSTempRotateInfo pSTempRotateInfo) throws Throwable {
        FileOutputStream fileOutputStream = null;
        try {
            try {
                String strM14257k = new C6399a().m14257k(pSTempRotateInfo);
                try {
                    FileOutputStream fileOutputStream2 = new FileOutputStream(new File(m15845e(), ".temp_rotate.info"));
                    try {
                        fileOutputStream2.write(strM14257k.getBytes());
                        fileOutputStream2.flush();
                        gd2.m30020a(fileOutputStream2);
                    } catch (Exception e) {
                        e = e;
                        fileOutputStream = fileOutputStream2;
                        e.printStackTrace();
                        gd2.m30020a(fileOutputStream);
                    } catch (Throwable th) {
                        th = th;
                        fileOutputStream = fileOutputStream2;
                        gd2.m30020a(fileOutputStream);
                        throw th;
                    }
                } catch (Exception e2) {
                    e = e2;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e3) {
                e = e3;
            }
        } catch (Throwable th3) {
            th = th3;
        }
    }

    /* JADX INFO: renamed from: s */
    public final void m15858s() throws Throwable {
        Throwable th;
        FileOutputStream fileOutputStream;
        Exception e;
        if (this.f31388b == null) {
            return;
        }
        try {
            String strM14257k = new C6399a().m14257k(this.f31388b);
            fileOutputStream = new FileOutputStream(new File(m15845e(), "filter.info"));
            try {
                try {
                    fileOutputStream.write(strM14257k.getBytes());
                    fileOutputStream.flush();
                    gd2.m30020a(fileOutputStream);
                } catch (Exception e2) {
                    e = e2;
                    throw new PSException(e);
                }
            } catch (Throwable th2) {
                th = th2;
                gd2.m30020a(fileOutputStream);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            fileOutputStream = null;
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            gd2.m30020a(fileOutputStream);
            throw th;
        }
    }

    /* JADX INFO: renamed from: t */
    public final void m15859t() throws Throwable {
        Throwable th;
        FileOutputStream fileOutputStream;
        Exception e;
        if (this.f31387a == null) {
            return;
        }
        try {
            String strM14257k = new C6399a().m14257k(this.f31387a);
            fileOutputStream = new FileOutputStream(new File(m15845e(), "process.info"));
            try {
                try {
                    fileOutputStream.write(strM14257k.getBytes());
                    fileOutputStream.flush();
                    gd2.m30020a(fileOutputStream);
                } catch (Exception e2) {
                    e = e2;
                    throw new PSException(e);
                }
            } catch (Throwable th2) {
                th = th2;
                gd2.m30020a(fileOutputStream);
                throw th;
            }
        } catch (Exception e3) {
            e = e3;
            fileOutputStream = null;
        } catch (Throwable th3) {
            th = th3;
            fileOutputStream = null;
            gd2.m30020a(fileOutputStream);
            throw th;
        }
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) throws Throwable {
        parcel.writeInt(this.pageID);
        parcel.writeString(this.storagePath);
        Date date = this.dateCreate;
        parcel.writeLong(date != null ? date.getTime() : -1L);
        Date date2 = this.dateModify;
        parcel.writeLong(date2 != null ? date2.getTime() : -1L);
        parcel.writeString(this.searchKeyword);
        parcel.writeInt(this.pageIndex);
        parcel.writeInt(this.documentID);
        parcel.writeByte(this.isProcessCompleted ? (byte) 1 : (byte) 0);
        parcel.writeString(this.originalFileName);
        parcel.writeLong(this.resultFileSize);
        parcel.writeInt(this.resultImageWidth);
        parcel.writeInt(this.resultImageHeight);
        parcel.writeString(this.note);
        parcel.writeString(this.ocr);
        Date date3 = this.dateOCR;
        parcel.writeLong(date3 != null ? date3.getTime() : -1L);
        Date date4 = this.dateNote;
        parcel.writeLong(date4 != null ? date4.getTime() : -1L);
        parcel.writeString(this.title);
        parcel.writeInt(this.jpgQuality);
        pv3 pv3Var = this.fileType;
        parcel.writeInt(pv3Var == null ? -1 : pv3Var.ordinal());
        Date date5 = this.dateTrash;
        parcel.writeLong(date5 != null ? date5.getTime() : -1L);
        parcel.writeInt(this.iOpt);
        parcel.writeSerializable(this.cOpt);
        iv3 iv3Var = this.status;
        parcel.writeInt(iv3Var != null ? iv3Var.ordinal() : -1);
        try {
            m15859t();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            m15858s();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public PSPage() {
        this.searchKeyword = "";
        this.isProcessCompleted = false;
        this.note = "";
        this.ocr = "";
        this.title = "";
        this.cOpt = new HashMap();
        this.searchKeyword = "";
        this.isProcessCompleted = false;
        this.resultFileSize = 0L;
        this.resultImageWidth = 0;
        this.resultImageHeight = 0;
        this.note = "";
        this.ocr = "";
        this.title = "";
        this.fileType = pv3.JPG;
        this.status = iv3.kStatusNormal;
    }
}
