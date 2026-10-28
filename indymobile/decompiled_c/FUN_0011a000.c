
void FUN_0011a000(long param_1,undefined8 param_2,long param_3,undefined8 *param_4)

{
  ulong uVar1;
  ulong uVar2;
  int iVar3;
  void *pvVar4;
  long lVar5;
  undefined8 *puVar6;
  undefined8 *puVar7;
  ulong uVar8;
  undefined8 uVar9;
  ulong uVar10;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 *puVar13;
  undefined8 uVar14;
  undefined8 uVar15;
  undefined8 uVar16;
  undefined8 uVar17;
  undefined8 uVar18;
  
  if (1 < param_3) {
    uVar10 = param_3 - 2U >> 1;
    if ((long)param_4 - param_1 >> 6 <= (long)uVar10) {
      lVar5 = (long)param_4 - param_1 >> 5;
      uVar1 = lVar5 + 1;
      uVar2 = lVar5 + 2;
      puVar6 = (undefined8 *)(param_1 + uVar1 * 0x40);
      uVar8 = uVar1;
      if ((long)uVar2 < param_3) {
        uVar8 = uVar2;
        if (*(int *)(puVar6 + 7) <= *(int *)(puVar6 + 0xf)) {
          uVar8 = uVar1;
        }
        puVar6 = puVar6 + (ulong)(*(int *)(puVar6 + 0xf) < *(int *)(puVar6 + 7)) * 8;
      }
      iVar3 = *(int *)(param_4 + 7);
      if (*(int *)(puVar6 + 7) <= iVar3) {
        uVar17 = param_4[1];
        uVar16 = *param_4;
        uVar12 = param_4[2];
        uVar15 = param_4[4];
        uVar14 = param_4[3];
        uVar11 = param_4[5];
        uVar18 = param_4[6];
        *param_4 = 0;
        param_4[1] = 0;
        param_4[2] = 0;
        param_4[3] = 0;
        param_4[4] = 0;
        param_4[5] = 0;
        do {
          puVar7 = puVar6;
          pvVar4 = (void *)*param_4;
          if (pvVar4 != (void *)0x0) {
            param_4[1] = pvVar4;
            operator_delete(pvVar4);
            *param_4 = 0;
            param_4[1] = 0;
            param_4[2] = 0;
          }
          uVar9 = *puVar7;
          param_4[1] = puVar7[1];
          *param_4 = uVar9;
          param_4[2] = puVar7[2];
          *puVar7 = 0;
          puVar7[1] = 0;
          puVar7[2] = 0;
          pvVar4 = (void *)param_4[3];
          if (pvVar4 != (void *)0x0) {
            param_4[4] = pvVar4;
            operator_delete(pvVar4);
            param_4[3] = 0;
            param_4[4] = 0;
            param_4[5] = 0;
          }
          puVar13 = puVar7 + 3;
          uVar9 = *puVar13;
          param_4[4] = puVar7[4];
          param_4[3] = uVar9;
          param_4[5] = puVar7[5];
          uVar9 = puVar7[6];
          *puVar13 = 0;
          puVar7[4] = 0;
          puVar7[5] = 0;
          *(undefined4 *)(param_4 + 7) = *(undefined4 *)(puVar7 + 7);
          param_4[6] = uVar9;
          if ((long)uVar10 < (long)uVar8) break;
          uVar2 = uVar8 << 1 | 1;
          uVar1 = uVar8 * 2 + 2;
          puVar6 = (undefined8 *)(param_1 + uVar2 * 0x40);
          uVar8 = uVar2;
          if ((long)uVar1 < param_3) {
            uVar8 = uVar1;
            if (*(int *)(puVar6 + 7) <= *(int *)(puVar6 + 0xf)) {
              uVar8 = uVar2;
            }
            puVar6 = puVar6 + (ulong)(*(int *)(puVar6 + 0xf) < *(int *)(puVar6 + 7)) * 8;
          }
          param_4 = puVar7;
        } while (*(int *)(puVar6 + 7) <= iVar3);
        pvVar4 = (void *)*puVar7;
        if (pvVar4 != (void *)0x0) {
          puVar7[1] = pvVar4;
          operator_delete(pvVar4);
          *puVar7 = 0;
          puVar7[1] = 0;
          puVar7[2] = 0;
        }
        pvVar4 = (void *)puVar7[3];
        puVar7[2] = uVar12;
        puVar7[1] = uVar17;
        *puVar7 = uVar16;
        if (pvVar4 != (void *)0x0) {
          puVar7[4] = pvVar4;
          operator_delete(pvVar4);
          *puVar13 = 0;
          puVar7[4] = 0;
          puVar7[5] = 0;
        }
        puVar7[5] = uVar11;
        puVar7[6] = uVar18;
        puVar7[4] = uVar15;
        puVar7[3] = uVar14;
        *(int *)(puVar7 + 7) = iVar3;
      }
    }
  }
  return;
}

