package com.example.demo_springboot.resourceObjects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
public class Battery {

  private @Id
  @GeneratedValue
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  Long id;
  private String name;
  private String postcode;
  private Long capacity;

  public Battery() {}

  public Battery(String name, String postcode, Long capacity) {
    final Long id;
    this.name = name;
    this.postcode = postcode;
    this.capacity = capacity;
  }

  public Long getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }
  
  public String getPostcode() {
    return this.postcode;
  }

  public Long getCapacity() {
    return this.capacity;
  }

  public void setId(Long id) {
    throw new UnsupportedOperationException("ID is auto-generated and cannot be modified.");
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setPostcode(String postcode) {
    this.postcode = postcode;
  }

  public void setCapacity(Long capacity) {
    this.capacity = capacity;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o)
      return true;
    if (!(o instanceof Battery))
      return false;
    Battery battery = (Battery) o;
    return Objects.equals(this.id, battery.id) && Objects.equals(this.name, battery.name)
        && Objects.equals(this.postcode, battery.postcode) && Objects.equals(this.capacity, battery.capacity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.id, this.name, this.postcode, this.capacity);
  }

  @Override
  public String toString() {
    return "Battery{" + "id=" + this.id + ", name='" + this.name + '\'' + ", postcode='" + this.postcode
        + '\'' + ", capacity='" + this.capacity + '\'' + '}';
  }
}

