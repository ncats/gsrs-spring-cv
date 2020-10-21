package com.example.demo.exampleModel;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class NameDemo {
    @Id
    String name;

}
