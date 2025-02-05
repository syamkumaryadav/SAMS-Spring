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
        convertImagesToBase64(students);//converts their images to Base64 (for display in web pages), and adds them to the model.
        model.addAttribute("students", students);  //MODEL-->Used to pass data from a controller to the view.
        return "students"; // Maps to students.html
    }
    
    /* Base64 ================>   binary ----------> image format
       Base64 is an encoding algorithm that converts any characters, binary data, and even images or sound files into a readable string,
	 
	    Why Base64 
		For Displaying Images: This method is useful when you want to display images directly on the page without needing a separate image file or URL. The Base64-encoded image is embedded into the HTML as part of the page content, which simplifies things when serving static files.
	*/
    @GetMapping("/search")
    public String searchStudents(
            @RequestParam(required = false) String name,//@RequestParam is used to bind HTTP request parameters to method parameters in a controller. 
            @RequestParam(required = false) String registrationNo,//The attribute required = false specifies that the request parameter is optional.
            @RequestParam(required = false) String academicYear,
            @RequestParam(required = false) String achievementType,
            Model model) {

        List<Student> students = studentService.searchStudents(name, registrationNo, academicYear, achievementType);
        convertImagesToBase64(students); //that converts images associated with student objects to Base64
		
        //Base64 for displaying
        
        /*Accept an array of students, where each object might have an image property containing the image file.
		Convert the image file to a Base64 string using a FileReader (in browsers) or a library like fs in Node.js.
		Replace or add a Base64-encoded image string to the respective student object.
		*/
        model.addAttribute("students", students);

        // this is for Counting in prizes     
        Map<String, Long> summaryCounts = new HashMap<>();	//summaryCounts it maps to output side count tables
        summaryCounts.put("first", 0L);		//The value is 0L, which is a Long literal
        summaryCounts.put("second", 0L);	// (a long integer with a value of 0).
        summaryCounts.put("third", 0L);
        summaryCounts.put("fourth", 0L);
        summaryCounts.put("fifth", 0L);
        summaryCounts.put("certifications", 0L);
        summaryCounts.put("academics", 0L);
        summaryCounts.put("participation", 0L);
        summaryCounts.put("others", 0L);

        students.forEach(student -> {   // here im using lambda expression
            String prize = student.getPrize();
            if (prize != null) {
                prize = prize.toLowerCase();
                summaryCounts.put(prize, summaryCounts.getOrDefault(prize, 0L) + 1);
                //is used to update a count in a map (summaryCounts) for a specific prize key.
                // getOrDefault() method is part of the Java Map interface. It is used to retrieve the value associated with a specific key in a map. If the key is not present, it returns a default value specified by the user.
            }
        });

        model.addAttribute("summaryCounts", summaryCounts);
        return "search";
    }

    
    @GetMapping("/adminSearch")
    public String adminsearchStudents(
            @RequestParam(required = false) String name,
            //@RequestParam(defaultValue = "Guest") String name, here u give any name display that name
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
        summaryCounts.put("second", 0L);// (a long integer with a value of 0).
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

        // Add the summary counts map to the model for use in my Thymeleaf that is in templates
        model.addAttribute("summaryCounts", summaryCounts);
        return "admin-search";  // Maps to admin-search.html
    }

    
    @GetMapping("/saveStudent")
    public String showStudentForm(Model model) {
        model.addAttribute("student", new Student());//Student class might be a POJO (Plain Old Java Object) with properties such as name, id, age, etc.
        return "students"; // Maps to student-form.html
    }

    @PostMapping("/upload")		// this for first save/ upload into database
    public String uploadStudent(@ModelAttribute Student student, @RequestParam("file") MultipartFile file) {  //Captures the uploaded file from the form with the field name file.
    																				//The MultipartFile interface provides methods to access file details (e.g., name, type, and content).
        try {	// @ModelAttribute Student student Binds form data to the Student object.
            if (file != null && !file.isEmpty()) {
                student.setFileName(file.getOriginalFilename());
                student.setFileType(file.getContentType());		// Sets the MIME type of the file (Multipurpose Internet Mail Extension)  e.g., image/png, application/pdf
                	// enctype="multipart/form-data" in the upload form in templates as a attribute
                student.setData(file.getBytes());// Reads file content as a byte array
            } else {
                System.out.println("File is empty or not selected");
            }

            studentService.saveStudent(student);//Passes the Student object (including file data) to the studentService, 
            // service class responsible for saving the data to a database or repository.
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/students?error=upload_failed"; //when uploading an IMAGE there showing unknown error reason is this line
        }

        return "redirect:/students/success"; // go to success page
    }

    @GetMapping("/success")
    public String successPage(Model model) {
        return "success";
    }
    
    
  //Converts the binary data (byte[]) to a Base64-encoded string using:    
    private void convertImagesToBase64(List<Student> students) {
        students.forEach(student -> {
            if (student.getData() != null) {        
                String base64Image = Base64.getEncoder().encodeToString(student.getData());
                student.setFileName("data:" + student.getFileType() + ";base64," + base64Image);	//data:image/png;base64,iVBORw0KGgoAAAANS...
                
				/*
				   data:: Indicates the start of a Data URI.
				student.getFileType(): Represents the MIME type of the image (e.g., image/png or image/jpeg).
				;base64,: Specifies the data encoding format as Base64.
				base64Image: The encoded image data
				
				Stores the resulting Data URI in the fileName field of the Student object.
				
				List<Student> students = List.of(
   						//new Student("John", "image/png", new byte[] { /* binary data */ //}),
                		//new Student("Jane", "image/jpeg", new byte[] { /* binary data */ })
				/*);
				 
				 
				After Execution:
				Each Student object will have its fileName property updated with a Data URI:
				
				data:image/png;base64,iVBORw0KGgoAAAANS...
				data:image/jpeg;base64,/9j/4AAQSkZJRgABA...*/
				
            }
        });
    }

    
    @GetMapping("/edit/{id}")  //this method used in admin-search for editing (indirectly to updating)
    public String editStudent(@PathVariable("id") Long id, Model model) {
        Student student = studentService.getStudentById(id);
        model.addAttribute("student", student);
        //Adds the retrieved Student object to the model with the attribute name "student".
        //This makes the student object available in the view (Thymeleaf or JSP) for rendering or data binding.
        return "student-form";// and that data transfers to this form(student-form for updating)
    }

    @PostMapping("/update")		// this for updating
    public String updateStudent(@ModelAttribute Student student, @RequestParam("file") MultipartFile file) {
        try {
            Student existingStudent = studentService.getStudentById(student.getStudentId()); //for getting student data from db 
            	 //student-form.html
            //this is not requiried but use
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
