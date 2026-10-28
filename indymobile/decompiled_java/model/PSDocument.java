package com.indymobile.app.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import com.box.androidsdk.content.models.BoxGroup;
import java.util.Date;
import p000.av3;
import p000.dy3;
import p000.fu4;
import p000.iv3;
import p000.ud2;
import p000.wv3;
import p000.zu3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSDocument implements Parcelable {
    public static final Parcelable.Creator<PSDocument> CREATOR = new C66561();
    public static final String TABLE_NAME = "document";
    public static int columnCount = 19;

    @fu4("cOpt")
    public String cOpt;

    @fu4("created")
    public Date dateCreate;

    @fu4("modified")
    public Date dateModify;

    @fu4("dateTrash")
    public Date dateTrash;

    @fu4("folderId")
    public int directoryId;

    @fu4(BoxGroup.TYPE)
    public int documentGroupID;

    @fu4("id")
    public int documentID;

    @fu4("title")
    public String documentTitle;

    @fu4("iOpt")
    public int iOpt;

    @fu4("isFolder")
    public boolean isDirectory;

    @fu4("scaleType")
    public zu3 pageContentMode;

    @fu4("marginBottom")
    public int pageMarginBottom;

    @fu4("marginLeft")
    public int pageMarginLeft;

    @fu4("marginRight")
    public int pageMarginRight;

    @fu4("marginTop")
    public int pageMarginTop;

    @fu4("orientation")
    public av3 pageOrientation;

    @fu4("paperId")
    public int paperSizeID;

    @fu4("keyword")
    public String searchKeyword;

    @fu4("status")
    public iv3 status;

    /* JADX INFO: renamed from: com.indymobile.app.model.PSDocument$1 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    public class C66561 implements Parcelable.Creator<PSDocument> {
        @Override // android.os.Parcelable.Creator
        public final PSDocument createFromParcel(Parcel parcel) {
            PSDocument pSDocument = new PSDocument();
            pSDocument.documentID = parcel.readInt();
            pSDocument.documentTitle = parcel.readString();
            long j = parcel.readLong();
            pSDocument.dateCreate = j == -1 ? null : new Date(j);
            long j2 = parcel.readLong();
            pSDocument.dateModify = j2 == -1 ? null : new Date(j2);
            pSDocument.searchKeyword = parcel.readString();
            pSDocument.paperSizeID = parcel.readInt();
            pSDocument.documentGroupID = parcel.readInt();
            int i = parcel.readInt();
            pSDocument.pageContentMode = i == -1 ? null : zu3.values()[i];
            int i2 = parcel.readInt();
            pSDocument.pageOrientation = i2 == -1 ? null : av3.values()[i2];
            pSDocument.pageMarginTop = parcel.readInt();
            pSDocument.pageMarginLeft = parcel.readInt();
            pSDocument.pageMarginBottom = parcel.readInt();
            pSDocument.pageMarginRight = parcel.readInt();
            pSDocument.isDirectory = parcel.readByte() != 0;
            pSDocument.directoryId = parcel.readInt();
            long j3 = parcel.readLong();
            pSDocument.dateTrash = j3 == -1 ? null : new Date(j3);
            pSDocument.iOpt = parcel.readInt();
            pSDocument.cOpt = parcel.readString();
            int i3 = parcel.readInt();
            pSDocument.status = i3 != -1 ? iv3.values()[i3] : null;
            return pSDocument;
        }

        @Override // android.os.Parcelable.Creator
        public final PSDocument[] newArray(int i) {
            return new PSDocument[i];
        }
    }

    public PSDocument(Cursor cursor) {
        int i;
        Exception e;
        int i2;
        this.documentID = cursor.getInt(0);
        this.documentTitle = cursor.getString(1);
        this.dateCreate = new Date(cursor.getLong(2));
        this.dateModify = new Date(cursor.getLong(3));
        this.searchKeyword = cursor.getString(4);
        this.paperSizeID = cursor.getInt(5);
        this.documentGroupID = cursor.getInt(6);
        this.pageContentMode = zu3.kPSDocumentPageContentModeAspectFit;
        try {
            i = 8;
            try {
                this.pageContentMode = zu3.values()[cursor.getInt(7)];
            } catch (Exception e2) {
                e = e2;
                e.printStackTrace();
            }
        } catch (Exception e3) {
            i = 7;
            e = e3;
        }
        this.pageOrientation = av3.kPSDocumentPageOrientationPortrait;
        try {
            i2 = i + 1;
            try {
                this.pageOrientation = av3.values()[cursor.getInt(i)];
            } catch (Exception e4) {
                e = e4;
                i = i2;
                e.printStackTrace();
                i2 = i;
            }
        } catch (Exception e5) {
            e = e5;
        }
        this.pageMarginTop = cursor.getInt(i2);
        this.pageMarginLeft = cursor.getInt(i2 + 1);
        this.pageMarginBottom = cursor.getInt(i2 + 2);
        this.pageMarginRight = cursor.getInt(i2 + 3);
        int i3 = i2 + 5;
        this.isDirectory = cursor.getInt(i2 + 4) != 0;
        this.directoryId = cursor.getInt(i3);
        this.dateTrash = new Date(cursor.getLong(i2 + 6));
        int i4 = i2 + 8;
        this.iOpt = cursor.getInt(i2 + 7);
        int i5 = i2 + 9;
        this.cOpt = cursor.getString(i4);
        this.status = iv3.kStatusNormal;
        try {
            this.status = iv3.values()[cursor.getInt(i5)];
        } catch (Exception e6) {
            e6.printStackTrace();
        }
    }

    /* JADX INFO: renamed from: a, reason: merged with bridge method [inline-methods] */
    public final PSDocument clone() {
        PSDocument pSDocument = new PSDocument();
        pSDocument.documentID = this.documentID;
        pSDocument.documentTitle = this.documentTitle;
        pSDocument.dateCreate = this.dateCreate;
        pSDocument.dateModify = this.dateModify;
        pSDocument.searchKeyword = this.searchKeyword;
        pSDocument.paperSizeID = this.paperSizeID;
        pSDocument.documentGroupID = this.documentGroupID;
        pSDocument.pageContentMode = this.pageContentMode;
        pSDocument.pageOrientation = this.pageOrientation;
        pSDocument.pageMarginTop = this.pageMarginTop;
        pSDocument.pageMarginLeft = this.pageMarginLeft;
        pSDocument.pageMarginBottom = this.pageMarginBottom;
        pSDocument.pageMarginRight = this.pageMarginRight;
        pSDocument.isDirectory = this.isDirectory;
        pSDocument.directoryId = this.directoryId;
        pSDocument.dateTrash = this.dateTrash;
        pSDocument.iOpt = this.iOpt;
        pSDocument.cOpt = this.cOpt;
        pSDocument.status = this.status;
        return pSDocument;
    }

    /* JADX INFO: renamed from: b */
    public final ContentValues m15839b() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("i_document_id", Integer.valueOf(this.documentID));
        contentValues.put("c_document_title", this.documentTitle);
        contentValues.put("i_date_create", Long.valueOf(this.dateCreate.getTime()));
        contentValues.put("i_date_modify", Long.valueOf(this.dateModify.getTime()));
        contentValues.put("c_search_keyword", this.searchKeyword);
        contentValues.put("i_paper_size_id", Integer.valueOf(this.paperSizeID));
        contentValues.put("i_document_group_id", Integer.valueOf(this.documentGroupID));
        contentValues.put("i_page_content_mode", Integer.valueOf(this.pageContentMode.ordinal()));
        contentValues.put("i_page_orientation", Integer.valueOf(this.pageOrientation.ordinal()));
        contentValues.put("i_page_margin_top", Integer.valueOf(this.pageMarginTop));
        contentValues.put("i_page_margin_left", Integer.valueOf(this.pageMarginLeft));
        contentValues.put("i_page_margin_bottom", Integer.valueOf(this.pageMarginBottom));
        contentValues.put("i_page_margin_right", Integer.valueOf(this.pageMarginRight));
        contentValues.put("i_is_directory", Boolean.valueOf(this.isDirectory));
        contentValues.put("i_directory_id", Integer.valueOf(this.directoryId));
        Date date = this.dateTrash;
        if (date != null) {
            contentValues.put("i_date_trash", Long.valueOf(date.getTime()));
        }
        contentValues.put("i_opt", Integer.valueOf(this.iOpt));
        contentValues.put("c_opt", this.cOpt);
        contentValues.put("i_status", Integer.valueOf(this.status.ordinal()));
        return contentValues;
    }

    @Override // android.os.Parcelable
    public final int describeContents() {
        return 0;
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof PSDocument) && this.documentID == ((PSDocument) obj).documentID;
    }

    public final int hashCode() {
        return this.documentID;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.documentID);
        parcel.writeString(this.documentTitle);
        Date date = this.dateCreate;
        parcel.writeLong(date != null ? date.getTime() : -1L);
        Date date2 = this.dateModify;
        parcel.writeLong(date2 != null ? date2.getTime() : -1L);
        parcel.writeString(this.searchKeyword);
        parcel.writeInt(this.paperSizeID);
        parcel.writeInt(this.documentGroupID);
        zu3 zu3Var = this.pageContentMode;
        parcel.writeInt(zu3Var == null ? -1 : zu3Var.ordinal());
        av3 av3Var = this.pageOrientation;
        parcel.writeInt(av3Var == null ? -1 : av3Var.ordinal());
        parcel.writeInt(this.pageMarginTop);
        parcel.writeInt(this.pageMarginLeft);
        parcel.writeInt(this.pageMarginBottom);
        parcel.writeInt(this.pageMarginRight);
        parcel.writeByte(this.isDirectory ? (byte) 1 : (byte) 0);
        parcel.writeInt(this.directoryId);
        Date date3 = this.dateTrash;
        parcel.writeLong(date3 != null ? date3.getTime() : -1L);
        parcel.writeInt(this.iOpt);
        parcel.writeString(this.cOpt);
        iv3 iv3Var = this.status;
        parcel.writeInt(iv3Var != null ? iv3Var.ordinal() : -1);
    }

    public PSDocument() {
        Date date = new Date();
        this.documentTitle = ud2.m36439n();
        this.dateCreate = date;
        this.dateModify = date;
        this.searchKeyword = "";
        dy3 dy3VarM28727f = dy3.m28727f();
        dy3VarM28727f.getClass();
        int iM37387f = wv3.m37387f();
        try {
            iM37387f = Integer.parseInt(dy3VarM28727f.f46873f);
        } catch (Exception unused) {
        }
        this.paperSizeID = iM37387f;
        this.documentGroupID = 0;
        dy3 dy3VarM28727f2 = dy3.m28727f();
        dy3VarM28727f2.getClass();
        zu3 zu3Var = zu3.kPSDocumentPageContentModeAspectFit;
        try {
            zu3Var = zu3.values()[Integer.parseInt(dy3VarM28727f2.f46875h)];
        } catch (Exception unused2) {
        }
        this.pageContentMode = zu3Var;
        dy3 dy3VarM28727f3 = dy3.m28727f();
        dy3VarM28727f3.getClass();
        av3 av3Var = av3.kPSDocumentPageOrientationAuto;
        try {
            av3Var = av3.values()[Integer.parseInt(dy3VarM28727f3.f46874g)];
        } catch (Exception unused3) {
        }
        this.pageOrientation = av3Var;
        this.pageMarginTop = 0;
        this.pageMarginLeft = 0;
        this.pageMarginBottom = 0;
        this.pageMarginRight = 0;
        this.isDirectory = false;
        this.directoryId = 0;
        this.status = iv3.kStatusNormal;
    }
}
