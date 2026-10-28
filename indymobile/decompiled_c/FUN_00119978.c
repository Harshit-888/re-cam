
undefined8 * FUN_00119978(undefined8 *param_1,ulong param_2)

{
  int *piVar1;
  long lVar2;
  ulong uVar3;
  int iVar4;
  undefined4 uVar5;
  long lVar6;
  long lVar7;
  void *pvVar8;
  undefined8 extraout_x0;
  long lVar9;
  undefined8 uVar10;
  undefined8 uVar11;
  undefined8 *puVar12;
  undefined8 uVar13;
  ulong uVar14;
  undefined8 *puVar15;
  undefined8 uVar16;
  undefined8 uVar17;
  undefined8 uVar18;
  undefined8 uVar19;
  undefined8 uVar20;
  ulong local_88;
  ulong local_80;
  long local_78;
  
  lVar6 = tpidr_el0;
  local_78 = *(long *)(lVar6 + 0x28);
  uVar19 = param_1[1];
  uVar18 = *param_1;
  uVar13 = param_1[2];
  uVar10 = param_1[5];
  puVar15 = param_1 + 3;
  uVar17 = param_1[4];
  uVar16 = *puVar15;
  iVar4 = *(int *)(param_1 + 7);
  uVar20 = param_1[6];
  *param_1 = 0;
  param_1[1] = 0;
  *puVar15 = 0;
  param_1[2] = 0;
  param_1[4] = 0;
  param_1[5] = 0;
  lVar9 = 0;
  do {
    lVar2 = lVar9 + 0x40;
    lVar7 = lVar9 + 0x78;
    lVar9 = lVar2;
  } while (iVar4 < *(int *)((long)param_1 + lVar7));
  uVar3 = (long)param_1 + lVar2;
  if (lVar2 == 0x40) {
    do {
      if (param_2 <= uVar3) break;
      piVar1 = (int *)(param_2 - 8);
      param_2 = param_2 - 0x40;
    } while (*piVar1 <= iVar4);
  }
  else {
    do {
      piVar1 = (int *)(param_2 - 8);
      param_2 = param_2 - 0x40;
    } while (*piVar1 <= iVar4);
  }
  uVar14 = uVar3;
  local_88 = param_2;
  local_80 = uVar3;
  if (uVar3 < param_2) {
    do {
      FUN_001190dc(&local_80,&local_88);
      do {
        piVar1 = (int *)(local_80 + 0x78);
        local_80 = local_80 + 0x40;
      } while (iVar4 < *piVar1);
      do {
        piVar1 = (int *)(local_88 - 8);
        local_88 = local_88 - 0x40;
      } while (*piVar1 <= iVar4);
      uVar14 = local_80;
    } while (local_80 < local_88);
  }
  puVar12 = (undefined8 *)(uVar14 - 0x40);
  local_80 = uVar14;
  if (puVar12 != param_1) {
    pvVar8 = (void *)*param_1;
    if (pvVar8 != (void *)0x0) {
      param_1[1] = pvVar8;
      operator_delete(pvVar8);
      *param_1 = 0;
      param_1[1] = 0;
      param_1[2] = 0;
    }
    uVar11 = *(undefined8 *)(uVar14 - 0x40);
    param_1[1] = *(undefined8 *)(uVar14 - 0x38);
    *param_1 = uVar11;
    param_1[2] = *(undefined8 *)(uVar14 - 0x30);
    *puVar12 = 0;
    *(undefined8 *)(uVar14 - 0x38) = 0;
    *(undefined8 *)(uVar14 - 0x30) = 0;
    pvVar8 = (void *)param_1[3];
    if (pvVar8 != (void *)0x0) {
      param_1[4] = pvVar8;
      operator_delete(pvVar8);
      *puVar15 = 0;
      param_1[4] = 0;
      param_1[5] = 0;
    }
    uVar11 = *(undefined8 *)(uVar14 - 0x28);
    param_1[4] = *(undefined8 *)(uVar14 - 0x20);
    param_1[3] = uVar11;
    param_1[5] = *(undefined8 *)(uVar14 - 0x18);
    uVar5 = *(undefined4 *)(uVar14 - 8);
    uVar11 = *(undefined8 *)(uVar14 - 0x10);
    *(undefined8 *)(uVar14 - 0x28) = 0;
    *(undefined8 *)(uVar14 - 0x20) = 0;
    *(undefined8 *)(uVar14 - 0x18) = 0;
    *(undefined4 *)(param_1 + 7) = uVar5;
    param_1[6] = uVar11;
  }
  pvVar8 = (void *)*puVar12;
  if (pvVar8 != (void *)0x0) {
    *(void **)(uVar14 - 0x38) = pvVar8;
    operator_delete(pvVar8);
  }
  pvVar8 = *(void **)(uVar14 - 0x28);
  *(undefined8 *)(uVar14 - 0x30) = uVar13;
  *(undefined8 *)(uVar14 - 0x38) = uVar19;
  *(undefined8 *)(uVar14 - 0x40) = uVar18;
  uVar13 = 0;
  if (pvVar8 != (void *)0x0) {
    *(void **)(uVar14 - 0x20) = pvVar8;
    operator_delete(pvVar8);
    uVar13 = extraout_x0;
  }
  *(undefined8 *)(uVar14 - 0x10) = uVar20;
  *(undefined8 *)(uVar14 - 0x20) = uVar17;
  *(undefined8 *)(uVar14 - 0x28) = uVar16;
  *(undefined8 *)(uVar14 - 0x18) = uVar10;
  *(int *)(uVar14 - 8) = iVar4;
  if (*(long *)(lVar6 + 0x28) != local_78) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail(uVar13,param_2 <= uVar3);
  }
  return puVar12;
}

