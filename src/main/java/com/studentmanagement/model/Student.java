package com.studentmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "address")
    private String address;

    @Column(name = "record_book_number", unique = true, nullable = false, length = 20)
    private String recordBookNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    public Student() {}

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
}
