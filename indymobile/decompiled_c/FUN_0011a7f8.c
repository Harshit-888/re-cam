
void FUN_0011a7f8(undefined8 *param_1)

{
  void *pvVar1;
  undefined8 *puVar2;
  undefined8 *puVar3;
  undefined8 *puVar4;
  undefined8 *puVar5;
  
  if ((*(byte *)(param_1 + 1) & 1) == 0) {
    puVar3 = (undefined8 *)*param_1;
    puVar4 = (undefined8 *)*puVar3;
    if (puVar4 != (undefined8 *)0x0) {
      puVar2 = puVar4;
      puVar5 = (undefined8 *)puVar3[1];
      if ((undefined8 *)puVar3[1] != puVar4) {
        do {
          pvVar1 = (void *)puVar5[-5];
          if (pvVar1 != (void *)0x0) {
            puVar5[-4] = pvVar1;
            operator_delete(pvVar1);
          }
          puVar2 = puVar5 + -8;
          pvVar1 = (void *)*puVar2;
          if (pvVar1 != (void *)0x0) {
            puVar5[-7] = pvVar1;
            operator_delete(pvVar1);
          }
          puVar5 = puVar2;
        } while (puVar2 != puVar4);
        puVar2 = *(undefined8 **)*param_1;
      }
      puVar3[1] = puVar4;
      operator_delete(puVar2);
      return;
    }
  }
  return;
}

