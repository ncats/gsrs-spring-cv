package com.example.demo;

import lombok.Data;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.GenericField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;


import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Indexed
public class Book {
    @Id
    @GeneratedValue
    private Integer id;

    @FullTextField(analyzer = GsrsAnalyzers.DEFAULT_NAME)
    private String title;


    private int year;
    @ElementCollection
    private Set<String> keywords = new HashSet<>();

    @ManyToMany
    @IndexedEmbedded
    private Set<Author> authors = new HashSet<>();

    public void addAuthor(Author author){
        authors.add(author);
    }
}
