package com.spring_food.springfood.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "tag")
@AttributeOverride(name = "id", column = @Column(name = "tag_name"))
public class Tag extends AbstractEntity {

    @Column(name = "tag_description")
    private String tagDescription;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL)
    private Set<ProductTag> productTags = new HashSet<>();
}
