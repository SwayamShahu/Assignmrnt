package com.infilect.assignment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String username;

    @Column(length = 150)
    private String firstName;

    @Column(length = 150)
    private String lastName;

    @Column(nullable = false, length = 254)
    private String email;

    @Column
    private Integer userType = 1;

    @Column(length = 32)
    private String phoneNumber;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime modifiedOn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getUserType() { return userType; }
    public void setUserType(Integer userType) { this.userType = userType; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public User getSupervisor() { return supervisor; }
    public void setSupervisor(User supervisor) { this.supervisor = supervisor; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getModifiedOn() { return modifiedOn; }
    public void setModifiedOn(LocalDateTime modifiedOn) { this.modifiedOn = modifiedOn; }
}
