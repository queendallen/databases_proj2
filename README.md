# databases_proj2

## About

In this project, the following tables from the Student Registration System will be used: 

> `Students(sid, firstname, lastname, status, gpa, email)`

> `Courses(dept_code, course_no, title)`

> `Prerequisites(dept_code, course_no, pre_dept_code, pre_course_no)`

> `Classes(classid, dept_code, course_no, sect_no, year, semester, limit, class_size)`

> `Enrollments(sid, classid, lgrade)`
     
In addition, the following table is also required for this project:          

> `Logs(logid, who, time, table_name, operation, key_value)`
	
Each tuple in the logs table describes who (the login name of a database user) has performed what operation (insert, delete, update) on which table (give the table name) and which tuple (as indicated by the value of the primary key of the tuple) at what time. Attribute logid is the primary key of the table.

## Functionality

1. Use a sequence to generate the values for logid automatically when new log records are inserted into the logs table. All logid values should have 4 digits. 
2. Write procedures in your package to display the tuples in each of the six tables: students, courses, prerequisites, classes, enrollments, and logs. As an example, you can write a procedure, say **show_students**, to display all students in the students table. 
3. Write a procedure in your package to add a student into the students table. The information of the student to be inserted is provided as parameters to your procedure.
4. Write a procedure in your package that, for a given student (with sid provided as a parameter), lists the sid, fistname, lastname, and gpa of the student as well as all classes the student has taken or is taking. For each class, show classid, dept_code, course_no, semester, and year. (dept_code and course_no should be displayed together, e.g., CS532.) If the student is not in the students table, report “The sid is invalid.” If the student has not taken any course, report “The student has not taken any course.”
5. Write a procedure in your package that, for a given course (with dept_code and course_no as parameters), returns all its prerequisite courses, including both direct and indirect prerequisite courses. If course C1 has course C2 as a prerequisite, C2 is a direct prerequisite. In addition, if C2 has course C3 as a prerequisite, then C3 is an indirect prerequisite for C1. Please also note that indirect prerequisites can be more than two levels away. For each direct/indirect prerequisite course, show the dept_code, course_no, and title, e.g., CS532 Database.
6. Write a procedure in your package that, for a given class (with classid provided as a parameter), lists the classid, course title, semester, and year of the class as well as all the students (show sid, firstname, lastname, and email) who have taken or are taking the class. If the class is not in the classes table, report “The cid is invalid.” If no student has taken or is taking the class, report “No student is enrolled in the class.”
7. Write a procedure in your package to enroll a student into a class. The sid of the student and the classid of the class are provided as parameters.
- If the student is not in the students table, report “sid not found.” If the class is not in the classes table, print an error message: “invalid classid” 
- If enrolling the student into a class would cause “class_size > limit”, reject the enrollment and print “class full” 
- If the student is already in the class, print “already in this class” 
- If the student is already enrolled in four other classes in the same semester and the same year, print “overloaded!” and disallow the student to be enrolled. 
- If the student has not completed the required prerequisite courses with minimum grade “C”, reject the enrollment and print “Prerequisite courses have not been completed.” 
- For all the other cases, the requested enrollment should be performed. You should make sure that all data are consistent after each enrollment. For example, after you successfully enrolled a student into a class, the size of the corresponding class should be updated accordingly. Use trigger(s) to implement the updates of values caused by successfully enrolling a student into a class. It is recommended that all triggers for this project be implemented outside of the package.
8. Write a procedure in your package to drop a student from a class (i.e., delete a tuple from the enrollments table). The sid of the student and the classid of the class are provided as parameters.  
-	If the student is not in the students table, print an error message: “sid not found.” 
-	If the classid is not in the classes table, print “classid not found.” 
-	If the student is not enrolled in the class, print “student not enrolled in this class.” 
-	If dropping the student from the class would cause a violation of the prerequisite requirement for another class, then reject the drop attempt and report “drop request rejected due to prerequisite requirements.” 
-	In all the other cases, the student should be dropped from the class. If the class is the last class for the student, print “student enrolled in no class.”  If this student is the last student in the class, print “no student in this class.” 
-	Once again, you should make sure that all data are consistent after the drop and all updates caused by the drop should be implemented using trigger(s). 
9. Write a procedure in your package to delete a student from the students table based on a given sid (input parameter). If the student is not in the students table, report “sid not found.” When a student is deleted, all tuples in the enrollments table involving the student should also be deleted (use a trigger to implement this) and this will trigger a number of actions as described in #8.
10. Write triggers to add tuples to the logs table automatically whenever a student is added to or deleted from the students table, or when a student is successfully enrolled into or dropped from a class
