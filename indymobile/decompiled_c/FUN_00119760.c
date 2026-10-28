
void FUN_00119760(undefined8 *param_1,undefined8 *param_2)

{
  int *piVar1;
  int iVar2;
  long lVar3;
  void *pvVar4;
  undefined8 uVar5;
  undefined8 uVar6;
  undefined8 uVar7;
  undefined8 *puVar8;
  undefined8 *puVar9;
  undefined8 *puVar10;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  undefined8 uVar14;
  undefined8 uVar15;
  undefined8 *local_78;
  undefined8 *local_70;
  long local_68;
  
  lVar3 = tpidr_el0;
  local_68 = *(long *)(lVar3 + 0x28);
  uVar14 = param_1[1];
  uVar13 = *param_1;
  *param_1 = 0;
  param_1[1] = 0;
  uVar7 = param_1[2];
  param_1[2] = 0;
  puVar9 = param_1 + 3;
  uVar12 = param_1[4];
  uVar11 = *puVar9;
  *puVar9 = 0;
  uVar6 = param_1[5];
  iVar2 = *(int *)(param_1 + 7);
  param_1[4] = 0;
  param_1[5] = 0;
  local_70 = param_1;
  puVar8 = param_1;
  if (*(int *)(param_2 + -1) < iVar2) {
    do {
      piVar1 = (int *)(local_70 + 0xf);
      local_70 = local_70 + 8;
    } while (iVar2 <= *piVar1);
  }
  else {
    do {
      local_70 = puVar8 + 8;
      if (param_2 <= local_70) break;
      piVar1 = (int *)(puVar8 + 0xf);
      puVar8 = local_70;
    } while (iVar2 <= *piVar1);
  }
  uVar15 = param_1[6];
  if (local_70 < param_2) {
    do {
      piVar1 = (int *)(param_2 + -1);
      param_2 = param_2 + -8;
    } while (*piVar1 < iVar2);
  }
  puVar8 = local_70;
  local_78 = param_2;
  if (local_70 < param_2) {
    do {
      FUN_001190dc(&local_70,&local_78);
      do {
        piVar1 = (int *)(local_70 + 0xf);
        local_70 = local_70 + 8;
      } while (iVar2 <= *piVar1);
      do {
        piVar1 = (int *)(local_78 + -1);
        local_78 = local_78 + -8;
      } while (*piVar1 < iVar2);
      puVar8 = local_70;
    } while (local_70 < local_78);
  }
  puVar10 = puVar8 + -8;
  local_70 = puVar8;
  if (puVar10 != param_1) {
    pvVar4 = (void *)*param_1;
    if (pvVar4 != (void *)0x0) {
      param_1[1] = pvVar4;
      operator_delete(pvVar4);
      *param_1 = 0;
      param_1[1] = 0;
      param_1[2] = 0;
    }
    uVar5 = puVar8[-8];
    param_1[1] = puVar8[-7];
    *param_1 = uVar5;
    param_1[2] = puVar8[-6];
    *puVar10 = 0;
    puVar8[-7] = 0;
    puVar8[-6] = 0;
    pvVar4 = (void *)param_1[3];
    if (pvVar4 != (void *)0x0) {
      param_1[4] = pvVar4;
      operator_delete(pvVar4);
      *puVar9 = 0;
      param_1[4] = 0;
      param_1[5] = 0;
    }
    uVar5 = puVar8[-5];
    param_1[4] = puVar8[-4];
    param_1[3] = uVar5;
    param_1[5] = puVar8[-3];
    uVar5 = puVar8[-2];
    puVar8[-5] = 0;
    puVar8[-4] = 0;
    puVar8[-3] = 0;
    *(undefined4 *)(param_1 + 7) = *(undefined4 *)(puVar8 + -1);
    param_1[6] = uVar5;
  }
  pvVar4 = (void *)*puVar10;
  if (pvVar4 != (void *)0x0) {
    puVar8[-7] = pvVar4;
    operator_delete(pvVar4);
  }
  pvVar4 = (void *)puVar8[-5];
  puVar8[-6] = uVar7;
  puVar8[-7] = uVar14;
  puVar8[-8] = uVar13;
  if (pvVar4 != (void *)0x0) {
    puVar8[-4] = pvVar4;
    operator_delete(pvVar4);
  }
  puVar8[-3] = uVar6;
  puVar8[-2] = uVar15;
  puVar8[-4] = uVar12;
  puVar8[-5] = uVar11;
  *(int *)(puVar8 + -1) = iVar2;
  if (*(long *)(lVar3 + 0x28) != local_68) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail(local_70);
  }
  return;
}

