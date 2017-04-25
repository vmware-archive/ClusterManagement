package com.geode.management.domain;

public class RegionProperty {
  
  private String name;
  private String type;
  
  public String getName() {
    return name;
  }
  public void setName(String property) {
    this.name = property;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  
  @Override
  public String toString() {
    return "RegionProperty [name=" + name + ", type=" + type + "]";
  }
}
