
/* IOU::iouEx(std::__ndk1::vector<IOU::Vec2<double>, std::__ndk1::allocator<IOU::Vec2<double> > >
   const&, std::__ndk1::vector<IOU::Vec2<double>, std::__ndk1::allocator<IOU::Vec2<double> > >
   const&) */

undefined1  [16] IOU::iouEx(vector *param_1,vector *param_2)

{
  double dVar1;
  double dVar2;
  double dVar3;
  double dVar4;
  undefined1 auVar5 [16];
  
  dVar1 = (double)areaIntersectionEx(param_1,param_2);
  dVar2 = (double)areaEx(param_1);
  dVar3 = (double)areaEx(param_2);
  dVar4 = (double)areaIntersectionEx(param_1,param_2);
  auVar5._0_8_ = dVar1 / ((dVar2 + dVar3) - dVar4);
  auVar5._8_8_ = 0;
  return auVar5;
}

