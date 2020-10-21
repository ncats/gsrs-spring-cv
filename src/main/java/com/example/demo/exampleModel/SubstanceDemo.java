package com.example.demo.exampleModel;

import lombok.Data;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
public class SubstanceDemo {
    @Id
    private UUID id;
    @ElementCollection
    private List<NameDemo> names = new ArrayList<>();
}
