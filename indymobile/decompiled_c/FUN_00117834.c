
undefined8 * FUN_00117834(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3)

{
  bool bVar1;
  ulong uVar2;
  ulong uVar3;
  ulong uVar4;
  long lVar5;
  undefined8 *puVar6;
  ulong uVar7;
  long lVar8;
  undefined8 *puVar9;
  ulong uVar10;
  int iVar11;
  undefined8 uVar12;
  undefined8 *puVar13;
  undefined8 *puVar14;
  
  if (param_1 != param_2) {
    lVar5 = (long)param_2 - (long)param_1 >> 3;
    if (1 < lVar5) {
      uVar7 = lVar5 - 2U >> 1;
      uVar4 = uVar7;
      do {
        if ((long)uVar4 <= (long)uVar7) {
          uVar10 = (uVar4 & 0x1fffffffffffffff) << 1 | 1;
          uVar2 = (uVar4 & 0x1fffffffffffffff) * 2 + 2;
          puVar9 = param_1 + uVar10;
          uVar3 = uVar10;
          if ((long)uVar2 < lVar5) {
            uVar3 = uVar2;
            if (*(int *)((long)puVar9 + 0xc) <= *(int *)((long)puVar9 + 4)) {
              uVar3 = uVar10;
            }
            puVar9 = puVar9 + (*(int *)((long)puVar9 + 4) < *(int *)((long)puVar9 + 0xc));
          }
          puVar6 = param_1 + uVar4;
          if (*(int *)((long)puVar6 + 4) <= *(int *)((long)puVar9 + 4)) {
            uVar12 = *puVar6;
            do {
              puVar14 = puVar9;
              *puVar6 = *puVar14;
              if ((long)uVar7 < (long)uVar3) break;
              uVar10 = uVar3 << 1 | 1;
              uVar2 = uVar3 * 2 + 2;
              puVar9 = param_1 + uVar10;
              uVar3 = uVar10;
              if ((long)uVar2 < lVar5) {
                uVar3 = uVar2;
                if (*(int *)((long)puVar9 + 0xc) <= *(int *)((long)puVar9 + 4)) {
                  uVar3 = uVar10;
                }
                puVar9 = puVar9 + (*(int *)((long)puVar9 + 4) < *(int *)((long)puVar9 + 0xc));
              }
              puVar6 = puVar14;
            } while ((int)((ulong)uVar12 >> 0x20) <= *(int *)((long)puVar9 + 4));
            *puVar14 = uVar12;
          }
        }
        bVar1 = 0 < (long)uVar4;
        uVar4 = uVar4 - 1;
      } while (bVar1);
    }
    puVar9 = param_2;
    if (param_2 != param_3) {
      puVar6 = param_2;
      do {
        if (*(int *)((long)puVar6 + 4) < *(int *)((long)param_1 + 4)) {
          uVar12 = *puVar6;
          *puVar6 = *param_1;
          *param_1 = uVar12;
          if (1 < lVar5) {
            if ((long)param_2 - (long)param_1 == 0x10) {
              uVar4 = 1;
              puVar9 = param_1 + 1;
            }
            else {
              uVar4 = 1;
              if (*(int *)((long)param_1 + 0xc) < *(int *)((long)param_1 + 0x14)) {
                uVar4 = 2;
              }
              puVar9 = param_1 + 1 +
                       (*(int *)((long)param_1 + 0xc) < *(int *)((long)param_1 + 0x14));
            }
            iVar11 = (int)((ulong)uVar12 >> 0x20);
            puVar14 = param_1;
            if (iVar11 <= *(int *)((long)puVar9 + 4)) {
              do {
                puVar13 = puVar9;
                *puVar14 = *puVar13;
                if ((long)(lVar5 - 2U >> 1) < (long)uVar4) break;
                uVar2 = uVar4 << 1 | 1;
                uVar7 = uVar4 * 2 + 2;
                puVar9 = param_1 + uVar2;
                uVar4 = uVar2;
                if ((long)uVar7 < lVar5) {
                  uVar4 = uVar7;
                  if (*(int *)((long)puVar9 + 0xc) <= *(int *)((long)puVar9 + 4)) {
                    uVar4 = uVar2;
                  }
                  puVar9 = puVar9 + (*(int *)((long)puVar9 + 4) < *(int *)((long)puVar9 + 0xc));
                }
                puVar14 = puVar13;
              } while (iVar11 <= *(int *)((long)puVar9 + 4));
              *puVar13 = uVar12;
            }
          }
        }
        puVar6 = puVar6 + 1;
        puVar9 = param_3;
      } while (puVar6 != param_3);
    }
    param_3 = puVar9;
    if (1 < lVar5) {
      do {
        uVar12 = *param_1;
        puVar9 = param_1;
        uVar4 = 0;
        do {
          uVar2 = uVar4 << 1 | 1;
          uVar7 = uVar4 * 2 + 2;
          puVar6 = puVar9 + uVar4 + 1;
          uVar10 = uVar2;
          if (((long)uVar7 < lVar5) &&
             (puVar6 = puVar9 + uVar4 + 2, uVar10 = uVar7,
             *(int *)((long)puVar9 + uVar4 * 8 + 0x14) <= *(int *)((long)puVar9 + uVar4 * 8 + 0xc)))
          {
            puVar6 = puVar9 + uVar4 + 1;
            uVar10 = uVar2;
          }
          *puVar9 = *puVar6;
          puVar9 = puVar6;
          uVar4 = uVar10;
        } while ((long)uVar10 <= (long)(lVar5 - 2U >> 1));
        param_2 = param_2 + -1;
        if (puVar6 == param_2) {
          *puVar6 = uVar12;
        }
        else {
          lVar8 = (long)puVar6 + (8 - (long)param_1) >> 3;
          *puVar6 = *param_2;
          *param_2 = uVar12;
          if (1 < lVar8) {
            uVar4 = lVar8 - 2U >> 1;
            if (*(int *)((long)(param_1 + uVar4) + 4) < *(int *)((long)puVar6 + 4)) {
              uVar12 = *puVar6;
              puVar9 = param_1 + uVar4;
              do {
                puVar14 = puVar9;
                *puVar6 = *puVar14;
                if (uVar4 == 0) break;
                uVar4 = uVar4 - 1 >> 1;
                puVar6 = puVar14;
                puVar9 = param_1 + uVar4;
              } while (*(int *)((long)(param_1 + uVar4) + 4) < (int)((ulong)uVar12 >> 0x20));
              *puVar14 = uVar12;
            }
          }
        }
        bVar1 = 2 < lVar5;
        lVar5 = lVar5 + -1;
      } while (bVar1);
    }
  }
  return param_3;
}

