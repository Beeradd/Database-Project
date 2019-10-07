/*Brady Sprinkle
  CS430 Project
  Schema.sql script
*/
use bsprinkle;

CREATE TABLE professors( 
			prof_ssn CHAR(10),
			name	  CHAR(64),
			age 	  CHAR(10),
			rank	  CHAR(10),
			speciality CHAR(64),
			PRIMARY KEY(prof_ssn));



CREATE TABLE depts(
			dno	 CHAR(64),
			dname	 CHAR(64),
			office CHAR(10),
			PRIMARY KEY (dno));


CREATE TABLE runs(	
			dno	 CHAR(64),
			prof_ssn CHAR(10),
			PRIMARY KEY (dno, prof_ssn),
			FOREIGN KEY (prof_ssn) REFERENCES professors,
			FOREIGN KEY (dno)      REFERENCES depts);


CREATE TABLE work_dept( 
			dno	 CHAR(64),
			prof_ssn CHAR(10),
			pc_time  CHAR(10),
			PRIMARY KEY (dno, prof_ssn),
			FOREIGN KEY (prof_ssn) REFERENCES professors,
			FOREIGN KEY (dno)      REFERENCES depts);
 
CREATE TABLE projects( 	
			pid	  CHAR(10),
			sponsor CHAR(32),
			start_date DATE,
			end_date DATE,
			budget CHAR(10),
			PRIMARY KEY (pid));

CREATE TABLE graduates( 
			grad_ssn CHAR(10),
			age 	 CHAR(10),
			name	 CHAR(64),
			deg_prog CHAR(32),
			major	 CHAR(10),
			PRIMARY KEY (grad_ssn),
			FOREIGN KEY (major) REFERENCES depts);

CREATE TABLE advisor(   
			senior_ssn CHAR(10),
		 	grad_ssn   CHAR(10),
			PRIMARY KEY (senior_ssn, grad_ssn),
			FOREIGN KEY (senior_ssn) REFERENCES graduates(grad_ssn),
			FOREIGN KEY (grad_ssn)   REFERENCES graduates);

CREATE TABLE manages( 	
			pid	CHAR(10),
			prof_ssn CHAR(10),
			PRIMARY KEY (pid, prof_ssn),
			FOREIGN KEY (prof_ssn) REFERENCES professors,
			FOREIGN KEY (pid)      REFERENCES projects);

CREATE TABLE works_in( 	
			pid 	CHAR(10),
			prof_ssn CHAR(10),
			PRIMARY KEY (pid, prof_ssn),
			FOREIGN KEY (prof_ssn) REFERENCES professors,
			FOREIGN KEY (pid)      REFERENCES projects);

CREATE TABLE supervises( 
			prof_ssn CHAR(10),
			grad_ssn CHAR(10),
			pid	  CHAR(10),
			PRIMARY KEY (prof_ssn,grad_ssn,pid),
			FOREIGN KEY (prof_ssn) REFERENCES professors,
			FOREIGN KEY (grad_ssn) REFERENCES graduates,
			FOREIGN KEY (pid)      REFERENCES projects); 

CREATE TABLE courses(
			c_name CHAR(64),
			c_ID   CHAR(10),
			c_department CHAR(64),
			c_instructor CHAR(64),
			c_enrolled INTEGER,
			PRIMARY KEY (c_name,c_ID,c_department,c_instructor),
			FOREIGN KEY (c_department) REFERENCES depts(dname),
			FOREIGN KEY (c_instructor) REFERENCES professors(name),
			check (c_enrolled<=15));

CREATE TABLE enrollment(
			c_ID CHAR(10),
			c_name CHAR(64),
			s_name CHAR(64),
			PRIMARY KEY (c_ID,c_name,s_name),
			FOREIGN KEY (c_ID) REFERENCES courses(c_ID),
			FOREIGN KEY (c_name) REFERENCES courses(c_name),
			FOREIGN KEY (s_name) REFERENCES graduates(name));

CREATE TABLE current_grade(
			c_ID   CHAR(10),
			c_name CHAR(64),
			s_name CHAR(64),
			grade  CHAR(6),
			PRIMARY KEY (c_ID,c_name,s_name,grade),
			FOREIGN KEY (c_ID) REFERENCES courses(c_ID),
			FOREIGN KEY (c_name) REFERENCES courses(c_name),
			FOREIGN KEY (s_name) REFERENCES graduates(name));
