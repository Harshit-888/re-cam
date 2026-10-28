
/* WARNING: Removing unreachable block (ram,0x0011a474) */
/* WARNING: Removing unreachable block (ram,0x0011a480) */

void FUN_0011a1f4(undefined8 *param_1,long param_2,undefined8 param_3,long param_4)

{
  ulong uVar1;
  ulong uVar2;
  undefined8 uVar3;
  undefined8 uVar4;
  undefined4 uVar5;
  long lVar6;
  undefined8 *puVar7;
  long lVar8;
  long lVar9;
  long lVar10;
  long lVar11;
  void *pvVar12;
  undefined8 uVar13;
  long lVar14;
  undefined8 uVar15;
  undefined8 *puVar16;
  undefined8 *puVar17;
  ulong uVar18;
  undefined8 *puVar19;
  undefined8 uVar20;
  undefined8 uVar21;
  undefined8 uVar22;
  undefined8 uVar23;
  
  lVar6 = tpidr_el0;
  lVar14 = *(long *)(lVar6 + 0x28);
  if (1 < param_4) {
    uVar15 = param_1[2];
    uVar22 = param_1[1];
    uVar20 = *param_1;
    uVar5 = *(undefined4 *)(param_1 + 7);
    uVar3 = param_1[5];
    uVar4 = param_1[6];
    uVar23 = param_1[4];
    uVar21 = param_1[3];
    uVar18 = 0;
    *param_1 = 0;
    param_1[1] = 0;
    param_1[2] = 0;
    param_1[3] = 0;
    param_1[4] = 0;
    param_1[5] = 0;
    puVar16 = param_1;
    do {
      uVar2 = uVar18 << 1 | 1;
      uVar1 = uVar18 * 2 + 2;
      lVar8 = uVar18 * 8;
      if ((long)uVar1 < param_4) {
        lVar9 = uVar18 * 8;
        lVar10 = uVar18 * 8;
        lVar11 = uVar18 * 8;
        uVar18 = uVar1;
        puVar7 = puVar16 + lVar11 + 0x10;
        if (*(int *)(puVar16 + lVar9 + 0xf) <= *(int *)(puVar16 + lVar10 + 0x17)) {
          uVar18 = uVar2;
          puVar7 = puVar16 + lVar8 + 8;
        }
        pvVar12 = (void *)*puVar16;
      }
      else {
        pvVar12 = (void *)*puVar16;
        uVar18 = uVar2;
        puVar7 = puVar16 + lVar8 + 8;
      }
      if (pvVar12 != (void *)0x0) {
        puVar16[1] = pvVar12;
        operator_delete(pvVar12);
        *puVar16 = 0;
        puVar16[1] = 0;
        puVar16[2] = 0;
      }
      uVar13 = *puVar7;
      puVar16[1] = puVar7[1];
      *puVar16 = uVar13;
      puVar16[2] = puVar7[2];
      *puVar7 = 0;
      puVar7[1] = 0;
      puVar7[2] = 0;
      pvVar12 = (void *)puVar16[3];
      if (pvVar12 != (void *)0x0) {
        puVar16[4] = pvVar12;
        operator_delete(pvVar12);
        puVar16[3] = 0;
        puVar16[4] = 0;
        puVar16[5] = 0;
      }
      puVar19 = puVar7 + 3;
      uVar13 = *puVar19;
      puVar16[4] = puVar7[4];
      puVar16[3] = uVar13;
      puVar16[5] = puVar7[5];
      puVar17 = puVar7 + 6;
      uVar13 = *puVar17;
      *puVar19 = 0;
      puVar7[5] = 0;
      puVar7[4] = 0;
      *(undefined4 *)(puVar16 + 7) = *(undefined4 *)(puVar7 + 7);
      puVar16[6] = uVar13;
      puVar16 = puVar7;
    } while ((long)uVar18 <= (long)(param_4 - 2U >> 1));
    puVar16 = (undefined8 *)(param_2 + -0x40);
    pvVar12 = (void *)*puVar7;
    if (puVar7 == puVar16) {
      if (pvVar12 != (void *)0x0) {
        puVar7[1] = pvVar12;
        operator_delete(pvVar12);
        *puVar7 = 0;
        puVar7[1] = 0;
        puVar7[2] = 0;
      }
      pvVar12 = (void *)puVar7[3];
      puVar7[1] = uVar22;
      *puVar7 = uVar20;
      puVar7[2] = uVar15;
      if (pvVar12 != (void *)0x0) {
        puVar7[4] = pvVar12;
        operator_delete(pvVar12);
        *puVar19 = 0;
        puVar7[4] = 0;
        puVar7[5] = 0;
      }
      puVar7[4] = uVar23;
      puVar7[3] = uVar21;
      puVar7[5] = uVar3;
      *(undefined4 *)(puVar7 + 7) = uVar5;
      *puVar17 = uVar4;
    }
    else {
      if (pvVar12 != (void *)0x0) {
        puVar7[1] = pvVar12;
        operator_delete(pvVar12);
        *puVar7 = 0;
        puVar7[1] = 0;
        puVar7[2] = 0;
      }
      uVar13 = *(undefined8 *)(param_2 + -0x40);
      puVar7[1] = *(undefined8 *)(param_2 + -0x38);
      *puVar7 = uVar13;
      puVar7[2] = *(undefined8 *)(param_2 + -0x30);
      *puVar16 = 0;
      *(undefined8 *)(param_2 + -0x38) = 0;
      *(undefined8 *)(param_2 + -0x30) = 0;
      pvVar12 = (void *)puVar7[3];
      if (pvVar12 != (void *)0x0) {
        puVar7[4] = pvVar12;
        operator_delete(pvVar12);
        *puVar19 = 0;
        puVar7[4] = 0;
        puVar7[5] = 0;
      }
      puVar19 = (undefined8 *)(param_2 + -0x28);
      uVar13 = *puVar19;
      puVar7[4] = *(undefined8 *)(param_2 + -0x20);
      puVar7[3] = uVar13;
      puVar7[5] = *(undefined8 *)(param_2 + -0x18);
      uVar13 = *(undefined8 *)(param_2 + -0x10);
      *puVar19 = 0;
      *(undefined8 *)(param_2 + -0x18) = 0;
      *(undefined8 *)(param_2 + -0x20) = 0;
      *(undefined4 *)(puVar7 + 7) = *(undefined4 *)(param_2 + -8);
      *puVar17 = uVar13;
      pvVar12 = *(void **)(param_2 + -0x40);
      if (pvVar12 != (void *)0x0) {
        *(void **)(param_2 + -0x38) = pvVar12;
        operator_delete(pvVar12);
        *puVar16 = 0;
        *(undefined8 *)(param_2 + -0x38) = 0;
        *(undefined8 *)(param_2 + -0x30) = 0;
      }
      pvVar12 = *(void **)(param_2 + -0x28);
      *(undefined8 *)(param_2 + -0x38) = uVar22;
      *(undefined8 *)(param_2 + -0x40) = uVar20;
      *(undefined8 *)(param_2 + -0x30) = uVar15;
      if (pvVar12 != (void *)0x0) {
        *(void **)(param_2 + -0x20) = pvVar12;
        operator_delete(pvVar12);
        *puVar19 = 0;
        *(undefined8 *)(param_2 + -0x20) = 0;
        *(undefined8 *)(param_2 + -0x18) = 0;
      }
      *(undefined8 *)(param_2 + -0x18) = uVar3;
      *(undefined8 *)(param_2 + -0x20) = uVar23;
      *(undefined8 *)(param_2 + -0x28) = uVar21;
      *(undefined8 *)(param_2 + -0x10) = uVar4;
      *(undefined4 *)(param_2 + -8) = uVar5;
      FUN_0011a550(param_1,puVar7 + 8,param_3,(long)(puVar7 + 8) - (long)param_1 >> 6);
    }
  }
  if (*(long *)(lVar6 + 0x28) == lVar14) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

