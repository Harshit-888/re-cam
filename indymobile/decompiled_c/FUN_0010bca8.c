
undefined8 * FUN_0010bca8(undefined8 *param_1,undefined8 *param_2,undefined8 *param_3)

{
  long lVar1;
  ulong uVar2;
  undefined8 *puVar3;
  undefined8 uVar4;
  undefined8 local_80;
  undefined8 uStack_78;
  undefined8 local_70;
  undefined8 local_60;
  undefined8 uStack_58;
  long local_48;
  
  lVar1 = tpidr_el0;
  local_48 = *(long *)(lVar1 + 0x28);
  uStack_78 = param_1[1];
  local_80 = *param_1;
  local_70 = param_1[2];
  uVar2 = (*(code *)*param_3)(&local_80,param_2 + -3);
  puVar3 = param_1;
  if ((uVar2 & 1) == 0) {
    do {
      puVar3 = puVar3 + 3;
      if (param_2 <= puVar3) break;
      uVar2 = (*(code *)*param_3)(&local_80,puVar3);
    } while ((uVar2 & 1) == 0);
  }
  else {
    do {
      puVar3 = puVar3 + 3;
      uVar2 = (*(code *)*param_3)(&local_80,puVar3);
    } while ((uVar2 & 1) == 0);
  }
  if (puVar3 < param_2) {
    do {
      param_2 = param_2 + -3;
      uVar2 = (*(code *)*param_3)(&local_80,param_2);
    } while ((uVar2 & 1) != 0);
  }
  while (puVar3 < param_2) {
    uVar4 = *puVar3;
    *puVar3 = *param_2;
    *param_2 = uVar4;
    uStack_58 = puVar3[2];
    local_60 = puVar3[1];
    uVar4 = param_2[1];
    puVar3[2] = param_2[2];
    puVar3[1] = uVar4;
    param_2[2] = uStack_58;
    param_2[1] = local_60;
    do {
      puVar3 = puVar3 + 3;
      uVar2 = (*(code *)*param_3)(&local_80,puVar3);
    } while ((uVar2 & 1) == 0);
    do {
      param_2 = param_2 + -3;
      uVar2 = (*(code *)*param_3)(&local_80,param_2);
    } while ((uVar2 & 1) != 0);
  }
  if (puVar3 + -3 != param_1) {
    *param_1 = puVar3[-3];
    uVar4 = puVar3[-2];
    param_1[2] = puVar3[-1];
    param_1[1] = uVar4;
  }
  puVar3[-3] = local_80;
  puVar3[-1] = local_70;
  puVar3[-2] = uStack_78;
  if (*(long *)(lVar1 + 0x28) != local_48) {
                    /* WARNING: Subroutine does not return */
    __stack_chk_fail();
  }
  return puVar3;
}

