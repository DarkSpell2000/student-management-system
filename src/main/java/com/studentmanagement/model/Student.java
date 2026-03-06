package com.studentmanagement.model;

import io.micronaut.data.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String patronymic;

    @Past
    private LocalDate birthDate;

    @Pattern(regexp = "^\\+?[0-9\\-\\s]{10,15}$")
    private String phoneNumber;

    @Email
    private String email;

    private String address;

    private String recordBookNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Version
    private Long version;

    @DateCreated
    private java.time.Instant dateCreated;

    @DateUpdated
    private java.time.Instant dateUpdated;

    // Constructors
    public Student() {}

    public Student(String firstName, String lastName, String recordBookNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.recordBookNumber = recordBookNumber;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPatronymic() { return patronymic; }
    public void setPatronymic(String patronymic) { this.patronymic = patronymic; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRecordBookNumber() { return recordBookNumber; }
    public void setRecordBookNumber(String recordBookNumber) { this.recordBookNumber = recordBookNumber; }

    public Group getGroup() { return group; }
    public void setGroup(Group group) { this.group = group; }

    public String getFullName() {
        return lastName + " " + firstName + (patronymic != null ? " " + patronymic : "");
    }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public java.time.Instant getDateCreated() { return dateCreated; }
    public void setDateCreated(java.time.Instant dateCreated) { this.dateCreated = dateCreated; }

    public java.time.Instant getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(java.time.Instant dateUpdated) { this.dateUpdated = dateUpdated; }
}