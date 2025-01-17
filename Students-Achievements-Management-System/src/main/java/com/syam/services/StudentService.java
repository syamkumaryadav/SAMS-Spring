package com.syam.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.syam.entity.Student;
import com.syam.repo.StudentRepository;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> searchStudents(String name, String registrationNo, String academicYear, String achievementType) {
        return studentRepository.findByFilters(name, registrationNo, academicYear, achievementType);
    }

    public void saveStudent(Student student) {
        studentRepository.save(student);
    }

	public void deleteStudent(Long id) {
		studentRepository.deleteById(id);
	}

	public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null); // Fetch student by ID, return null if not found
    }
}
