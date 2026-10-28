
/* IOU::Line::isOnEdge(IOU::Vec2<double> const&) const */

bool __thiscall IOU::Line::isOnEdge(Line *this,Vec2 *param_1)

{
  double dVar1;
  double dVar2;
  double dVar3;
  double dVar4;
  
  dVar2 = *(double *)(this + 8);
  dVar4 = *(double *)(this + 0x10);
  dVar3 = *(double *)this;
  dVar1 = *(double *)(this + 0x18);
  if ((DAT_00104508 < ABS(dVar3 - dVar4)) || (DAT_00104508 < ABS(dVar2 - dVar1))) {
    dVar2 = *(double *)(param_1 + 8) - dVar2;
    dVar4 = *(double *)param_1 - dVar4;
    dVar3 = *(double *)param_1 - dVar3;
    dVar1 = *(double *)(param_1 + 8) - dVar1;
    if ((ABS(-(dVar2 * dVar4) + dVar1 * dVar3) < DAT_00104508) &&
       (dVar2 * dVar1 + dVar4 * dVar3 < DAT_00104508)) {
      return true;
    }
  }
  else if (ABS(*(double *)param_1 + (dVar3 + dVar4) * -0.5) <= DAT_00104508) {
    return ABS(*(double *)(param_1 + 8) + (dVar2 + dVar1) * -0.5) <= DAT_00104508;
  }
  return false;
}

