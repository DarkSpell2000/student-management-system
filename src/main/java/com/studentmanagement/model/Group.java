package com.studentmanagement.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_number", unique = true, nullable = false, length = 20)
    private String groupNumber;

    @Column(nullable = false)
    private String faculty;

    @Column
    private Integer course;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Student> students = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "curator_id")
    private User curator;

    // Constructors
    public Group() {}

    public Group(String groupNumber, String faculty, Integer course) {
        this.groupNumber = groupNumber;
        this.faculty = faculty;
        this.course = course;
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

    public List<Student> getStudents() { return students; }
    public void setStudents(List<Student> students) { this.students = students; }

    public User getCurator() { return curator; }
    public void setCurator(User curator) { this.curator = curator; }

    public int getStudentCount() { return students != null ? students.size() : 0; }
}
