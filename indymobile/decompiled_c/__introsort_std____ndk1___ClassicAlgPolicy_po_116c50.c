
/* void std::__ndk1::__introsort<std::__ndk1::_ClassicAlgPolicy, point_y_sorter&, cv::Point_<int>*,
   false>(cv::Point_<int>*, cv::Point_<int>*, point_y_sorter&,
   std::__ndk1::iterator_traits<cv::Point_<int>*>::difference_type, bool) */

void std::__ndk1::__introsort<std::__ndk1::_ClassicAlgPolicy,point_y_sorter&,cv::Point_<int>*,false>
               (undefined1 (*param_1) [16],undefined1 (*param_2) [16],undefined8 param_3,
               long param_4,uint param_5)

{
  long lVar1;
  undefined8 *puVar2;
  undefined1 (*pauVar3) [16];
  undefined1 *puVar4;
  ulong uVar5;
  long lVar6;
  int iVar7;
  int iVar8;
  ulong uVar9;
  undefined8 *puVar10;
  undefined8 uVar11;
  undefined1 (*pauVar12) [16];
  undefined1 (*pauVar13) [16];
  undefined1 (*pauVar14) [16];
  undefined8 uVar15;
  undefined1 (*pauVar16) [16];
  long lVar17;
  undefined1 (*pauVar18) [16];
  undefined1 (*pauVar19) [16];
  undefined1 auVar20 [16];
  
LAB_00116c80:
  do {
    pauVar14 = param_1;
    uVar9 = (long)param_2 - (long)pauVar14 >> 3;
    if ((long)uVar9 < 3) {
      if (uVar9 < 2) {
        return;
      }
      if (uVar9 == 2) {
        if (*(int *)(*pauVar14 + 4) <= *(int *)(param_2[-1] + 0xc)) {
          return;
        }
        uVar11 = *(undefined8 *)*pauVar14;
        *(undefined8 *)*pauVar14 = *(undefined8 *)(param_2[-1] + 8);
        *(undefined8 *)(param_2[-1] + 8) = uVar11;
        return;
      }
    }
    else {
      if (uVar9 == 3) {
        iVar7 = *(int *)(*pauVar14 + 0xc);
        if (iVar7 < *(int *)(*pauVar14 + 4)) {
          uVar11 = *(undefined8 *)*pauVar14;
          if (*(int *)(param_2[-1] + 0xc) < iVar7) {
            *(undefined8 *)*pauVar14 = *(undefined8 *)(param_2[-1] + 8);
          }
          else {
            *(undefined8 *)*pauVar14 = *(undefined8 *)(*pauVar14 + 8);
            *(undefined8 *)(*pauVar14 + 8) = uVar11;
            if ((int)((ulong)uVar11 >> 0x20) <= *(int *)(param_2[-1] + 0xc)) {
              return;
            }
            *(undefined8 *)(*pauVar14 + 8) = *(undefined8 *)(param_2[-1] + 8);
          }
          *(undefined8 *)(param_2[-1] + 8) = uVar11;
          return;
        }
        if (iVar7 <= *(int *)(param_2[-1] + 0xc)) {
          return;
        }
        uVar11 = *(undefined8 *)(*pauVar14 + 8);
        *(undefined8 *)(*pauVar14 + 8) = *(undefined8 *)(param_2[-1] + 8);
        *(undefined8 *)(param_2[-1] + 8) = uVar11;
        if (*(int *)(*pauVar14 + 4) <= *(int *)(*pauVar14 + 0xc)) {
          return;
        }
        auVar20 = NEON_ext(*pauVar14,*pauVar14,8,1);
        *(long *)(*pauVar14 + 8) = auVar20._8_8_;
        *(long *)*pauVar14 = auVar20._0_8_;
        return;
      }
      if (uVar9 == 4) {
        FUN_00117730(pauVar14,*pauVar14 + 8,pauVar14 + 1,param_2[-1] + 8,param_3);
        return;
      }
      if (uVar9 == 5) {
        FUN_00117730(pauVar14,*pauVar14 + 8,pauVar14 + 1,pauVar14[1] + 8,param_3);
        if (*(int *)(pauVar14[1] + 0xc) <= *(int *)(param_2[-1] + 0xc)) {
          return;
        }
        uVar11 = *(undefined8 *)(pauVar14[1] + 8);
        *(undefined8 *)(pauVar14[1] + 8) = *(undefined8 *)(param_2[-1] + 8);
        *(undefined8 *)(param_2[-1] + 8) = uVar11;
        if (*(int *)(pauVar14[1] + 4) <= *(int *)(pauVar14[1] + 0xc)) {
          return;
        }
        uVar11 = *(undefined8 *)pauVar14[1];
        uVar15 = *(undefined8 *)(pauVar14[1] + 8);
        iVar7 = *(int *)(*pauVar14 + 0xc);
        *(undefined8 *)pauVar14[1] = uVar15;
        *(undefined8 *)(pauVar14[1] + 8) = uVar11;
        iVar8 = (int)((ulong)uVar15 >> 0x20);
        if (iVar7 <= iVar8) {
          return;
        }
        iVar7 = *(int *)(*pauVar14 + 4);
        uVar11 = *(undefined8 *)(*pauVar14 + 8);
        *(undefined8 *)(*pauVar14 + 8) = uVar15;
        *(undefined8 *)pauVar14[1] = uVar11;
        if (iVar7 <= iVar8) {
          return;
        }
        uVar11 = *(undefined8 *)*pauVar14;
        *(undefined8 *)*pauVar14 = uVar15;
        *(undefined8 *)(*pauVar14 + 8) = uVar11;
        return;
      }
    }
    if ((long)uVar9 < 0x18) {
      pauVar12 = (undefined1 (*) [16])(*pauVar14 + 8);
      if ((param_5 & 1) == 0) {
        if (pauVar14 == param_2 || pauVar12 == param_2) {
          return;
        }
        do {
          pauVar13 = pauVar12;
          if (*(int *)(*pauVar14 + 0xc) < *(int *)(*pauVar14 + 4)) {
            uVar11 = *(undefined8 *)*pauVar13;
            pauVar14 = pauVar13;
            do {
              iVar7 = *(int *)(pauVar14[-1] + 4);
              pauVar12 = (undefined1 (*) [16])(pauVar14[-1] + 8);
              *(undefined8 *)*pauVar14 = *(undefined8 *)*pauVar12;
              pauVar14 = pauVar12;
            } while ((int)((ulong)uVar11 >> 0x20) < iVar7);
            *(undefined8 *)*pauVar12 = uVar11;
          }
          pauVar12 = (undefined1 (*) [16])(*pauVar13 + 8);
          pauVar14 = pauVar13;
        } while ((undefined1 (*) [16])(*pauVar13 + 8) != param_2);
        return;
      }
      if (pauVar14 == param_2 || pauVar12 == param_2) {
        return;
      }
      lVar6 = 0;
      pauVar13 = pauVar14;
      do {
        pauVar16 = pauVar12;
        if (*(int *)(*pauVar13 + 0xc) < *(int *)(*pauVar13 + 4)) {
          uVar11 = *(undefined8 *)*pauVar16;
          lVar1 = lVar6;
          do {
            lVar17 = lVar1;
            puVar2 = (undefined8 *)(*pauVar14 + lVar17);
            puVar2[1] = *puVar2;
            pauVar12 = pauVar14;
            if (lVar17 == 0) goto LAB_00117320;
            lVar1 = lVar17 + -8;
          } while ((int)((ulong)uVar11 >> 0x20) < *(int *)((long)puVar2 + -4));
          pauVar12 = (undefined1 (*) [16])(*pauVar14 + lVar17);
LAB_00117320:
          *(undefined8 *)*pauVar12 = uVar11;
        }
        lVar6 = lVar6 + 8;
        pauVar12 = (undefined1 (*) [16])(*pauVar16 + 8);
        pauVar13 = pauVar16;
        if ((undefined1 (*) [16])(*pauVar16 + 8) == param_2) {
          return;
        }
      } while( true );
    }
    if (param_4 == 0) {
      if (pauVar14 == param_2) {
        return;
      }
      FUN_00117834(pauVar14,param_2,param_2,param_3);
      return;
    }
    iVar7 = *(int *)(param_2[-1] + 0xc);
    puVar2 = (undefined8 *)(*pauVar14 + (uVar9 >> 1) * 8);
    param_1 = pauVar14;
    if (uVar9 < 0x81) {
      iVar8 = *(int *)(*pauVar14 + 4);
      if (iVar8 < *(int *)((long)puVar2 + 4)) {
        uVar11 = *puVar2;
        if (iVar7 < iVar8) {
          *puVar2 = *(undefined8 *)(param_2[-1] + 8);
        }
        else {
          *puVar2 = *(undefined8 *)*pauVar14;
          *(undefined8 *)*pauVar14 = uVar11;
          if ((int)((ulong)uVar11 >> 0x20) <= *(int *)(param_2[-1] + 0xc)) goto LAB_00116e68;
          *(undefined8 *)*pauVar14 = *(undefined8 *)(param_2[-1] + 8);
        }
        *(undefined8 *)(param_2[-1] + 8) = uVar11;
      }
      else if (iVar7 < iVar8) {
        uVar11 = *(undefined8 *)*pauVar14;
        *(undefined8 *)*pauVar14 = *(undefined8 *)(param_2[-1] + 8);
        *(undefined8 *)(param_2[-1] + 8) = uVar11;
        if (*(int *)(*pauVar14 + 4) < *(int *)((long)puVar2 + 4)) {
          uVar11 = *puVar2;
          *puVar2 = *(undefined8 *)*pauVar14;
          *(undefined8 *)*pauVar14 = uVar11;
          goto joined_r0x00116fd0;
        }
      }
LAB_00116e68:
      if ((param_5 & 1) != 0) goto LAB_00116e70;
LAB_00116fd4:
      uVar11 = *(undefined8 *)*pauVar14;
      if (*(int *)(*pauVar14 + 4) <= *(int *)(pauVar14[-1] + 0xc)) {
        iVar7 = (int)((ulong)uVar11 >> 0x20);
        pauVar12 = pauVar14;
        if (iVar7 < *(int *)(param_2[-1] + 0xc)) {
          do {
            puVar4 = *param_1;
            param_1 = (undefined1 (*) [16])(*param_1 + 8);
          } while (*(int *)(puVar4 + 0xc) <= iVar7);
        }
        else {
          do {
            param_1 = (undefined1 (*) [16])(*pauVar12 + 8);
            if (param_2 <= param_1) break;
            puVar4 = *pauVar12;
            pauVar12 = param_1;
          } while (*(int *)(puVar4 + 0xc) <= iVar7);
        }
        pauVar12 = param_2;
        if (param_1 < param_2) {
          do {
            pauVar13 = pauVar12 + -1;
            pauVar12 = (undefined1 (*) [16])(pauVar12[-1] + 8);
          } while (iVar7 < *(int *)(*pauVar13 + 0xc));
        }
        while (param_1 < pauVar12) {
          uVar15 = *(undefined8 *)*param_1;
          *(undefined8 *)*param_1 = *(undefined8 *)*pauVar12;
          *(undefined8 *)*pauVar12 = uVar15;
          do {
            puVar4 = *param_1;
            param_1 = (undefined1 (*) [16])(*param_1 + 8);
          } while (*(int *)(puVar4 + 0xc) <= iVar7);
          do {
            pauVar13 = pauVar12 + -1;
            pauVar12 = (undefined1 (*) [16])(pauVar12[-1] + 8);
          } while (iVar7 < *(int *)(*pauVar13 + 0xc));
        }
        pauVar12 = (undefined1 (*) [16])(param_1[-1] + 8);
        if (pauVar12 != pauVar14) {
          *(undefined8 *)*pauVar14 = *(undefined8 *)*pauVar12;
        }
        param_5 = 0;
        *(undefined8 *)*pauVar12 = uVar11;
        param_4 = param_4 + -1;
        goto LAB_00116c80;
      }
    }
    else {
      iVar8 = *(int *)((long)puVar2 + 4);
      if (iVar8 < *(int *)(*pauVar14 + 4)) {
        uVar11 = *(undefined8 *)*pauVar14;
        if (iVar7 < iVar8) {
          *(undefined8 *)*pauVar14 = *(undefined8 *)(param_2[-1] + 8);
        }
        else {
          *(undefined8 *)*pauVar14 = *puVar2;
          *puVar2 = uVar11;
          if ((int)((ulong)uVar11 >> 0x20) <= *(int *)(param_2[-1] + 0xc)) goto LAB_00116dd4;
          *puVar2 = *(undefined8 *)(param_2[-1] + 8);
        }
        *(undefined8 *)(param_2[-1] + 8) = uVar11;
      }
      else if (iVar7 < iVar8) {
        uVar11 = *puVar2;
        *puVar2 = *(undefined8 *)(param_2[-1] + 8);
        *(undefined8 *)(param_2[-1] + 8) = uVar11;
        if (*(int *)((long)puVar2 + 4) < *(int *)(*pauVar14 + 4)) {
          uVar11 = *(undefined8 *)*pauVar14;
          *(undefined8 *)*pauVar14 = *puVar2;
          *puVar2 = uVar11;
        }
      }
LAB_00116dd4:
      iVar7 = *(int *)((long)puVar2 + -4);
      puVar10 = puVar2 + -1;
      if (iVar7 < *(int *)(*pauVar14 + 0xc)) {
        uVar11 = *(undefined8 *)(*pauVar14 + 8);
        if (*(int *)(param_2[-1] + 4) < iVar7) {
          *(undefined8 *)(*pauVar14 + 8) = *(undefined8 *)param_2[-1];
        }
        else {
          *(undefined8 *)(*pauVar14 + 8) = *puVar10;
          *puVar10 = uVar11;
          if ((int)((ulong)uVar11 >> 0x20) <= *(int *)(param_2[-1] + 4)) goto LAB_00116ea0;
          *puVar10 = *(undefined8 *)param_2[-1];
        }
        *(undefined8 *)param_2[-1] = uVar11;
      }
      else if (*(int *)(param_2[-1] + 4) < iVar7) {
        uVar11 = *puVar10;
        *puVar10 = *(undefined8 *)param_2[-1];
        *(undefined8 *)param_2[-1] = uVar11;
        if (*(int *)((long)puVar2 + -4) < *(int *)(*pauVar14 + 0xc)) {
          uVar11 = *(undefined8 *)(*pauVar14 + 8);
          *(undefined8 *)(*pauVar14 + 8) = *puVar10;
          *puVar10 = uVar11;
        }
      }
LAB_00116ea0:
      iVar7 = *(int *)((long)puVar2 + 0xc);
      if (iVar7 < *(int *)(pauVar14[1] + 4)) {
        uVar11 = *(undefined8 *)pauVar14[1];
        if (*(int *)(param_2[-2] + 0xc) < iVar7) {
          *(undefined8 *)pauVar14[1] = *(undefined8 *)(param_2[-2] + 8);
        }
        else {
          *(undefined8 *)pauVar14[1] = puVar2[1];
          puVar2[1] = uVar11;
          if ((int)((ulong)uVar11 >> 0x20) <= *(int *)(param_2[-2] + 0xc)) goto LAB_00116f30;
          puVar2[1] = *(undefined8 *)(param_2[-2] + 8);
        }
        *(undefined8 *)(param_2[-2] + 8) = uVar11;
      }
      else if (*(int *)(param_2[-2] + 0xc) < iVar7) {
        uVar11 = puVar2[1];
        puVar2[1] = *(undefined8 *)(param_2[-2] + 8);
        *(undefined8 *)(param_2[-2] + 8) = uVar11;
        if (*(int *)((long)puVar2 + 0xc) < *(int *)(pauVar14[1] + 4)) {
          uVar11 = *(undefined8 *)pauVar14[1];
          *(undefined8 *)pauVar14[1] = puVar2[1];
          puVar2[1] = uVar11;
        }
      }
LAB_00116f30:
      iVar7 = *(int *)((long)puVar2 + 4);
      iVar8 = *(int *)((long)puVar2 + 0xc);
      if (iVar7 < *(int *)((long)puVar2 + -4)) {
        uVar11 = *puVar10;
        if (iVar8 < iVar7) {
          uVar15 = puVar2[1];
          puVar2[1] = uVar11;
          *puVar10 = uVar15;
          uVar11 = *puVar2;
        }
        else {
          uVar15 = *puVar2;
          *puVar2 = uVar11;
          *puVar10 = uVar15;
          if (iVar8 < (int)((ulong)uVar11 >> 0x20)) {
            uVar15 = puVar2[1];
            *puVar2 = uVar15;
            puVar2[1] = uVar11;
            uVar11 = uVar15;
          }
        }
      }
      else {
        uVar15 = *puVar2;
        uVar11 = uVar15;
        if (iVar8 < iVar7) {
          uVar11 = puVar2[1];
          *puVar2 = uVar11;
          puVar2[1] = uVar15;
          if ((int)((ulong)uVar11 >> 0x20) < *(int *)((long)puVar2 + -4)) {
            uVar15 = *puVar10;
            *puVar10 = uVar11;
            *puVar2 = uVar15;
            uVar11 = uVar15;
          }
        }
      }
      uVar15 = *(undefined8 *)*pauVar14;
      *(undefined8 *)*pauVar14 = uVar11;
      *puVar2 = uVar15;
joined_r0x00116fd0:
      if ((param_5 & 1) == 0) goto LAB_00116fd4;
LAB_00116e70:
      uVar11 = *(undefined8 *)*pauVar14;
    }
    param_4 = param_4 + -1;
    lVar6 = 0;
    do {
      lVar1 = lVar6 + 8;
      lVar17 = lVar6 + 0xc;
      iVar7 = (int)((ulong)uVar11 >> 0x20);
      lVar6 = lVar1;
    } while (*(int *)(*pauVar14 + lVar17) < iVar7);
    pauVar12 = (undefined1 (*) [16])(*pauVar14 + lVar1);
    pauVar13 = param_2;
    if (lVar1 == 8) {
      do {
        if (pauVar13 <= pauVar12) break;
        pauVar16 = pauVar13 + -1;
        pauVar13 = (undefined1 (*) [16])(pauVar13[-1] + 8);
      } while (iVar7 <= *(int *)(*pauVar16 + 0xc));
    }
    else {
      do {
        pauVar16 = pauVar13 + -1;
        pauVar13 = (undefined1 (*) [16])(pauVar13[-1] + 8);
      } while (iVar7 <= *(int *)(*pauVar16 + 0xc));
    }
    pauVar16 = pauVar12;
    pauVar18 = pauVar13;
    if (pauVar12 < pauVar13) {
      do {
        uVar15 = *(undefined8 *)*pauVar16;
        *(undefined8 *)*pauVar16 = *(undefined8 *)*pauVar18;
        *(undefined8 *)*pauVar18 = uVar15;
        do {
          pauVar19 = pauVar16;
          pauVar16 = (undefined1 (*) [16])(*pauVar19 + 8);
        } while (*(int *)(*pauVar19 + 0xc) < iVar7);
        do {
          pauVar3 = pauVar18 + -1;
          pauVar18 = (undefined1 (*) [16])(pauVar18[-1] + 8);
        } while (iVar7 <= *(int *)(*pauVar3 + 0xc));
      } while (pauVar16 < pauVar18);
    }
    else {
      pauVar19 = (undefined1 (*) [16])(pauVar12[-1] + 8);
    }
    if (pauVar19 != pauVar14) {
      *(undefined8 *)*pauVar14 = *(undefined8 *)*pauVar19;
    }
    *(undefined8 *)*pauVar19 = uVar11;
    if (pauVar12 < pauVar13) {
LAB_00117100:
      __introsort<std::__ndk1::_ClassicAlgPolicy,point_y_sorter&,cv::Point_<int>*,false>
                (pauVar14,pauVar19,param_3,param_4,param_5 & 1);
      param_5 = 0;
      param_1 = (undefined1 (*) [16])(*pauVar19 + 8);
    }
    else {
      uVar9 = FUN_00117478(pauVar14,pauVar19,param_3);
      uVar5 = FUN_00117478((undefined1 (*) [16])(*pauVar19 + 8),param_2,param_3);
      if ((uVar5 & 1) == 0) {
        param_1 = (undefined1 (*) [16])(*pauVar19 + 8);
        if ((uVar9 & 1) == 0) goto LAB_00117100;
      }
      else {
        param_2 = pauVar19;
        if ((uVar9 & 1) != 0) {
          return;
        }
      }
    }
  } while( true );
}

