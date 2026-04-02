package com.infilect.assignment.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "permanent_journey_plans",
       uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "store_id", "date"})})
public class PermanentJourneyPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column
    private LocalDate date;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime modifiedOn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getModifiedOn() { return modifiedOn; }
    public void setModifiedOn(LocalDateTime modifiedOn) { this.modifiedOn = modifiedOn; }
}
