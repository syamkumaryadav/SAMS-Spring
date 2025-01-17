package com.syam.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "stt")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;
    
    @Column(length = 30)
    private String name;
    
    @Column(length = 5)
    private String branch;
    
    @Column(length = 9)
    private String registrationNo;
    
    @Column(length = 15)
    private String achievementType;
    
    @Column(length = 25)
    private String achievementDetails;
    
    @Column(length = 15)
    private String prize;
    
    @Column(length = 30)
    private String event;
    
    @Column(length = 7)
    private String academicYear;
    
    private LocalDate achievementDate;
    
    private String fileName;
    private String fileType;

    @Lob
    private byte[] data;
   

} 