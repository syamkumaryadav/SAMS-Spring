package com.syam.controllers;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.syam.entity.Student;
import com.syam.services.StudentService;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/home")
    public String homePage(Model model) {
        return "home"; // Maps to home.html
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        return "contact"; // Maps to contact.html
    }

    @GetMapping("/testimonials")
    public String testimonialsPage(Model model) {
        return "testimonials"; // Maps to testimonials.html
    }

    @GetMapping("/list")
    public String listStudents(Model model) {
        List<Student> students = studentService.getAllStudents();
        convertImagesToBase64(students);
        model.addAttribute("students", students);
        return "students"; // Maps to students.html
    }

    @GetMapping("/search")
    public String searchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String achievementType,
            Model model) {

        List<Student> students = studentService.searchStudents(name, registrationNo, academicYear, achievementType);
        convertImagesToBase64(students);
        model.addAttribute("students", students);

        Map<String, Long> summaryCounts = new HashMap<>();
        summaryCounts.put("first", 0L);
        summaryCounts.put("second", 0L);
        summaryCounts.put("third", 0L);
        summaryCounts.put("fourth", 0L);
        summaryCounts.put("fifth", 0L);
        summaryCounts.put("certifications", 0L);
        summaryCounts.put("academics", 0L);
        summaryCounts.put("participation", 0L);
        summaryCounts.put("others", 0L);

        students.forEach(student -> {
            String prize = student.getPrize();
            if (prize != null) {
                prize = prize.toLowerCase();
                summaryCounts.put(prize, summaryCounts.getOrDefault(prize, 0L) + 1);
            }
        });

        model.addAttribute("summaryCounts", summaryCounts);
        return "search";
    }

    
    @GetMapping("/adminSearch")
    public String adminsearchStudents(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String registrationNo,
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String achievementType,
            Model model) {

        // Fetch the students based on the search criteria
        List<Student> students = studentService.searchStudents(name, registrationNo, academicYear, achievementType);
        convertImagesToBase64(students);  // Convert images to base64 if needed
        model.addAttribute("students", students);

        // Prepare summary counts map with default values
        Map<String, Long> summaryCounts = new HashMap<>();
        summaryCounts.put("first", 0L);
        summaryCounts.put("second", 0L);
        summaryCounts.put("third", 0L);
        summaryCounts.put("certifications", 0L);
        summaryCounts.put("academics", 0L);
        summaryCounts.put("participation", 0L);

        // Iterate over students to update the summary counts based on prize type
        students.forEach(student -> {
            String prize = student.getPrize();
            if (prize != null && !prize.isEmpty()) {
                prize = prize.toLowerCase();  // Normalize the prize string to lowercase
                summaryCounts.put(prize, summaryCounts.getOrDefault(prize, 0L) + 1);  // Increment the count
            }
        });

        // Add the summary counts map to the model for use in Thymeleaf template
        model.addAttribute("summaryCounts", summaryCounts);
        return "admin-search";  // Maps to admin-search.html
    }

    
    @GetMapping("/saveStudent")
    public String showStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "students"; // Maps to student-form.html
    }

    @PostMapping("/upload")
    public String uploadStudent(@ModelAttribute Student student, @RequestParam("file") MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                student.setFileName(file.getOriginalFilename());
                student.setFileType(file.getContentType());
                student.setData(file.getBytes());
            } else {
                System.out.println("File is empty or not selected");
            }

            studentService.saveStudent(student);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/students?error=upload_failed";
        }

        return "redirect:/students/success";
    }

    @GetMapping("/success")
    public String successPage(Model model) {
        return "success";
    }

    private void convertImagesToBase64(List<Student> students) {
        students.forEach(student -> {
            if (student.getData() != null) {
                String base64Image = Base64.getEncoder().encodeToString(student.getData());
                student.setFileName("data:" + student.getFileType() + ";base64," + base64Image);
            }
        });
    }

    @GetMapping("/edit/{id}")
    public String editStudent(@PathVariable("id") Long id, Model model) {
        Student student = studentService.getStudentById(id);
        model.addAttribute("student", student);
        return "student-form";
    }

    @PostMapping("/update")
    public String updateStudent(@ModelAttribute Student student, @RequestParam("file") MultipartFile file) {
        try {
            Student existingStudent = studentService.getStudentById(student.getStudentId());

            if (existingStudent == null) {
                return "redirect:/students?error=not_found";
            }

            existingStudent.setName(student.getName());
            existingStudent.setBranch(student.getBranch());
            existingStudent.setRegistrationNo(student.getRegistrationNo());
            existingStudent.setAchievementType(student.getAchievementType());
            existingStudent.setAchievementDetails(student.getAchievementDetails());
            existingStudent.setPrize(student.getPrize());
            existingStudent.setEvent(student.getEvent());
            existingStudent.setAcademicYear(student.getAcademicYear());
            existingStudent.setAchievementDate(student.getAchievementDate());

            if (file != null && !file.isEmpty()) {
                existingStudent.setFileName(file.getOriginalFilename());
                existingStudent.setFileType(file.getContentType());
                existingStudent.setData(file.getBytes());
            }

            studentService.saveStudent(existingStudent);
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/students?error=update_failed";
        }
        return "redirect:/students/adminSearch";
    }

    @GetMapping("/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id) {
        studentService.deleteStudent(id);
        return "redirect:/students/adminSearch";
    }
}
