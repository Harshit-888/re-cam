
/* angle(cv::Point_<int>, cv::Point_<int>, cv::Point_<int>) */

undefined1  [16] angle(undefined8 param_1,undefined8 param_2,undefined8 param_3)

{
  int iVar1;
  double dVar2;
  undefined1 auVar3 [16];
  double dVar4;
  double dVar5;
  double dVar6;
  
  iVar1 = (int)((ulong)param_3 >> 0x20);
  dVar2 = (double)((int)((ulong)param_1 >> 0x20) - iVar1);
  dVar4 = (double)((int)((ulong)param_2 >> 0x20) - iVar1);
  dVar5 = (double)((int)param_1 - (int)param_3);
  dVar6 = (double)((int)param_2 - (int)param_3);
  auVar3._0_8_ = (dVar2 * dVar4 + dVar6 * dVar5) /
                 SQRT(DAT_00104510 +
                      (dVar4 * dVar4 + dVar6 * dVar6) * (dVar2 * dVar2 + dVar5 * dVar5));
  auVar3._8_8_ = 0;
  return auVar3;
}

