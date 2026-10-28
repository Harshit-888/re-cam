
void FUN_001184b0(undefined8 *param_1)

{
  void *pvVar1;
  undefined8 *puVar2;
  undefined8 *puVar3;
  undefined8 *puVar4;
  undefined8 *puVar5;
  
  if ((*(byte *)(param_1 + 1) & 1) == 0) {
    puVar4 = (undefined8 *)*param_1;
    puVar5 = (undefined8 *)*puVar4;
    if (puVar5 != (undefined8 *)0x0) {
      puVar3 = (undefined8 *)puVar4[1];
      puVar2 = puVar5;
      if (puVar3 != puVar5) {
        do {
          puVar2 = puVar3 + -3;
          pvVar1 = (void *)*puVar2;
          if (pvVar1 != (void *)0x0) {
            puVar3[-2] = pvVar1;
            operator_delete(pvVar1);
          }
          puVar3 = puVar2;
        } while (puVar2 != puVar5);
        puVar2 = *(undefined8 **)*param_1;
      }
      puVar4[1] = puVar5;
      operator_delete(puVar2);
      return;
    }
  }
  return;
}

