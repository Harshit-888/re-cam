
void FUN_00119e3c(long param_1,long param_2,long param_3,long param_4)

{
  long lVar1;
  long local_68;
  long local_60;
  long local_58;
  long local_50;
  long local_48;
  
  lVar1 = tpidr_el0;
  local_48 = *(long *)(lVar1 + 0x28);
  local_68 = param_4;
  local_60 = param_3;
  local_58 = param_2;
  local_50 = param_1;
  FUN_0011953c();
  if (*(int *)(param_3 + 0x38) < *(int *)(param_4 + 0x38)) {
    FUN_001190dc(&local_60,&local_68);
    if (*(int *)(param_2 + 0x38) < *(int *)(local_60 + 0x38)) {
      FUN_001190dc(&local_58,&local_60);
      if (*(int *)(param_1 + 0x38) < *(int *)(local_58 + 0x38)) {
        FUN_001190dc(&local_50,&local_58);
      }
    }
  }
  if (*(long *)(lVar1 + 0x28) == local_48) {
    return;
  }
                    /* WARNING: Subroutine does not return */
  __stack_chk_fail();
}

