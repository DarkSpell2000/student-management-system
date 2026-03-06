package com.studentmanagement.model;

import io.micronaut.data.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 20)
    @Column(unique = true)
    private String groupNumber;

    @NotBlank
    private String faculty;

    private Integer course;

    private Integer studentCount;

    @OneToOne(mappedBy = "group")
    private User curator;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Student> students = new ArrayList<>();

    @Version
    private Long version;

    @DateCreated
    private java.time.Instant dateCreated;

    @DateUpdated
    private java.time.Instant dateUpdated;

    // Constructors
    public Group() {}

    public Group(String groupNumber, String faculty, Integer course) {
        this.groupNumber = groupNumber;
        this.faculty = faculty;
        this.course = course;
        this.studentCount = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGroupNumber() { return groupNumber; }
    public void setGroupNumber(String groupNumber) { this.groupNumber = groupNumber; }

    public String getFaculty() { return faculty; }
    public void setFaculty(String faculty) { this.faculty = faculty; }

    public Integer getCourse() { return course; }
    public void setCourse(Integer course) { this.course = course; }

    public Integer getStudentCount() { return studentCount; }
    public void setStudentCount(Integer studentCount) { this.studentCount = studentCount; }
    public void incrementStudentCount() { this.studentCount++; }
    public void decrementStudentCount() { this.studentCount--; }

    public User getCurator() { return curator; }
    public void setCurator(User curator) { this.curator = curator; }

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }
    public void addStudent(Student student) {
        students.add(student);
        student.setGroup(this);
        incrementStudentCount();
    }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public java.time.Instant getDateCreated() { return dateCreated; }
    public void setDateCreated(java.time.Instant dateCreated) { this.dateCreated = dateCreated; }

    public java.time.Instant getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(java.time.Instant dateUpdated) { this.dateUpdated = dateUpdated; }
}