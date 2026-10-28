
/* angle2(cv::Point_<int>, cv::Point_<int>, cv::Point_<int>) */

undefined1  [16] angle2(undefined8 param_1,undefined8 param_2,undefined8 param_3)

{
  int iVar1;
  double dVar2;
  double dVar3;
  undefined1 auVar4 [16];
  
  iVar1 = (int)((ulong)param_3 >> 0x20);
  dVar2 = atan2((double)((int)((ulong)param_1 >> 0x20) - iVar1),
                (double)((int)param_1 - (int)param_3));
  dVar3 = atan2((double)((int)((ulong)param_2 >> 0x20) - iVar1),
                (double)((int)param_2 - (int)param_3));
  auVar4._0_8_ = dVar2 - dVar3;
  auVar4._8_8_ = 0;
  return auVar4;
}

