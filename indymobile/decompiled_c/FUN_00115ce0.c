
void FUN_00115ce0(undefined8 *param_1)

{
  void *pvVar1;
  undefined8 *puVar2;
  undefined8 *puVar3;
  undefined8 *puVar4;
  
  puVar3 = (undefined8 *)*param_1;
  if (puVar3 == (undefined8 *)0x0) {
    return;
  }
  puVar2 = puVar3;
  puVar4 = (undefined8 *)param_1[1];
  if ((undefined8 *)param_1[1] != puVar3) {
    do {
      pvVar1 = (void *)puVar4[-5];
      if (pvVar1 != (void *)0x0) {
        puVar4[-4] = pvVar1;
        operator_delete(pvVar1);
      }
      puVar2 = puVar4 + -8;
      pvVar1 = (void *)*puVar2;
      if (pvVar1 != (void *)0x0) {
        puVar4[-7] = pvVar1;
        operator_delete(pvVar1);
      }
      puVar4 = puVar2;
    } while (puVar2 != puVar3);
    puVar2 = (undefined8 *)*param_1;
  }
  param_1[1] = puVar3;
  operator_delete(puVar2);
  return;
}

