package com.pdf.excel.etl.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Author: Cosmin Chiriac
 * Date: 2025-03-01
 */
@RequiredArgsConstructor
@Getter
public enum CountyEnum
{
  AB("Alba"),
  AR("Arad"),
  AG("Arges"),
  BC("Bacau"),
  BH("Bihor"),
  BN("Bistrita-Nasaud"),
  BT("Botosani"),
  BV("Brasov"),
  BR("Braila"),
  B("Bucuresti"),
  BZ("Buzau"),
  CS("Caras-Severin"),
  CL("Calarasi"),
  CJ("Cluj"),
  CT("Constanta"),
  CV("Convasna"),
  DB("Dambovita"),
  DJ("Dolj"),
  GL("Galati"),
  GR("Giurgiu"),
  GJ("Gorj"),
  HR("Harghita"),
  HD("Hunedoara"),
  IL("Ialmoita"),
  IS("Iasi"),
  IF("Ilfov"),
  MM("Maramures"),
  MH("Mehedinti"),
  MS("Mures"),
  NT("Neamt"),
  OT("Olt"),
  PH("Prahova"),
  SM("Satu Mare"),
  SJ("Salaj"),
  SB("Sibiu"),
  SV("Suceava"),
  TR("Teleorman"),
  TM("Timis"),
  TL("Tulcea"),
  VS("Vaslui"),
  VL("Valcea"),
  VN("Vrancea");

  private final String name;
}
