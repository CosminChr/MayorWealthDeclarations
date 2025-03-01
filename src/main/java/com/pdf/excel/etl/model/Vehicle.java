package com.pdf.excel.etl.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Vehicle {
  private Owner owner;
  private String type;
  private String brand;
  private String pieces;
  private String productionYear;
  private String acquisitionManner;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Vehicle vehicle = (Vehicle) o;
    return Objects.equals(owner, vehicle.owner) && Objects.equals(type, vehicle.type) && Objects.equals(brand, vehicle.brand) && Objects.equals(pieces, vehicle.pieces) && Objects.equals(productionYear, vehicle.productionYear) && Objects.equals(acquisitionManner, vehicle.acquisitionManner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(owner, type, brand, pieces, productionYear, acquisitionManner);
  }
}
