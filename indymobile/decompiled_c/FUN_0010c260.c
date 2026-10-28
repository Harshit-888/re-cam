
void FUN_0010c260(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3,undefined8 *param_4,
                 undefined8 *param_5,undefined8 *param_6)

{
  long lVar1;
  ulong uVar2;
  long lVar3;
  undefined8 uVar4;
  undefined8 uVar5;
  undefined8 uVar6;
  
  lVar1 = tpidr_el0;
  lVar3 = *(long *)(lVar1 + 0x28);
  FUN_0010bb30();
  uVar2 = (*(code *)*param_6)(param_4,param_3);
  if ((uVar2 & 1) != 0) {
    uVar4 = *param_3;
    *param_3 = *param_4;
    *param_4 = uVar4;
    uVar4 = param_4[1];
    uVar6 = param_3[2];
    uVar5 = param_3[1];
    param_3[2] = param_4[2];
    param_3[1] = uVar4;
    param_4[2] = uVar6;
    param_4[1] = uVar5;
    uVar2 = (*(code *)*param_6)(param_3,param_2);
    if ((uVar2 & 1) != 0) {
      uVar4 = *param_2;
      *param_2 = *param_3;
      *param_3 = uVar4;
      uVar4 = param_3[1];
      uVar6 = param_2[2];
      uVar5 = param_2[1];
      param_2[2] = param_3[2];
      param_2[1] = uVar4;
      param_3[2] = uVar6;
      param_3[1] = uVar5;
      uVar2 = (*(code *)*param_6)(param_2,param_1);
      if ((uVar2 & 1) != 0) {
        uVar4 = *param_1;
        *param_1 = *param_2;
        *param_2 = uVar4;
        uVar4 = param_2[1];
        uVar6 = param_1[2];
        uVar5 = param_1[1];
        param_1[2] = param_2[2];
        param_1[1] = uVar4;
        param_2[2] = uVar6;
        param_2[1] = uVar5;
      }
    }
  }
  uVar2 = (*(code *)*param_6)(param_5,param_4);
  if ((uVar2 & 1) != 0) {
    uVar4 = *param_4;
    *param_4 = *param_5;
    *param_5 = uVar4;
    uVar4 = param_5[1];
    uVar6 = param_4[2];
    uVar5 = param_4[1];
    param_4[2] = param_5[2];
    param_4[1] = uVar4;
    param_5[2] = uVar6;
    param_5[1] = uVar5;
    uVar2 = (*(code *)*param_6)(param_4,param_3);
    if ((uVar2 & 1) != 0) {
      uVar4 = *param_3;
      *param_3 = *param_4;
      *param_4 = uVar4;
      uVar4 = param_4[1];
      uVar6 = param_3[2];
      uVar5 = param_3[1];
      param_3[2] = param_4[2];
      param_3[1] = uVar4;
      param_4[2] = uVar6;
      param_4[1] = uVar5;
      uVar2 = (*(code *)*param_6)(param_3,param_2);
      if ((uVar2 & 1) != 0) {
        uVar4 = *param_2;
        *param_2 = *param_3;
        *param_3 = uVar4;
        uVar4 = param_3[1];
        uVar6 = param_2[2];
        uVar5 = param_2[1];
        param_2[2] = param_3[2];
        param_2[1] = uVar4;
        param_3[2] = uVar6;
        param_3[1] = uVar5;
        uVar2 = (*(code *)*param_6)(param_2,param_1);
        if ((uVar2 & 1) != 0) {
          uVar4 = *param_1;
          *param_1 = *param_2;
          *param_2 = uVar4;
          uVar4 = param_2[1];
          uVar6 = param_1[2];
          uVar5 = param_1[1];
          param_1[2] = param_2[2];
          param_1[1] = uVar4;
          param_2[2] = uVar6;
          param_2[1] = uVar5;
        }
      }
    }
  }
  if (*(long *)(lVar1 + 0x28) == lVar3) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

