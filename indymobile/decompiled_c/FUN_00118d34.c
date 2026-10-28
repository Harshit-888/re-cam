
void FUN_00118d34(long param_1)

{
  undefined8 *puVar1;
  void *pvVar2;
  undefined8 *puVar3;
  undefined8 *puVar4;
  
  if ((*(byte *)(param_1 + 0x18) & 1) != 0) {
    return;
  }
  puVar3 = (undefined8 *)**(undefined8 **)(param_1 + 8);
  puVar4 = (undefined8 *)**(long **)(param_1 + 0x10);
  while (puVar1 = puVar4, puVar1 != puVar3) {
    pvVar2 = (void *)puVar1[-5];
    if (pvVar2 != (void *)0x0) {
      puVar1[-4] = pvVar2;
      operator_delete(pvVar2);
    }
    puVar4 = puVar1 + -8;
    pvVar2 = (void *)*puVar4;
    if (pvVar2 != (void *)0x0) {
      puVar1[-7] = pvVar2;
      operator_delete(pvVar2);
    }
  }
  return;
}

