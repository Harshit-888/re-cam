
void FUN_00118ca4(undefined8 *param_1)

{
  long lVar1;
  void *pvVar2;
  long lVar3;
  
  lVar1 = param_1[1];
  lVar3 = param_1[2];
  while (lVar3 != lVar1) {
    pvVar2 = *(void **)(lVar3 + -0x28);
    param_1[2] = (undefined8 *)(lVar3 + -0x40);
    if (pvVar2 != (void *)0x0) {
      *(void **)(lVar3 + -0x20) = pvVar2;
      operator_delete(pvVar2);
    }
    pvVar2 = *(void **)(lVar3 + -0x40);
    if (pvVar2 != (void *)0x0) {
      *(void **)(lVar3 + -0x38) = pvVar2;
      operator_delete(pvVar2);
    }
    lVar3 = param_1[2];
  }
  if ((void *)*param_1 != (void *)0x0) {
    operator_delete((void *)*param_1);
    return;
  }
  return;
}

