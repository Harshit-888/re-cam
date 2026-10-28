package com.indymobile.app.model.bean;

import com.indymobile.app.model.PSDocument;
import java.util.ArrayList;
import java.util.List;
import p000.y75;

/* JADX INFO: compiled from: r8-map-id-fd227b9ca2408bf12dab7890d81a029f48c8368719a26ab66e8084b55e10ecd8 */
/* JADX INFO: loaded from: classes2.dex */
public class PSFolderInfoBean {
    public List<PSDocumentInfoBean> documentInfoBeanList;
    public int folderElementCount;
    public long folderElementSize;
    public String folderPath;

    /* JADX INFO: renamed from: c */
    public static PSFolderInfoBean m15867c(PSDocument pSDocument, ArrayList arrayList, boolean z, boolean z2, boolean z3) {
        PSFolderInfoBean pSFolderInfoBean = new PSFolderInfoBean();
        pSFolderInfoBean.documentInfoBeanList = arrayList;
        int i = 0;
        int i2 = pSDocument != null ? pSDocument.documentID : 0;
        if (z) {
            ArrayList arrayList2 = new ArrayList();
            pSFolderInfoBean.m15869b(arrayList2, i2);
            ArrayList arrayList3 = new ArrayList();
            int size = arrayList2.size();
            while (i < size) {
                Object obj = arrayList2.get(i);
                i++;
                arrayList3.add(((PSDocument) obj).documentTitle);
            }
            pSFolderInfoBean.folderPath = y75.m38049c("/", arrayList3);
        }
        if (z3) {
            for (PSDocumentInfoBean pSDocumentInfoBean : pSFolderInfoBean.documentInfoBeanList) {
                PSDocument pSDocument2 = pSDocumentInfoBean.document;
                if (pSDocument2.directoryId == i2) {
                    if (pSDocument2.isDirectory) {
                        pSFolderInfoBean.m15868a(pSDocumentInfoBean);
                        pSFolderInfoBean.folderElementSize += pSDocumentInfoBean.folderElementSize;
                        pSFolderInfoBean.folderElementCount += pSDocumentInfoBean.folderElementCount;
                    } else {
                        pSFolderInfoBean.folderElementSize += pSDocumentInfoBean.documentSize;
                        pSFolderInfoBean.folderElementCount++;
                    }
                }
            }
        }
        if (z2) {
            for (int size2 = pSFolderInfoBean.documentInfoBeanList.size() - 1; size2 >= 0; size2--) {
                PSDocumentInfoBean pSDocumentInfoBean2 = pSFolderInfoBean.documentInfoBeanList.get(size2);
                if (pSDocumentInfoBean2.document.directoryId != i2) {
                    pSFolderInfoBean.documentInfoBeanList.remove(pSDocumentInfoBean2);
                }
            }
        }
        return pSFolderInfoBean;
    }

    /* JADX INFO: renamed from: a */
    public final void m15868a(PSDocumentInfoBean pSDocumentInfoBean) {
        for (PSDocumentInfoBean pSDocumentInfoBean2 : this.documentInfoBeanList) {
            PSDocument pSDocument = pSDocumentInfoBean2.document;
            if (pSDocument.directoryId == pSDocumentInfoBean.document.documentID) {
                if (pSDocument.isDirectory) {
                    m15868a(pSDocumentInfoBean2);
                    pSDocumentInfoBean.folderElementSize += pSDocumentInfoBean2.folderElementSize;
                    pSDocumentInfoBean.folderElementCount += pSDocumentInfoBean2.folderElementCount;
                } else {
                    pSDocumentInfoBean.folderElementSize += pSDocumentInfoBean2.documentSize;
                    pSDocumentInfoBean.folderElementCount++;
                }
            }
        }
    }

    /* JADX INFO: renamed from: b */
    public final void m15869b(ArrayList arrayList, int i) {
        if (i != 0) {
            for (PSDocumentInfoBean pSDocumentInfoBean : this.documentInfoBeanList) {
                PSDocument pSDocument = pSDocumentInfoBean.document;
                if (pSDocument.documentID == i) {
                    arrayList.add(0, pSDocument);
                    m15869b(arrayList, pSDocumentInfoBean.document.directoryId);
                    return;
                }
            }
        }
    }
}
