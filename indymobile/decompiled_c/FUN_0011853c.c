
undefined8 *
FUN_0011853c(undefined8 param_1,undefined8 *param_2,undefined8 *param_3,undefined8 *param_4)

{
  void *__src;
  ulong __n;
  long lVar1;
  void *__dest;
  undefined8 uVar2;
  undefined8 *local_88;
  undefined8 *local_80;
  undefined8 local_78;
  undefined8 **ppuStack_70;
  undefined8 **local_68;
  undefined1 local_60;
  long local_58;
  
  lVar1 = tpidr_el0;
  local_58 = *(long *)(lVar1 + 0x28);
  ppuStack_70 = &local_88;
  local_68 = &local_80;
  local_60 = 0;
  local_88 = param_4;
  local_78 = param_1;
  do {
    local_80 = param_4;
    if (param_2 == param_3) {
      if (*(long *)(lVar1 + 0x28) == local_58) {
        return param_4;
      }
LAB_00118670:
                    /* WARNING: Subroutine does not return */
      __stack_chk_fail();
    }
    *param_4 = 0;
    param_4[1] = 0;
    param_4[2] = 0;
    __src = (void *)*param_2;
    __n = param_2[1] - (long)__src;
    if (__n != 0) {
      if ((long)__n < 0) {
        if (*(long *)(lVar1 + 0x28) == local_58) {
          uVar2 = FUN_00117b34(param_4);
          FUN_00118450(&local_78);
          if (*(long *)(lVar1 + 0x28) == local_58) {
                    /* WARNING: Subroutine does not return */
            FUN_0011cac8(uVar2);
          }
        }
        goto LAB_00118670;
      }
      __dest = operator_new(__n);
      *param_4 = __dest;
      param_4[1] = __dest;
      param_4[2] = (long)__dest + __n;
      memcpy(__dest,__src,__n);
      param_4[1] = (long)__dest + __n;
    }
    param_2 = param_2 + 3;
    param_4 = param_4 + 3;
  } while( true );
}

