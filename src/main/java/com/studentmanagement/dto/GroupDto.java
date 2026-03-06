package com.studentmanagement.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Serdeable
public class GroupDto {

    private Long id;

    @NotBlank(message = "Номер группы обязателен")
    @Size(min = 2, max = 20, message = "Номер группы должен быть от 2 до 20 символов")
    private String groupNumber;

    @NotBlank(message = "Факультет обязателен")
    private String faculty;

    private Integer course;
    private Integer studentCount;

    private Long curatorId;
    private String curatorName;

    // Constructors
    public GroupDto() {}

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

    public Long getCuratorId() { return curatorId; }
    public void setCuratorId(Long curatorId) { this.curatorId = curatorId; }

    public String getCuratorName() { return curatorName; }
    public void setCuratorName(String curatorName) { this.curatorName = curatorName; }
}