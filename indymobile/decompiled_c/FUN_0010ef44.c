
void FUN_0010ef44(long *param_1,long *param_2)

{
  long lVar1;
  bool bVar2;
  long *plVar3;
  long *plVar4;
  long lVar5;
  long *plVar6;
  long lVar7;
  undefined8 *puVar8;
  long *plVar9;
  long *plVar10;
  
  plVar4 = (long *)*param_2;
  plVar6 = param_2;
  if (plVar4 == (long *)0x0) {
LAB_0010ef64:
    plVar4 = (long *)plVar6[1];
    if (plVar4 == (long *)0x0) {
      puVar8 = (undefined8 *)plVar6[2];
      bVar2 = true;
      goto LAB_0010ef88;
    }
  }
  else {
    plVar3 = (long *)param_2[1];
    if ((long *)param_2[1] != (long *)0x0) {
      do {
        plVar6 = plVar3;
        plVar3 = (long *)*plVar6;
      } while ((long *)*plVar6 != (long *)0x0);
      goto LAB_0010ef64;
    }
  }
  puVar8 = (undefined8 *)plVar6[2];
  bVar2 = false;
  plVar4[2] = (long)puVar8;
LAB_0010ef88:
  plVar3 = (long *)*puVar8;
  if (plVar3 == plVar6) {
    *puVar8 = plVar4;
    if (plVar6 == param_1) {
      plVar3 = (long *)0x0;
      param_1 = plVar4;
    }
    else {
      plVar3 = (long *)puVar8[1];
    }
  }
  else {
    puVar8[1] = plVar4;
  }
  lVar5 = plVar6[3];
  plVar10 = param_1;
  if (plVar6 != param_2) {
    puVar8 = (undefined8 *)param_2[2];
    plVar10 = (long *)*puVar8;
    plVar6[2] = (long)puVar8;
    puVar8[plVar10 != param_2] = plVar6;
    lVar7 = *param_2;
    lVar1 = param_2[1];
    *(long **)(lVar7 + 0x10) = plVar6;
    *plVar6 = lVar7;
    plVar6[1] = lVar1;
    if (lVar1 != 0) {
      *(long **)(lVar1 + 0x10) = plVar6;
    }
    plVar10 = plVar6;
    if (param_1 != param_2) {
      plVar10 = param_1;
    }
    *(char *)(plVar6 + 3) = (char)param_2[3];
  }
  if (((char)lVar5 != '\0') && (plVar10 != (long *)0x0)) {
    if (bVar2) {
      while( true ) {
        plVar6 = (long *)plVar3[2];
        if ((long *)*plVar6 != plVar3) break;
        if ((*(byte *)(plVar3 + 3) & 1) == 0) {
          lVar5 = plVar3[1];
          *(undefined1 *)(plVar3 + 3) = 1;
          *(undefined1 *)(plVar6 + 3) = 0;
          *plVar6 = lVar5;
          if (lVar5 != 0) {
            *(long **)(lVar5 + 0x10) = plVar6;
          }
          puVar8 = (undefined8 *)plVar6[2];
          plVar4 = (long *)*puVar8;
          plVar3[2] = (long)puVar8;
          puVar8[plVar4 != plVar6] = plVar3;
          plVar4 = plVar3;
          if (plVar10 != plVar6) {
            plVar4 = plVar10;
          }
          plVar3[1] = (long)plVar6;
          plVar6[2] = (long)plVar3;
          plVar3 = (long *)*plVar6;
          plVar6 = (long *)*plVar3;
          plVar10 = plVar4;
        }
        else {
          plVar6 = (long *)*plVar3;
        }
        if ((plVar6 != (long *)0x0) && (plVar4 = plVar3, (char)plVar6[3] == '\0')) {
LAB_0010f280:
          plVar3 = (long *)plVar4[2];
          lVar5 = *plVar3;
          *(char *)(plVar4 + 3) = (char)plVar3[3];
          lVar7 = *(long *)(lVar5 + 8);
          *(undefined1 *)(plVar3 + 3) = 1;
          *(undefined1 *)(plVar6 + 3) = 1;
          *plVar3 = lVar7;
          if (lVar7 != 0) {
            *(long **)(lVar7 + 0x10) = plVar3;
          }
          puVar8 = (undefined8 *)plVar3[2];
          plVar6 = (long *)*puVar8;
          *(undefined8 **)(lVar5 + 0x10) = puVar8;
          puVar8[plVar6 != plVar3] = lVar5;
          *(long **)(lVar5 + 8) = plVar3;
          plVar3[2] = lVar5;
          return;
        }
        plVar4 = (long *)plVar3[1];
        if ((plVar4 != (long *)0x0) && ((char)plVar4[3] == '\0')) {
          if (plVar6 != (long *)0x0) {
            plVar4 = plVar3;
            if ((char)plVar6[3] == '\0') goto LAB_0010f280;
            plVar4 = (long *)plVar3[1];
          }
          lVar5 = *plVar4;
          *(undefined1 *)(plVar4 + 3) = 1;
          *(undefined1 *)(plVar3 + 3) = 0;
          plVar3[1] = lVar5;
          if (lVar5 != 0) {
            *(long **)(lVar5 + 0x10) = plVar3;
          }
          puVar8 = (undefined8 *)plVar3[2];
          plVar6 = (long *)*puVar8;
          plVar4[2] = (long)puVar8;
          puVar8[plVar6 != plVar3] = plVar4;
          *plVar4 = (long)plVar3;
          plVar3[2] = (long)plVar4;
          plVar6 = plVar3;
          goto LAB_0010f280;
        }
        plVar6 = (long *)plVar3[2];
        *(undefined1 *)(plVar3 + 3) = 0;
        if (((char)plVar6[3] == '\0') || (plVar6 == plVar10)) {
LAB_0010f174:
          *(undefined1 *)(plVar6 + 3) = 1;
          return;
        }
LAB_0010f014:
        plVar3 = (long *)((undefined8 *)plVar6[2])[*(long **)plVar6[2] == plVar6];
      }
      if ((*(byte *)(plVar3 + 3) & 1) == 0) {
        plVar4 = (long *)plVar6[1];
        *(undefined1 *)(plVar3 + 3) = 1;
        *(undefined1 *)(plVar6 + 3) = 0;
        lVar5 = *plVar4;
        plVar6[1] = lVar5;
        if (lVar5 != 0) {
          *(long **)(lVar5 + 0x10) = plVar6;
        }
        puVar8 = (undefined8 *)plVar6[2];
        plVar9 = (long *)*puVar8;
        plVar4[2] = (long)puVar8;
        puVar8[plVar9 != plVar6] = plVar4;
        *plVar4 = (long)plVar6;
        plVar9 = (long *)*plVar3;
        plVar6[2] = (long)plVar4;
        plVar6 = plVar3;
        if (plVar10 != plVar9) {
          plVar6 = plVar10;
        }
        plVar3 = (long *)plVar9[1];
        plVar4 = (long *)*plVar3;
        plVar10 = plVar6;
        if (plVar4 != (long *)0x0) goto LAB_0010f0b0;
LAB_0010f0b8:
        plVar6 = (long *)plVar3[1];
        if ((plVar6 == (long *)0x0) || ((char)plVar6[3] != '\0')) {
          plVar4 = (long *)plVar3[2];
          *(undefined1 *)(plVar3 + 3) = 0;
          plVar6 = plVar10;
          if ((plVar4 != plVar10) && (plVar6 = plVar4, (*(byte *)(plVar4 + 3) & 1) != 0))
          goto LAB_0010f014;
          goto LAB_0010f174;
        }
LAB_0010f188:
        plVar10 = plVar3;
        if ((char)plVar6[3] == '\0') goto LAB_0010f1d8;
      }
      else {
        plVar4 = (long *)*plVar3;
        plVar6 = plVar10;
        if (plVar4 == (long *)0x0) goto LAB_0010f0b8;
LAB_0010f0b0:
        plVar10 = plVar6;
        if ((char)plVar4[3] != '\0') goto LAB_0010f0b8;
        plVar6 = (long *)plVar3[1];
        if (plVar6 != (long *)0x0) goto LAB_0010f188;
      }
      lVar5 = plVar4[1];
      *(undefined1 *)(plVar4 + 3) = 1;
      *(undefined1 *)(plVar3 + 3) = 0;
      *plVar3 = lVar5;
      if (lVar5 != 0) {
        *(long **)(lVar5 + 0x10) = plVar3;
      }
      puVar8 = (undefined8 *)plVar3[2];
      plVar6 = (long *)*puVar8;
      plVar4[2] = (long)puVar8;
      puVar8[plVar6 != plVar3] = plVar4;
      plVar4[1] = (long)plVar3;
      plVar3[2] = (long)plVar4;
      plVar10 = plVar4;
      plVar6 = plVar3;
LAB_0010f1d8:
      lVar5 = plVar10[2];
      plVar4 = *(long **)(lVar5 + 8);
      *(undefined1 *)(plVar10 + 3) = *(undefined1 *)(lVar5 + 0x18);
      lVar7 = *plVar4;
      *(undefined1 *)(lVar5 + 0x18) = 1;
      *(undefined1 *)(plVar6 + 3) = 1;
      *(long *)(lVar5 + 8) = lVar7;
      if (lVar7 != 0) {
        *(long *)(lVar7 + 0x10) = lVar5;
      }
      plVar6 = *(long **)(lVar5 + 0x10);
      lVar7 = *plVar6;
      plVar4[2] = (long)plVar6;
      plVar6[lVar7 != lVar5] = (long)plVar4;
      *plVar4 = lVar5;
      *(long **)(lVar5 + 0x10) = plVar4;
      return;
    }
    *(undefined1 *)(plVar4 + 3) = 1;
  }
  return;
}

