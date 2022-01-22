// usage:  1. compile: javac myinterface.java
//         2. execute: java myinterface
import java.sql.*; 
import oracle.jdbc.*;
import java.util.ArrayList;
import java.io.*;
import java.awt.*;
import oracle.jdbc.pool.OracleDataSource;

public class myinterface {
  /*
    Input: Connection obj
    Task: 
      - Displays every tuple in the six tables: students, courses, prerequistes, classes, enrollments, logs
      - Acquires user input to choose a table
    Output: Prints these tuples
  */
  public static void display(Connection connection) {
    try {
      // Input choice from keyboard
      BufferedReader readKeyBoard; 
      readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
      char option;
      System.out.println("Choose a table to display: ");
      System.out.println("1) Students");
      System.out.println("2) Courses");
      System.out.println("3) Prerequisites");
      System.out.println("4) Classes");
      System.out.println("5) Enrollments");
      System.out.println("6) Logs");
	    System.out.print("Choice: ");
      option = (char)readKeyBoard.read();

      // Set up connection & query
      Connection conn = connection;
      CallableStatement cs;
      switch(option){
        case '1': //Display students 
          cs = conn.prepareCall("begin ? := proj2.getstudents(); end;"); 
          cs.registerOutParameter(1,OracleTypes.CURSOR);
          break;
        case '2': //Display courses
          cs = conn.prepareCall("begin ? := proj2.getcourses(); end;"); 
          cs.registerOutParameter(1,OracleTypes.CURSOR);
          break;
        case '3': //Display prerequisities
          cs = conn.prepareCall("begin ? := proj2.getprereqs(); end;");
          cs.registerOutParameter(1,OracleTypes.CURSOR);
          break;
        case '4': //Display classes
          cs = conn.prepareCall("begin ? := proj2.getclasses(); end;");
          cs.registerOutParameter(1,OracleTypes.CURSOR);
          break;
        case '5': //Display enrollments
          cs = conn.prepareCall("begin ? := proj2.getenrollments(); end;");
          cs.registerOutParameter(1,OracleTypes.CURSOR);
          break;
        case '6': //Display logs
          cs = conn.prepareCall("begin ? := proj2.getlogs(); end;");
          cs.registerOutParameter(1,OracleTypes.CURSOR);
          break;
        default: //Default value --> Exits function call
          cs = conn.prepareCall("");
          System.out.println("Invalid choice");
          return;
      }

      //Execute
      cs.execute();
      //Note: Assumes tables have at least one tuple
      ResultSet rs = (ResultSet)cs.getObject(1); //get the out parameter result.

      int cols = rs.getMetaData().getColumnCount(); //Get num of cols in a table
		
	    int i = 1; //getString count starts at 1
       	// print the results
       	while (rs.next()) {
          while(i <= cols){
            if(i < cols){
              System.out.print(rs.getString(i) + "\t");
            } else { //Last col
              System.out.println(rs.getString(i));
            }
            i++;	
          }
          i = 1; //Reset col count for each row
        }
      //close the statement and the connection
      cs.close();
      conn.close();
    } 
      catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
      catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  /*
    Input: Connection obj
    Task: 
      - Adds a new student to the students table
      - Acquires user input for new student values (sid, firstname, lastname, status, gpa, email)
    Output: Prints insertion or failure message
  */
  public static void insertStudent(Connection connection) {
    try{
      BufferedReader readKeyBoard; 
      readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
      String sid, fname, lname, status, email;
      double gpa;
      Connection conn = connection;

      // Input student values from keyboard
      System.out.println("Inserting a new student. Enter: ");
      System.out.print("SID: ");
      sid = readKeyBoard.readLine();
      System.out.print("First Name: ");
      fname = readKeyBoard.readLine();
      System.out.print("Last Name: ");
      lname = readKeyBoard.readLine();
      System.out.print("Status: ");
      status = readKeyBoard.readLine();
      System.out.print("GPA: ");
      gpa = Double.parseDouble(readKeyBoard.readLine());
      System.out.print("Email: ");
      email = readKeyBoard.readLine();

      //Query add_student takes user input for student vals
      CallableStatement cs = conn.prepareCall("begin add_student(:1,:2,:3,:4,:5,:6); end;");
      cs.setString(1,sid);
      cs.setString(2,fname);
      cs.setString(3,lname);
      cs.setString(4,status);
      cs.setDouble(5,gpa);
      cs.setString(6,email);

      cs.executeQuery(); //Execute

      //close the statement and the connection. Print out acknowledgment
      cs.close();
      conn.close();
      System.out.println("Successfully inserted new student.");
    }
	  catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
    catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  /*
    Input: Connection obj
    Task: 
      - Enrolls a pre-existing student into a class
      - Acquires user input to find the student & the class (sid, classid)
    Output: Prints insertion or failure message
  */
  public static void enrollStudent(Connection connection) {
    try{
      BufferedReader readKeyBoard; 
      readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
      String classid, sid;
      Connection conn = connection;

      //Input sid and classid from keyboard
      System.out.println("Choose a student & class");
      System.out.print("Student ID: ");
      sid = readKeyBoard.readLine();
      System.out.print("Class ID: ");
      classid = readKeyBoard.readLine();

      //Check if CLASSID is valid
	    Statement stmt = conn.createStatement();
	    String cidValid = "SELECT * from classes where classid='" + classid + "'";
	    ResultSet rset = stmt.executeQuery(cidValid);

      //Check if SID is valid
      Statement stmt2 = conn.createStatement();
      String sidValid = "SELECT * from students where sid='" + sid + "'";
      ResultSet rset2 = stmt2.executeQuery(sidValid);

      if(!(rset.next())){ //classid invalid
        System.out.println("invalid classid.");
      } else if(!(rset2.next())){ //sid invalid
        System.out.println("sid not found.");
      } else { //Student and class is valid
        int flag = 0; //Flag to check validity of enrolling student; 0 - valid, 1 - invalid
        //Check if enrollment is valid
        Statement stmt3 = conn.createStatement();
        String eValid = "SELECT * from enrollments where sid='" + sid + "' and classid='" + classid + "'";
        ResultSet rset3 = stmt3.executeQuery(eValid);

        if(rset3.next()){
          System.out.println("already in this class");
          flag = 1;
        }

        if(flag != 1){
          //Count num of classes in same year & semester
          Statement stmt4 = conn.createStatement();
          String count = "SELECT count(year), count(semester) from enrollments natural join classes where sid='" + sid + "' and (year, semester) in (select year, semester from classes where classid='" + classid + "') group by year, semester";
          ResultSet rset4 = stmt4.executeQuery(count);
          if(rset4.next()){
            if(rset4.getInt(1) >= 4){ //Already taking four courses in that semester & year
              System.out.println("overloaded!");
              flag = 1;
            }  
          }
          //close the result set and statement
          stmt4.close();
          rset4.close();
        }

        if(flag != 1){
          //Check class_size < limit
          Statement stmt5 = conn.createStatement();
          String sizeLimit = "SELECT class_size, limit from classes where classid='" + classid + "'";
          ResultSet rset5 = stmt5.executeQuery(sizeLimit);
          if(rset5.next()){
            if(rset5.getInt(1) + 1 > rset5.getInt(2)){ //class size + 1 > limit --> class is already at cap
              System.out.println("class full");
              flag = 1;
            }  
          }
          //close the result set and statement
          stmt5.close();
          rset5.close();
        }
        
        if(flag != 1){
          //Check if prerequisites fulfilled
          //Get dept_code and course_no from classid
          Statement stmt6 = conn.createStatement();
          String deptCodeNum = "SELECT dept_code, course_no from classes natural join courses where classid='" + classid + "'";
          ResultSet rset6 = stmt6.executeQuery(deptCodeNum);

          if(!rset6.next()){
            return;
          } 
          //Get prereqs for class
          CallableStatement cs = conn.prepareCall("begin ? := proj2.getprereqs(?,?); end;");
          cs.registerOutParameter(1, OracleTypes.CURSOR);
          cs.setString(2, rset6.getString(1));
          cs.setInt(3, rset6.getInt(2));

          cs.execute();
          ResultSet rs = (ResultSet)cs.getObject(1);
          
          //Check if prerequisites were taken and got at least a C'
          while(rs.next()){
            Statement stmt7 = conn.createStatement();
            String grade = "SELECT lgrade from enrollments natural join classes natural join courses where lgrade <= 'C' and sid='" + sid + "' and dept_code='" + rs.getString(1) + "' and course_no=" + rs.getInt(2);
            ResultSet rset7 = stmt7.executeQuery(grade);
            if(!rset7.next()){
              System.out.println("Prequisite courses have not been completed");
              flag = 1;
              //close the result set and statement
              stmt7.close();
              rset7.close();
              break;
            }
            //close the result set and statement
            stmt7.close();
            rset7.close();
          }
          //close the result sets and statement
          stmt6.close();
          rset6.close();
          rs.close();
        }

        if(flag != 1){
          CallableStatement cs = conn.prepareCall("begin enroll_student(:1,:2); end;");
          cs.setString(1, sid);
          cs.setString(2, classid);
          cs.executeQuery();
          System.out.println("Enrolled student " + sid + " into " + classid + " successfully!");
          //close the statement
          cs.close();
        }
        //close the result set and statement
        stmt3.close();
        rset3.close();
      }

      //close the result set, statement, and the connection
      stmt.close();
      rset.close();
      stmt2.close();
      rset2.close();
      conn.close();
    }
	  catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
    catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  /*
    Input: Connection obj
    Task: 
      - Deletes a student from the student table
      - Acquires user input to find the student (sid)
    Output: Prints deletion or failure message (ex: sid not found)
  */
  public static void deleteStudent(Connection connection) {
    try {
        BufferedReader readKeyBoard; 
        readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
        String sid;
        Connection conn = connection;

        // Input sid from keyboard
        System.out.println("Choose a student");
        System.out.print("Student ID: ");
        sid = readKeyBoard.readLine();

        //Check if SID is valid
        Statement stmt = conn.createStatement();
        String sidValid = "SELECT * from students where sid='" + sid + "'";
        ResultSet rset = stmt.executeQuery(sidValid);

        if(!(rset.next())){ //SID invalid
            System.out.println("sid not found.");
        } else { //SID is valid
          CallableStatement cs = conn.prepareCall("begin delete_student(:1); end;");
          cs.setString(1, sid);
          cs.executeQuery();
          System.out.println("Deleted student " + sid + " successfully!");
        }

        // Close connection, result set, and statements
        rset.close();
        stmt.close();
        conn.close();
    }
    catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
    catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  /*
    Input: Connection obj
    Task: 
      - Drop a student from a class
      - Acquires user input to find the student & the class (sid, classid)
    Output: Prints deletion or failure message
  */
  public static void dropStudent(Connection connection) {
    try{
        BufferedReader readKeyBoard; 
        readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
        String classid, sid;
        Connection conn = connection;

        //Input sid and classid from keyboard
        System.out.println("Choose a student & class");
        System.out.print("Student ID: ");
        sid = readKeyBoard.readLine();
        System.out.print("Class ID: ");
        classid = readKeyBoard.readLine();

        //Check if CLASSID is valid
	    Statement stmt = conn.createStatement();
	    String cidValid = "SELECT * from classes where classid='" + classid + "'";
	    ResultSet rset = stmt.executeQuery(cidValid);

        //Check if SID is valid
        Statement stmt2 = conn.createStatement();
        String sidValid = "SELECT * from students where sid='" + sid + "'";
        ResultSet rset2 = stmt2.executeQuery(sidValid);

        if(!(rset.next())){
            System.out.println("classid not found.");
        } else if(!(rset2.next())){
            System.out.println("sid not found.");
        } else {//Student and class is valid
            int flag = 0;
            //Check if enrollment is valid
            Statement stmt3 = conn.createStatement();
            String eValid = "SELECT * from enrollments where sid='" + sid + "' and classid='" + classid + "'";
            ResultSet rset3 = stmt3.executeQuery(eValid);

            //Check if enrolled in this class
            if(!rset3.next()){
                System.out.println("student not enrolled in this class");
                flag = 1;
            }

            //Check for prereq conflicts
            if(flag != 1){
              //Get dept_code and course_no from classid
              Statement stmt4 = conn.createStatement();
              String deptCodeNum = "SELECT dept_code, course_no from classes natural join courses where classid='" + classid + "'";
              ResultSet rset4 = stmt4.executeQuery(deptCodeNum);

              if(!rset4.next()){
                return;
              } 
              //Check if student is enrolled in a course that depends on the course being dropped
              Statement stmt5 = conn.createStatement();
              String enrollDepend = "(SELECT dept_code, course_no from prerequisites where pre_dept_code='" + rset4.getString(1) + "' and pre_course_no=" + rset4.getInt(2) + ") intersect (select dept_code, course_no from enrollments natural join classes where sid ='" + sid + "')";
              ResultSet rset5 = stmt5.executeQuery(enrollDepend);

              if(rset5.next()){
                System.out.println("drop request rejected due to prerequisite requirements");
                flag = 1;
              }
              stmt4.close();
              rset4.close();
              stmt5.close();
              rset5.close();
            }

            //Drop student
            if(flag != 1){
                CallableStatement cs = conn.prepareCall("begin drop_student(:1,:2); end;");
                cs.setString(1, sid);
                cs.setString(2, classid);
                cs.executeQuery();
                System.out.println("Dropped student " + sid + " from " + classid + " successfully!");

                //Check if this is the last class for student
                Statement stmt4 = conn.createStatement();
                String lastClassValid = "SELECT * from enrollments where sid='" + sid + "'";
                ResultSet rset4 = stmt4.executeQuery(lastClassValid);
                if(!rset4.next())
                    System.out.println("student enrolled in no class");
                
                //Check if this is the last student in the class
                Statement stmt5 = conn.createStatement();
                String lastStudentValid = "SELECT class_size from classes where classid='" + classid + "'";
                ResultSet rset5 = stmt5.executeQuery(lastStudentValid);
                if(rset5.next()){
                    if(rset5.getInt(1) == 0)
                        System.out.println("no student in this class");
                }
                //close the result sets and statements
                stmt4.close();
                rset4.close();
                stmt5.close();
                rset5.close();
                cs.close();
            }
            stmt3.close();
            rset3.close();
        }
        //close the result set, statement, and the connection
        stmt.close();
        rset.close();
        stmt2.close();
        rset2.close();
        conn.close();
    }
    catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
    catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  /*
    Input: Connection obj
    Task: 
      - View a student's information (sid, firstname, lastname, gpa, classes (currently & previously taken))
      - Acquires user input to find the student (sid)
    Output: Prints student info or a failure message
  */
  public static void viewStudentInfo(Connection connection) {
    try{
      BufferedReader readKeyBoard; 
      readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
      String sid;
      Connection conn = connection;

      // Input SID from keyboard
      System.out.println("Choose a student");
      System.out.print("SID: ");
      sid = readKeyBoard.readLine();

      //Check if SID is valid
	    Statement stmt = conn.createStatement();
	    String sidValid = "SELECT * from students where sid='" + sid + "'";
	    ResultSet rset = stmt.executeQuery(sidValid);

      if(!rset.next()){ //SID invalid
        System.out.println("The sid is invalid.");
      } else {
        //Check if student has been enrolled in any courses
        String eValid = "SELECT * from enrollments where sid='" + sid + "'";
        Statement stmt2 = conn.createStatement();
        ResultSet rset2 = stmt2.executeQuery(eValid);

        if(!rset2.next()){ //SID is valid && Student isn't enrolled in any courses --> Print student info & student hasn't taken any courses
          String student = "SELECT firstname, lastname, gpa from students where sid='" + sid + "'";
          Statement stmt3 = conn.createStatement();
          ResultSet rset3 = stmt.executeQuery(student);
    	
          if(rset3.next()){
            System.out.println(sid + "\t" + rset3.getString(1) + "\t" + rset3.getString(2) + "\t" + rset3.getString(3));
            System.out.println("The student has not taken any course");
          }
          //close the result set and statement
          stmt3.close();
          rset3.close();
        } else { // SID is valid & student has courses
          CallableStatement cs = conn.prepareCall("begin ? := proj2.getstudentInfo(?); end;");
          cs.registerOutParameter(1, OracleTypes.CURSOR);
          cs.setString(2,sid);

          cs.execute();
          ResultSet rs = (ResultSet)cs.getObject(1);

          if(rs.next()){ //Prints out student info & then the courses they've taken
            System.out.println(sid + "\t"+ rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3)); //sid, firstname, lastname, gpa
            System.out.println(rs.getString(4) + "\t" + rs.getString(5) + "\t" + rs.getString(6) + "\t" + rs.getString(7)); //first class -- classid, (dept_code, course_no), semester, year
              
            while(rs.next()){
              System.out.println(rs.getString(4) + "\t" + rs.getString(5) + "\t" + rs.getString(6) + "\t" + rs.getString(7)); //other classes
            }
          }   
          //close the result set and statement
          cs.close();  
          rs.close();
        }
        //close the result set and statement
        stmt2.close();
        rset2.close(); 
      }

      //close the result set, statement, and the connection
      stmt.close();
      rset.close();
      conn.close();
    }
	  catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
    catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  /*
    Input: Connection obj
    Task: 
      - Views all course prerequistes (direct & indirect)
      - Acquires user input to find the class (dept_code, course#)
    Output: Prints dept_code, course#, and course title or failure message
  */
  public static void viewCoursePrereq(Connection connection) {
  	try{
      BufferedReader readKeyBoard; 
      readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
      String dept, pre_dept;
      int num, pre_num;
      Connection conn = connection;

      // Input dept_code and course_no from keyboard
      System.out.println("Choose a course");
      System.out.print("Department Code: ");
      dept = readKeyBoard.readLine();
      dept = dept.toUpperCase();
      System.out.print("Course Number: ");
      num = Integer.parseInt(readKeyBoard.readLine());

      // Calls procedure to get all prereqs
      CallableStatement cs = conn.prepareCall("begin ? := proj2.getprereqs(?,?); end;");
      cs.registerOutParameter(1, OracleTypes.CURSOR);
      cs.setString(2, dept);
      cs.setInt(3, num);

      cs.execute();
      ResultSet rs = (ResultSet)cs.getObject(1);
      while(rs.next()){ //Prints out course info
        System.out.println(rs.getString(1) + rs.getInt(2) + "\t" + rs.getString(3)); //dept_code + course_no, title
      }

      //close the result set, statement, and the connection
      cs.close();	
      rs.close();
      conn.close();	
    }
	  catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
    catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  /*
    Input: Connection obj
    Task: 
      - Views all information about a class (classid, course title, semester, year of the class, and current & previous students)
      - Acquires user input to find the class (classid)
    Output: Prints class info or a failure message
  */
  public static void viewClassInfo(Connection connection) {
    try{
      BufferedReader readKeyBoard; 
      readKeyBoard = new BufferedReader(new InputStreamReader(System.in));
      String classid;
      Connection conn = connection;

      // Input classid from keyboard
      System.out.println("Choose a class");
      System.out.print("Class ID: ");
      classid = readKeyBoard.readLine();

      //Check if CLASSID is valid
	    Statement stmt = conn.createStatement();
	    String idValid = "SELECT * from classes where classid='" + classid + "'";
	    ResultSet rset = stmt.executeQuery(idValid);

      if(!rset.next()){ //Invalid classid
        System.out.println("The cid is invalid.");
        //close the statement and the result set
        stmt.close();
        rset.close();
      } else {
        //Check if any student has been enrolled
        String eValid = "SELECT * from enrollments where classid='" + classid + "'";
        Statement stmt2 = conn.createStatement();
        ResultSet rset2 = stmt2.executeQuery(eValid);

        if(!rset2.next()){ //Classid is valid && no student is enrolled in this course
          String student = "SELECT title, semester, year from classes cl join courses c on cl.dept_code = c.dept_code and cl.course_no = c.course_no and classid='" + classid + "'";
          Statement stmt3 = conn.createStatement();
          ResultSet rset3 = stmt.executeQuery(student);
    	  
          if(rset3.next()){ 
            System.out.println(classid + "\t" + rset3.getString(1) + "\t" + rset3.getString(2) + "\t" + rset3.getString(3)); //classid, title, semester, year
            System.out.println("No student is enrolled in the class");
          }
          //close the statement and the result set
          stmt3.close();
          rset3.close();
        } else { // classid is valid & student has courses
          CallableStatement cs = conn.prepareCall("begin ? := proj2.getclassInfo(?); end;");
          cs.registerOutParameter(1, OracleTypes.CURSOR);
          cs.setString(2,classid);
        
          cs.execute();
          ResultSet rs = (ResultSet)cs.getObject(1);

          if(rs.next()){ //Prints out class info & then the students that have taken it
            System.out.println(classid + "\t"+ rs.getString(1) + "\t" + rs.getString(2) + "\t" + rs.getString(3)); //classid, course title, semester, year
            System.out.println(rs.getString(4) + "\t" + rs.getString(5) + "\t" + rs.getString(6) + "\t" + rs.getString(7)); //sid, firstname, lastname, email
              
            while(rs.next()){
              System.out.println(rs.getString(4) + "\t" + rs.getString(5) + "\t" + rs.getString(6) + "\t" + rs.getString(7));
            }
          }   
          //close the statement and the result set
          cs.close(); 
          rs.close();    
        }
        stmt2.close();
        rset2.close(); 
      }

      //close the connection
      conn.close();
    }
	  catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n" + ex.getMessage());}
    catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }

  public static void main (String args []) throws SQLException {
    try
    {

      //Connection to Oracle server
      OracleDataSource ds = new oracle.jdbc.pool.OracleDataSource();
      ds.setURL("jdbc:oracle:thin:@castor.cc.binghamton.edu:1521:ACAD111");
      Connection conn = ds.getConnection("dallen9", "succAriJaune12");

      // Input choice from keyboard
      BufferedReader readKeyBoard; 
      char option;
      readKeyBoard = new BufferedReader(new InputStreamReader(System.in)); 
      System.out.println("Choose from below: ");
      System.out.println("1) Display a table ");
      System.out.println("2) Insert a new student ");
      System.out.println("3) Enroll a student into class ");
      System.out.println("4) Delete a student from the table ");
      System.out.println("5) Drop a student from a class ");
      System.out.println("6) View a student's information ");
      System.out.println("7) View a course's prerequisites ");
      System.out.println("8) View a class's information ");
      System.out.print("Enter a number: ");

      option = (char)readKeyBoard.read();
      System.out.println("Option: " + option);
      // Go to corresponding function for the user's choice
      switch(option){
        case '1': 
          display(conn);
          break;
        case '2': 
          insertStudent(conn);
          break;
        case '3': 
          enrollStudent(conn);
          break;
        case '4': 
          deleteStudent(conn);
          break;
        case '5': 
          dropStudent(conn);
          break;
        case '6': 
          viewStudentInfo(conn);
          break;
        case '7': 
          viewCoursePrereq(conn);
          break;
        case '8': 
          viewClassInfo(conn);
          break;
      }
        //close the result set, statement, and the connection
        conn.close();
    } 
     catch (SQLException ex) { System.out.println ("\n*** SQLException caught ***\n");}
     catch (Exception e) {System.out.println ("\n*** other Exception caught ***\n");}
  }
} 

