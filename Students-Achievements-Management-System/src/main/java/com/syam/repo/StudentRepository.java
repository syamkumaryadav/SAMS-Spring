package com.syam.repo;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.syam.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE " +
           "(:name IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:registrationNo IS NULL OR LOWER(s.registrationNo) LIKE LOWER(CONCAT('%', :registrationNo, '%'))) AND " +
           "(:academicYear IS NULL OR s.academicYear = :academicYear) AND " +
           "(:achievementType IS NULL OR s.achievementType = :achievementType)")
    List<Student> findByFilters(
        @Param("name") String name,
        @Param("registrationNo") String registrationNo,
        @Param("academicYear") String academicYear,
        @Param("achievementType") String achievementType
    );
}