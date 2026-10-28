
void FUN_0010bb30(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3,undefined8 *param_4)

{
  long lVar1;
  ulong uVar2;
  ulong uVar3;
  undefined8 uVar4;
  long lVar5;
  undefined8 *puVar6;
  undefined8 uVar7;
  undefined8 uVar8;
  undefined8 uVar9;
  
  lVar1 = tpidr_el0;
  lVar5 = *(long *)(lVar1 + 0x28);
  uVar2 = (*(code *)*param_4)(param_2,param_1);
  uVar3 = (*(code *)*param_4)(param_3,param_2);
  if ((uVar2 & 1) == 0) {
    if ((uVar3 & 1) == 0) {
      uVar4 = 0;
      goto LAB_0010bc7c;
    }
    uVar4 = *param_2;
    *param_2 = *param_3;
    *param_3 = uVar4;
    uVar4 = param_3[1];
    uVar8 = param_2[2];
    uVar7 = param_2[1];
    param_2[2] = param_3[2];
    param_2[1] = uVar4;
    param_3[2] = uVar8;
    param_3[1] = uVar7;
    uVar2 = (*(code *)*param_4)(param_2,param_1);
    if ((uVar2 & 1) == 0) goto LAB_0010bc78;
    uVar4 = *param_1;
    *param_1 = *param_2;
    *param_2 = uVar4;
    uVar4 = param_2[1];
    uVar8 = param_1[2];
    uVar7 = param_1[1];
    param_1[2] = param_2[2];
    param_1[1] = uVar4;
    param_2[2] = uVar8;
    param_2[1] = uVar7;
  }
  else {
    puVar6 = param_1 + 1;
    uVar7 = *param_1;
    if ((uVar3 & 1) != 0) {
      uVar4 = 1;
      *param_1 = *param_3;
      *param_3 = uVar7;
      uVar7 = param_3[1];
      uVar9 = param_1[2];
      uVar8 = *puVar6;
      param_1[2] = param_3[2];
      *puVar6 = uVar7;
      param_3[2] = uVar9;
      param_3[1] = uVar8;
      goto LAB_0010bc7c;
    }
    *param_1 = *param_2;
    *param_2 = uVar7;
    uVar4 = param_2[1];
    uVar8 = param_1[2];
    uVar7 = *puVar6;
    param_1[2] = param_2[2];
    *puVar6 = uVar4;
    param_2[2] = uVar8;
    param_2[1] = uVar7;
    uVar2 = (*(code *)*param_4)(param_3,param_2);
    if ((uVar2 & 1) == 0) {
LAB_0010bc78:
      uVar4 = 1;
      goto LAB_0010bc7c;
    }
    uVar4 = *param_2;
    *param_2 = *param_3;
    *param_3 = uVar4;
    uVar4 = param_3[1];
    uVar8 = param_2[2];
    uVar7 = param_2[1];
    param_2[2] = param_3[2];
    param_2[1] = uVar4;
    param_3[2] = uVar8;
    param_3[1] = uVar7;
  }
  uVar4 = 2;
LAB_0010bc7c:
  if (*(long *)(lVar1 + 0x28) == lVar5) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail(uVar4);
}

