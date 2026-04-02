package com.infilect.assignment.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "countries", uniqueConstraints = {@UniqueConstraint(columnNames = "name")})
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
