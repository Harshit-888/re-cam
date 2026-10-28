
void FUN_0011a550(long param_1,long param_2,undefined8 param_3,long param_4)

{
  undefined8 *puVar1;
  int iVar2;
  void *pvVar3;
  undefined8 *puVar4;
  undefined8 uVar5;
  undefined8 *puVar6;
  undefined8 uVar7;
  undefined8 uVar8;
  ulong uVar9;
  undefined8 *puVar10;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  undefined8 uVar14;
  undefined8 uVar15;
  
  if (1 < param_4) {
    uVar9 = param_4 - 2U >> 1;
    iVar2 = *(int *)(param_2 + -8);
    puVar1 = (undefined8 *)(param_1 + uVar9 * 0x40);
    if (iVar2 < *(int *)(puVar1 + 7)) {
      puVar6 = (undefined8 *)(param_2 + -0x40);
      uVar14 = *(undefined8 *)(param_2 + -0x38);
      uVar13 = *puVar6;
      uVar8 = *(undefined8 *)(param_2 + -0x30);
      uVar12 = *(undefined8 *)(param_2 + -0x20);
      uVar11 = *(undefined8 *)(param_2 + -0x28);
      uVar7 = *(undefined8 *)(param_2 + -0x18);
      uVar15 = *(undefined8 *)(param_2 + -0x10);
      *puVar6 = 0;
      *(undefined8 *)(param_2 + -0x38) = 0;
      *(undefined8 *)(param_2 + -0x30) = 0;
      *(undefined8 *)(param_2 + -0x28) = 0;
      *(undefined8 *)(param_2 + -0x20) = 0;
      *(undefined8 *)(param_2 + -0x18) = 0;
      do {
        puVar4 = puVar1;
        pvVar3 = (void *)*puVar6;
        if (pvVar3 != (void *)0x0) {
          puVar6[1] = pvVar3;
          operator_delete(pvVar3);
          *puVar6 = 0;
          puVar6[1] = 0;
          puVar6[2] = 0;
        }
        uVar5 = *puVar4;
        puVar6[1] = puVar4[1];
        *puVar6 = uVar5;
        puVar6[2] = puVar4[2];
        *puVar4 = 0;
        puVar4[1] = 0;
        puVar4[2] = 0;
        pvVar3 = (void *)puVar6[3];
        if (pvVar3 != (void *)0x0) {
          puVar6[4] = pvVar3;
          operator_delete(pvVar3);
          puVar6[3] = 0;
          puVar6[4] = 0;
          puVar6[5] = 0;
        }
        puVar10 = puVar4 + 3;
        uVar5 = *puVar10;
        puVar6[4] = puVar4[4];
        puVar6[3] = uVar5;
        puVar6[5] = puVar4[5];
        uVar5 = puVar4[6];
        *puVar10 = 0;
        puVar4[4] = 0;
        puVar4[5] = 0;
        *(undefined4 *)(puVar6 + 7) = *(undefined4 *)(puVar4 + 7);
        puVar6[6] = uVar5;
        if (uVar9 == 0) break;
        uVar9 = uVar9 - 1 >> 1;
        puVar1 = (undefined8 *)(param_1 + uVar9 * 0x40);
        puVar6 = puVar4;
      } while (iVar2 < *(int *)(puVar1 + 7));
      pvVar3 = (void *)*puVar4;
      if (pvVar3 != (void *)0x0) {
        puVar4[1] = pvVar3;
        operator_delete(pvVar3);
        *puVar4 = 0;
        puVar4[1] = 0;
        puVar4[2] = 0;
      }
      pvVar3 = (void *)puVar4[3];
      puVar4[2] = uVar8;
      puVar4[1] = uVar14;
      *puVar4 = uVar13;
      if (pvVar3 != (void *)0x0) {
        puVar4[4] = pvVar3;
        operator_delete(pvVar3);
        *puVar10 = 0;
        puVar4[4] = 0;
        puVar4[5] = 0;
      }
      puVar4[5] = uVar7;
      puVar4[6] = uVar15;
      puVar4[4] = uVar12;
      puVar4[3] = uVar11;
      *(int *)(puVar4 + 7) = iVar2;
    }
  }
  return;
}

