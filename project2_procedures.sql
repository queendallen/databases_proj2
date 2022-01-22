/* Package specification */
create or replace package proj2 as
    /* Cursor object used to get the results of 
    1) retrieving all data from a table or 
    2) retrieving all of a class’ info or 
    3) retrieving all of a student’s info or 
    4) retrieving all the prerequisites for a class */
    type ref_cursor is ref cursor; 
    function getclasses
        return ref_cursor;

    function getcourses
        return ref_cursor;

    function getprereqs
        return ref_cursor;

    function getenrollments
        return ref_cursor;

    function getlogs
        return ref_cursor;

    function getstudents
        return ref_cursor;

    function getclassInfo(classid_in in classes.classid%type)
        return ref_cursor;

    function getstudentInfo(sid_in in students.sid%type)
        return ref_cursor;

    function getprereqs(dept_in in prerequisites.dept_code%type,
        num_in in prerequisites.course_no%type)
        return ref_cursor;

    procedure add_student(sid_in in students.sid%type,
        firstname in students.firstname%type, 
        lastname in students.lastname%type, 
        status_in in students.status%type, 
        gpa in students.gpa%type,
        email in students.email%type);
    
    procedure enroll_student(sid_in in students.sid%type,
        classid_in in enrollments.classid%type);
    
    procedure drop_student(sid_in in students.sid%type,
        classid_in in enrollments.classid%type);

    procedure delete_student(sid_in in students.sid%type);
    end;
/

/* Package body */
show errors
create or replace package body proj2 as
    /*Returns every class*/
    function getclasses
    return ref_cursor as
    rc ref_cursor;
    begin
        open rc for
        select * from classes;
        return rc;
    end;

    /*Returns every course*/
    function getcourses
    return ref_cursor as
    rc ref_cursor;
    begin
        open rc for
        select * from courses;
        return rc;
    end;

    /*Returns every prerequisite*/
    function getprereqs
    return ref_cursor as
    rc ref_cursor;
    begin
        open rc for
        select * from prerequisites;
        return rc;
    end;

    /*Returns every enrollment*/
    function getenrollments
    return ref_cursor as
    rc ref_cursor;
    begin
        open rc for
        select * from enrollments;
        return rc;
    end;

    /*Returns every log */
    function getlogs
    return ref_cursor as
    rc ref_cursor;
    begin
        open rc for
        select * from logs;
        return rc;
    end;

    /*Returns every student*/
    function getstudents
    return ref_cursor as
    rc ref_cursor;
    begin
        open rc for
        select * from students;
        return rc;
    end;

    /*Returns all of a class’ information
        Input: classid from classes table
    */
    function getclassInfo(classid_in in classes.classid%type)
    return ref_cursor as
    rc ref_cursor;
    begin
            open rc for
            select title, semester, year, sid, firstname, lastname, email
            from students natural join enrollments natural join classes natural join courses
            where classid=classid_in;
            return rc;
    end;

    /*Returns all of a student’s information
        Input: sid from students table
   */
    function getstudentInfo(sid_in in students.sid%type)
    return ref_cursor as
    rc ref_cursor;
    begin
            open rc for
            select firstname, lastname, gpa, classid, concat(dept_code, course_no), semester, year
            from students natural join enrollments natural join classes 
            where sid=sid_in;
            return rc;
    end;

    /*Returns all the prerequisites for a class
        Input: dept name and course no from prerequisites
    */
    function getprereqs(dept_in in prerequisites.dept_code%type,
        num_in in prerequisites.course_no%type)
    return ref_cursor as
    rc ref_cursor;
    begin
            open rc for
            with allPreReqs (dept_code_out, course_no_out, title_out) as 
                                /* Base Case */
                                (select dept_code, course_no, title 
                                from courses 
                                where (dept_code, course_no) in 
                                (select pre_dept_code, pre_course_no 
                                from prerequisites 
                                where dept_code = dept_in and course_no = num_in)

                                union all 
                                /* Recursive Case */
                                select dept_code, course_no, title 
                                from allPreReqs, courses
                                where (dept_code, course_no) in 
                                (select pre_dept_code, pre_course_no 
                                from prerequisites 
                                where dept_code = dept_code_out and course_no = course_no_out))
                                cycle dept_code_out set at_end to 1 default 0
            select dept_code_out, course_no_out, title_out from allPreReqs;
            return rc;
    end;

    /*Adds a student into a class – Invokes insert_student trigger 
	    Input: sid, firstname, lastname, status, gpa, and email of a student
    */
    procedure add_student(sid_in in students.sid%type,
        firstname in students.firstname%type, 
        lastname in students.lastname%type, 
        status_in in students.status%type, 
        gpa in students.gpa%type,
        email in students.email%type) is
    begin 
        insert into students values (sid_in, firstname, lastname, status_in, gpa, email);
    end;

    /*Enrolls a student into a class – Invokes insert_enrollment trigger and update_enrollstudent trigger
	    Input: student sid and class classid
    */
    procedure enroll_student(sid_in in students.sid%type,
        classid_in in enrollments.classid%type) is
    begin 
        insert into enrollments values (sid_in, classid_in, null);
    end;

    /*Drops a student from a class – Invokes delete_enrollment trigger and update_dropstudent trigger
	    Input: student sid and class classid
    */
    procedure drop_student(sid_in in students.sid%type,
        classid_in in enrollments.classid%type) is
    begin 
        delete from enrollments where sid = sid_in and classid = classid_in;
    end;

    /*Deletes a student from the students table – Invokes delete_student_trigger and update_deletestudent trigger which invokes update_dropstudent trigger
	    Input: student sid
    */
    procedure delete_student(sid_in in students.sid%type) is
    begin 
        delete from students where sid = sid_in;
    end;
    end;
/
show errors