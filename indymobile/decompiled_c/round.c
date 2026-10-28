
/* MathUtil::round(double) */

undefined1  [16] MathUtil::round(double param_1)

{
  double dVar1;
  undefined1 auVar2 [16];
  
  dVar1 = param_1 + 0.5;
  if (param_1 <= 0.0) {
    dVar1 = param_1 + -0.5;
  }
  auVar2._0_8_ = (ulong)dVar1;
  auVar2._8_8_ = 0;
  return auVar2;
}

