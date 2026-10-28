
void FUN_00119620(undefined8 *param_1,undefined8 *param_2)

{
  undefined4 uVar1;
  long lVar2;
  void *pvVar3;
  long lVar4;
  undefined8 uVar5;
  undefined8 uVar6;
  undefined8 uVar7;
  undefined8 uVar8;
  undefined8 *puVar9;
  undefined8 uVar10;
  undefined8 uVar11;
  undefined8 uVar12;
  undefined8 uVar13;
  
  lVar2 = tpidr_el0;
  lVar4 = *(long *)(lVar2 + 0x28);
  param_1 = (undefined8 *)*param_1;
  param_2 = (undefined8 *)*param_2;
  uVar13 = param_1[1];
  uVar12 = *param_1;
  *param_1 = 0;
  param_1[1] = 0;
  puVar9 = param_1 + 3;
  uVar11 = param_1[4];
  uVar10 = *puVar9;
  uVar8 = param_1[2];
  param_1[2] = 0;
  uVar7 = param_1[5];
  *puVar9 = 0;
  uVar1 = *(undefined4 *)(param_1 + 7);
  param_1[4] = 0;
  param_1[5] = 0;
  uVar5 = *param_2;
  param_1[1] = param_2[1];
  *param_1 = uVar5;
  param_1[2] = param_2[2];
  uVar5 = param_1[6];
  *param_2 = 0;
  param_2[1] = 0;
  param_2[2] = 0;
  pvVar3 = (void *)*puVar9;
  if (pvVar3 != (void *)0x0) {
    param_1[4] = pvVar3;
    operator_delete(pvVar3);
    *puVar9 = 0;
    param_1[4] = 0;
    param_1[5] = 0;
  }
  uVar6 = param_2[3];
  param_1[4] = param_2[4];
  param_1[3] = uVar6;
  param_1[5] = param_2[5];
  uVar6 = param_2[6];
  param_2[3] = 0;
  param_2[4] = 0;
  param_2[5] = 0;
  *(undefined4 *)(param_1 + 7) = *(undefined4 *)(param_2 + 7);
  param_1[6] = uVar6;
  pvVar3 = (void *)*param_2;
  if (pvVar3 != (void *)0x0) {
    param_2[1] = pvVar3;
    operator_delete(pvVar3);
  }
  pvVar3 = (void *)param_2[3];
  param_2[2] = uVar8;
  param_2[1] = uVar13;
  *param_2 = uVar12;
  if (pvVar3 != (void *)0x0) {
    param_2[4] = pvVar3;
    operator_delete(pvVar3);
  }
  param_2[5] = uVar7;
  param_2[4] = uVar11;
  param_2[3] = uVar10;
  param_2[6] = uVar5;
  *(undefined4 *)(param_2 + 7) = uVar1;
  if (*(long *)(lVar2 + 0x28) == lVar4) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

