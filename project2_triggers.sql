/* Creates a sequence to generate the logid values automatically when a new log record is inserted */
CREATE SEQUENCE logid_seq
START WITH 1
INCREMENT BY 1
MINVALUE 1
MAXVALUE 9999
CACHE 20;

/* Insert into the students table -> trigger inserts a log record for insert on the students table based on sid*/
CREATE OR REPLACE TRIGGER insert_student
    after insert on students
    for each row
    begin
        insert into logs values (logid_seq.NEXTVAL, 'dallen9', CURRENT_TIMESTAMP, 'students', 'insert', 'sid');
    end;
/

/* Delete from the students table -> trigger inserts a log record for deletion on the students table based on sid */
CREATE OR REPLACE TRIGGER delete_student_trigger
    after delete on students
    for each row
    begin
        insert into logs values (logid_seq.NEXTVAL, 'dallen9', CURRENT_TIMESTAMP, 'students', 'delete', 'sid');
    end;
/

/* Delete from the enrollments table -> trigger inserts a log record for deletion on the enrollments table based on sid and classid*/
CREATE OR REPLACE TRIGGER delete_enrollment
    after delete on enrollments
    for each row
    begin
        insert into logs values (logid_seq.NEXTVAL, 'dallen9', CURRENT_TIMESTAMP, 'enrollments', 'delete', '(sid, classid)');
    end;
/

/* Insert into the enrollments table -> trigger inserts a log record for insert on the enrollments table based on sid and classid*/
CREATE OR REPLACE TRIGGER insert_enrollment
    after insert on enrollments
    for each row
    begin
        insert into logs values (logid_seq.NEXTVAL, 'dallen9', CURRENT_TIMESTAMP, 'enrollments', 'insert', '(sid, classid)');
    end;
/

/* Delete from the students table -> trigger deletes all enrollments for the deleted student -> triggers update_dropstudent*/
CREATE OR REPLACE TRIGGER update_deletestudent
    after delete on students
    for each row
    begin
        delete from enrollments where sid = :old.sid;
    end;
/

/* Delete from the enrollments table -> trigger decreases the class size*/
CREATE OR REPLACE TRIGGER update_dropstudent
    after delete on enrollments
    for each row
    begin
        update classes set class_size = class_size - 1 where classid = :old.classid;
    end;
/

/* Insert into the enrollments table -> trigger increases the class size*/
CREATE OR REPLACE TRIGGER update_enrollstudent
    after insert on enrollments
    for each row
    begin
        update classes set class_size = class_size + 1 where classid = :new.classid;
    end;
/