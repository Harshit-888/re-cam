
void FUN_0011cac8(long param_1)

{
  undefined *puVar1;
  undefined1 auStack_4a0 [624];
  undefined1 auStack_230 [528];
  
  FUN_001205e8(auStack_230);
  if (*(long *)(param_1 + 0x10) == 0) {
    FUN_0011c994(auStack_230,auStack_4a0,param_1);
  }
  else {
    FUN_0011cb4c(auStack_230,auStack_4a0,param_1,*(long *)(param_1 + 0x10),
                 *(undefined8 *)(param_1 + 0x18));
  }
  puVar1 = PTR___sF_001250b0;
  fprintf((FILE *)(PTR___sF_001250b0 + 0x130),"libunwind: %s - %s\n","_Unwind_Resume",
          "_Unwind_Resume() can\'t return");
  fflush((FILE *)(puVar1 + 0x130));
                    /* WARNING: Subroutine does not return */
  abort();
}

