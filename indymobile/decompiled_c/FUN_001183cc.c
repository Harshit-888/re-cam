
void FUN_001183cc(undefined8 *param_1)

{
  undefined8 *puVar1;
  void *pvVar2;
  undefined8 *puVar3;
  undefined8 *puVar4;
  
  puVar1 = (undefined8 *)param_1[1];
  puVar3 = (undefined8 *)param_1[2];
  while (puVar3 != puVar1) {
    while( true ) {
      puVar4 = puVar3 + -3;
      pvVar2 = (void *)*puVar4;
      param_1[2] = puVar4;
      if (pvVar2 != (void *)0x0) break;
      puVar3 = puVar4;
      if (puVar4 == puVar1) goto LAB_0011841c;
    }
    puVar3[-2] = pvVar2;
    operator_delete(pvVar2);
    puVar3 = (undefined8 *)param_1[2];
  }
LAB_0011841c:
  if ((void *)*param_1 != (void *)0x0) {
    operator_delete((void *)*param_1);
    return;
  }
  return;
}

