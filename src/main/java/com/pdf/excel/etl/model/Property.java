package com.pdf.excel.etl.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Property
{
//  private Owner owner;
  private String locality;
  private String county;
  private String category;
  private String acquisitionYear;
  private String area;
  private String share;
  private String acquisitionManner;
  private String holder;
  private PropertyType propertyType;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Property property = (Property) o;
    return Objects.equals(locality, property.locality) && Objects.equals(county, property.county) && Objects.equals(category, property.category) && Objects.equals(acquisitionYear, property.acquisitionYear) && Objects.equals(area, property.area) && Objects.equals(share, property.share) && Objects.equals(acquisitionManner, property.acquisitionManner) && Objects.equals(holder, property.holder) && propertyType == property.propertyType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(locality, county, category, acquisitionYear, area, share, acquisitionManner, holder, propertyType);
  }
}
