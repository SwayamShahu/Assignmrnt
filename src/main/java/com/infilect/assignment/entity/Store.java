package com.infilect.assignment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stores", uniqueConstraints = {@UniqueConstraint(columnNames = "store_id")})
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String storeId;

    @Column(length = 255)
    private String storeExternalId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne
    @JoinColumn(name = "store_brand_id")
    private StoreBrand storeBrand;

    @ManyToOne
    @JoinColumn(name = "store_type_id")
    private StoreType storeType;

    @ManyToOne
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne
    @JoinColumn(name = "region_id")
    private Region region;

    @Column
    private Float latitude = 0.0f;

    @Column
    private Float longitude = 0.0f;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime modifiedOn = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getStoreExternalId() { return storeExternalId; }
    public void setStoreExternalId(String storeExternalId) { this.storeExternalId = storeExternalId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public StoreBrand getStoreBrand() { return storeBrand; }
    public void setStoreBrand(StoreBrand storeBrand) { this.storeBrand = storeBrand; }

    public StoreType getStoreType() { return storeType; }
    public void setStoreType(StoreType storeType) { this.storeType = storeType; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public Country getCountry() { return country; }
    public void setCountry(Country country) { this.country = country; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public Float getLatitude() { return latitude; }
    public void setLatitude(Float latitude) { this.latitude = latitude; }

    public Float getLongitude() { return longitude; }
    public void setLongitude(Float longitude) { this.longitude = longitude; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getModifiedOn() { return modifiedOn; }
    public void setModifiedOn(LocalDateTime modifiedOn) { this.modifiedOn = modifiedOn; }
}
