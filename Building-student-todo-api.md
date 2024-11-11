The best place to start is with the **Authentication API**. Here’s why and how to proceed:

### 1. **Start with the Authentication API**
   - **Purpose**: It will handle user authentication, providing secure access and token management, which is essential before integrating other services.
   - **Implementation**:
     - Create endpoints for user registration, login, and token generation (e.g., JWT).
     - Include role-based access control if needed, allowing flexibility for users with different access levels.

### 2. **Implement the Student API**
   - **Purpose**: Manages student data, providing endpoints to create, update, and retrieve students.
   - **Implementation**:
     - Define REST endpoints for CRUD operations for student records.
     - Integrate with the Authentication API to ensure secure access using tokens, making sure only authenticated users can access these endpoints.

### 3. **Set Up the Todo API**
   - **Purpose**: Provides functionality for managing todos, linking each to a specific student.
   - **Implementation**:
     - Build CRUD operations for todos, associating each todo with a student’s unique ID.
     - Integrate with both the Authentication and Student APIs, using tokens for secure access and linking each todo to the appropriate student.

### 4. **Combine the Microservices with API Gateway**
   - **Purpose**: The API Gateway will act as a single entry point, routing requests to the appropriate service.
   - **Setup**:
     - Configure routes in the API Gateway for each microservice.
     - Implement load balancing and centralize authentication (so requests to all services pass through the gateway).
     - Ensure secure communication between microservices, potentially adding an additional layer of security such as service-to-service authentication.

Starting with Authentication ensures secure access from the beginning, and then adding Student and Todo services lets you gradually build functionality. Would you like guidance on setting up the Authentication API first?




To build an Authentication API using Spring Boot, we’ll create endpoints for user registration, login, and token generation using JWT (JSON Web Token). This API will ensure that only authenticated users can access protected resources. Let’s break it down into steps.

### Step 1: Set Up the Project

1. **Create a new Spring Boot project**: Use Spring Initializr or your IDE to create a Spring Boot project with the following dependencies:
   - Spring Web
   - Spring Security
   - Spring Data JPA
   - MySQL Driver (or H2 if testing locally)
   - JWT (JSON Web Token) dependency

### Step 2: Configure MySQL Database (application.properties)

In `src/main/resources/application.properties`, add your database configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_db
spring.datasource.username=root
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.security.jwt.secret=my-secret-key
spring.security.jwt.expiration=86400000
```

The `jwt.secret` and `jwt.expiration` are custom properties that will be used for generating and validating JWTs.

### Step 3: Define the User Entity

Create an entity `User` that represents the user in the system.

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String password;
    private String role;  // e.g., "ROLE_USER", "ROLE_ADMIN"
    
    // Getters and setters
}
```

### Step 4: Create User Repository

Create a repository interface to handle database operations for the `User` entity.

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
```

### Step 5: Implement JWT Utility Class

Create a utility class to generate and validate JWT tokens.

```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${spring.security.jwt.secret}")
    private String jwtSecret;
    
    @Value("${spring.security.jwt.expiration}")
    private long jwtExpiration;

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### Step 6: Create Authentication Service

This service will handle user registration and login.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    public String register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        userRepository.save(user);
        return "User registered successfully";
    }

    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtil.generateToken(username);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
```

### Step 7: Configure Security

In `SecurityConfig`, configure HTTP security and use JWT for authentication.

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/auth/**").permitAll()
            .anyRequest().authenticated()
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Step 8: Create Authentication Controller

Create a controller to handle registration and login requests.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        return authService.login(username, password);
    }
}
```

### Step 9: Test the API with Postman

1. **Register a new user**:
   - **POST** request to `http://localhost:8081/auth/register`
   - **Body** (JSON): `{"username": "testuser", "password": "password"}`
   
2. **Login**:
   - **POST** request to `http://localhost:8081/auth/login`
   - **Params**: `username=testuser`, `password=password`
   - This will return a JWT token, which you should include in the `Authorization` header for subsequent requests to protected endpoints.

3. **Access Protected Endpoints**:
   - Include the token in the `Authorization` header as `Bearer <token>` when accessing protected endpoints.

This basic setup provides an authentication API with registration, login, and token-based security. Next, you can integrate this Authentication API with the Student and Todo APIs to secure access.




To build the Student API, we can follow these steps to set up endpoints that allow verified students to manage their profiles.

### Step 1: Create a Spring Boot Project for the Student API

1. **Create a New Spring Boot Project** using Spring Initializr (either through your IDE or from [start.spring.io](https://start.spring.io/)).
2. **Dependencies:** Add the following dependencies:
   - **Spring Web** for creating REST APIs.
   - **Spring Data JPA** for database operations.
   - **MySQL Driver** (or any other database you prefer).
   - **Spring Security** (optional, if you want to secure the endpoints individually).
   
### Step 2: Define the Student Entity

In the `model` package, create a `Student` entity to represent student profiles.

```java
package com.example.studentapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String department;

    // Getters and Setters
}
```

### Step 3: Create a Repository Interface

In the `repository` package, create a `StudentRepository` interface to manage `Student` entities.

```java
package com.example.studentapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.studentapi.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // Additional query methods if needed
}
```

### Step 4: Implement the Student Service

In the `service` package, create a `StudentService` to handle business logic.

```java
package com.example.studentapi.service;

import com.example.studentapi.model.Student;
import com.example.studentapi.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    @Autowired
    private StudentRepository studentRepository;

    public Student saveStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new RuntimeException("Student not found"));
    }
}
```

### Step 5: Create a Controller for the Student API

In the `controller` package, set up a `StudentController` to expose the endpoints.

```java
package com.example.studentapi.controller;

import com.example.studentapi.model.Student;
import com.example.studentapi.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @PostMapping
    public Student createStudent(@RequestBody Student student) {
        return studentService.saveStudent(student);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentService.getStudentById(id);
    }
}
```

### Step 6: Configure Security (Optional)

To allow only verified students to access the endpoints, add a security filter that validates the authentication token from the `Authentication API` for each request. You can use Spring Security and configure a `JwtFilter` to verify tokens.

### Step 7: Test the Student API

- Use Postman or another REST client to test the endpoints.
  - **POST** `/api/students` - Create a new student profile.
  - **GET** `/api/students` - Retrieve a list of all students.
  - **GET** `/api/students/{id}` - Retrieve a student profile by ID.

Once your Student API is ready, you can then integrate it with the Todo API to enable each student to have a personalized todo list. Let me know if you’re ready to proceed with linking these services or if you'd like to explore any other specific parts!




Yes I want the filter only if the student is verified then only he should be able to see his profile and only his profile:
To implement this filter, let's create an `AuthenticationFilter` in the `student-api` service. This filter will check for a valid token in the request headers, verify it with the `authentication-api`, and ensure the student can only access their own profile. Here’s a guide on how to set up this filter in `student-api`:

### 1. Create the `AuthenticationFilter`
The filter will intercept incoming requests, validate the token, and retrieve the student’s ID.

```java
package com.example.studentapi.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        // Check if token is missing
        if (token == null || token.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization token is required");
            return;
        }

        // Validate token with authentication API
        ResponseEntity<String> authResponse = restTemplate.getForEntity(
            "http://localhost:8082/auth/validate-token?token=" + token, String.class);

        if (authResponse.getStatusCode().is2xxSuccessful()) {
            String studentId = authResponse.getBody();
            request.setAttribute("studentId", studentId);
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
        }
    }
}
```

### 2. Configure the Filter for `student-api`
Register this filter so that it only applies to student profile endpoints.

```java
package com.example.studentapi.config;

import com.example.studentapi.filters.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authFilter() {
        FilterRegistrationBean<AuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthenticationFilter());
        registrationBean.addUrlPatterns("/students/profile/*"); // Adjust as needed
        return registrationBean;
    }
}
```

### 3. Update Student Profile Controller
Use the student ID set by the filter to restrict access to only the verified student’s profile.

```java
package com.example.studentapi.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/students")
public class StudentController {

    @GetMapping("/profile/{id}")
    public ResponseEntity<Student> getStudentProfile(@PathVariable String id, 
                                                     @RequestAttribute("studentId") String studentId) {

        if (!id.equals(studentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Student student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }
}
```

### Explanation

- **Filter Authentication**: The filter validates the token by making a call to `authentication-api`. If the token is valid, the student’s ID is added to the request attribute.
- **Controller Restriction**: The `studentId` from the filter ensures the student can only access their own profile.

With this setup, only authenticated and verified students can access their profile information in `student-api`.
