
void FUN_00118450(long param_1)

{
  undefined8 *puVar1;
  void *pvVar2;
  undefined8 *puVar3;
  undefined8 *puVar4;
  
  if ((*(byte *)(param_1 + 0x18) & 1) == 0) {
    puVar3 = (undefined8 *)**(undefined8 **)(param_1 + 8);
    puVar4 = (undefined8 *)**(long **)(param_1 + 0x10);
    while (puVar1 = puVar4, puVar1 != puVar3) {
      puVar4 = puVar1 + -3;
      pvVar2 = (void *)*puVar4;
      if (pvVar2 != (void *)0x0) {
        puVar1[-2] = pvVar2;
        operator_delete(pvVar2);
      }
    }
    return;
  }
  return;
}

