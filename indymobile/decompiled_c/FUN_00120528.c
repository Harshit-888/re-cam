
char * FUN_00120528(int param_1)

{
  if (param_1 + 2U < 0x62) {
    return &DAT_00105338 + *(int *)(&DAT_00105338 + (ulong)(param_1 + 2U) * 4);
  }
  return "unknown register";
}

