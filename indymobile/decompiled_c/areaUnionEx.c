
/* IOU::areaUnionEx(std::__ndk1::vector<IOU::Vec2<double>, std::__ndk1::allocator<IOU::Vec2<double>
   > > const&, std::__ndk1::vector<IOU::Vec2<double>, std::__ndk1::allocator<IOU::Vec2<double> > >
   const&) */

undefined1  [16] IOU::areaUnionEx(vector *param_1,vector *param_2)

{
  double dVar1;
  double dVar2;
  double dVar3;
  undefined1 auVar4 [16];
  
  dVar1 = (double)areaEx(param_1);
  dVar2 = (double)areaEx(param_2);
  dVar3 = (double)areaIntersectionEx(param_1,param_2);
  auVar4._0_8_ = (dVar1 + dVar2) - dVar3;
  auVar4._8_8_ = 0;
  return auVar4;
}

