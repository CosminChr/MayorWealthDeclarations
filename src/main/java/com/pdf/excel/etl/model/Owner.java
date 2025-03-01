package com.pdf.excel.etl.model;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Author: Cosmin Chiriac
 * Date: 2025-03-01
 */
@Getter
@Setter
public class Owner
{
  private String firstName;
  private String fatherInitial;
  private String lastName;
  private String office;
  private String county;
  private String party;
  private Set<Property> properties;
  private Set<Vehicle> vehicles;

  public Owner() {
    this.properties = new LinkedHashSet<>();
    this.vehicles = new LinkedHashSet<>();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Owner owner = (Owner) o;
    return Objects.equals(firstName, owner.firstName) && Objects.equals(fatherInitial, owner.fatherInitial) && Objects.equals(lastName, owner.lastName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(firstName, fatherInitial, lastName);
  }
}
