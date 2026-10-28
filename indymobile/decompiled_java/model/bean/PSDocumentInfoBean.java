package com.indymobile.app.model.bean;

import com.indymobile.app.model.PSDocument;
import com.indymobile.app.model.PSPage;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import p000.fu1;
import p000.ws3;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSDocumentInfoBean {
    public PSDocument document;
    public long documentSize;
    public PSPage firstPage;
    public int folderElementCount;
    public long folderElementSize;
    public int pageCount;
    public int searchPageCount;
    public int unfinishedCount;

    /* JADX INFO: renamed from: com.indymobile.app.model.bean.PSDocumentInfoBean$2 */
    /* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
    class C66682 implements Comparator<PSDocumentInfoBean> {
        @Override // java.util.Comparator
        public final int compare(PSDocumentInfoBean pSDocumentInfoBean, PSDocumentInfoBean pSDocumentInfoBean2) {
            return (!pSDocumentInfoBean.document.isDirectory ? 1 : 0) - (!pSDocumentInfoBean2.document.isDirectory ? 1 : 0);
        }
    }

    /* JADX INFO: renamed from: a */
    public static void m15865a(List list, boolean z) {
        try {
            Collections.sort(list, new Comparator<PSDocumentInfoBean>(z, new fu1()) { // from class: com.indymobile.app.model.bean.PSDocumentInfoBean.1

                /* JADX INFO: renamed from: a */
                public final /* synthetic */ boolean f31395a;

                @Override // java.util.Comparator
                public final int compare(PSDocumentInfoBean pSDocumentInfoBean, PSDocumentInfoBean pSDocumentInfoBean2) {
                    PSDocumentInfoBean pSDocumentInfoBean3 = pSDocumentInfoBean;
                    PSDocumentInfoBean pSDocumentInfoBean4 = pSDocumentInfoBean2;
                    return this.f31395a ? fu1.m29708a(pSDocumentInfoBean3.document.documentTitle, pSDocumentInfoBean4.document.documentTitle) : fu1.m29708a(pSDocumentInfoBean4.document.documentTitle, pSDocumentInfoBean3.document.documentTitle);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            ws3.m37351e(e);
        }
    }

    /* JADX INFO: renamed from: b */
    public static void m15866b(List list) {
        try {
            Collections.sort(list, new C66682());
        } catch (Exception e) {
            e.printStackTrace();
            ws3.m37351e(e);
        }
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof PSDocumentInfoBean) && this.document.documentID == ((PSDocumentInfoBean) obj).document.documentID;
    }

    public final int hashCode() {
        return this.document.documentID;
    }
}
