package com.bosch.manoj.sleuth.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class DemoEntity {

  @Id
  @GeneratedValue
  private int id;
  private String name;
}
