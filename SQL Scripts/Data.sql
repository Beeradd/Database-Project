/* Brady Sprinkle
   CS430
   Data.sql	

professors(prof_ssn, name, age, rank, speciality)
depts(dno, dname, office)
runs(dno, prof_ssn)
work_dept(dno, prof_ssn, pc_time)
projects(pid, sponsor, start_date, end_date, budget)
graduates(grad_ssn, age, name, deg_prog, major)
advisor(senior_ssn, grad_ssn)
manages(pid, prof_ssn)
works_in(pid, prof_ssn)
supervises(prof_ssn, grad_ssn, pid)
courses(c_name, c_ID, c_department, c_instructor, c_enrolled)
enrollment(c_ID, s_name)	
current_grade(c_ID, s_name, grade)
*/

use bsprinkle;
insert into professors(prof_ssn,name,age,rank,speciality) 
values
	('350908221','Nathanael Powers','45','3','Linux'),
	('350908222','Dakota Harvey','55','2','Networking'),
	('350908223','Sumaiya Rowley','42','2','Electrical Engineering'),
	('350908232','Lyle Gilbert','45','3','Mechanical Engineering'),
	('350908224','Lylah Almond','40','2','Physics'),
	('350908225','Liam Fox','45','1','Mathmatics'),
	('350908226','Carlos Mcfarland','38','3','Linguistics'),
	('350908227','Mitchel Archer','58','2','Accounting'),
	('350908228','Hamish Carr','47','3','Biology'),
	('350908229','Adelle Kaufman','39','2','Aviation'),
	('350908233','Duane Munro','48','3','Aviation Mecanic'),
	('350908230','Zeenat Roman','32','4','Politcal Science'),
	('350908231','Enrico Brewer','51','1','Forestry');

insert into depts(dno,dname,office) 
values
	('1','Computer Science','EGRA'),
	('2','Engineering','EGRB'),
	('3','Physical Science','Neckers'),
	('4','Mathmatics','Abbott'),
	('5','Linguistics','Charleston'),
	('6','Business','Doyle'),
	('7','Biological Science','Life Science II'),
	('8','Aviation Technologies','Kaplan'),
	('9','Political Science','Lawson'),
	('10','Agricultural Science','Lusk');

insert into runs(dno,prof_ssn) 
values
	('1','350908222'),
	('2','350908223'),
	('3','350908224'),
	('4','350908225'),
	('5','350908226'),
	('6','350908227'),
	('7','350908228'),
	('8','350908229'),
	('9','350908230'),
	('10','350908231');

insert into work_dept(dno,prof_ssn,pc_time) 
values
	('1','350908221','30'),
	('1','350908222','70'),
	('2','350908223','70'),
	('2','350908232','30'),
	('3','350908224','80'),
	('5','350908226','80'),
	('6','350908227','70'),
	('8','350908229','55'),
	('8','350908233','45'),
	('10','350908231','65');

insert into projects(pid,sponsor,start_date,end_date,budget) 
values
	('100','NSF','2018-01-22','2018-02-25','405.00'),
	('200','QLT','2015-06-13','2015-09-25','307.25'),
	('300','BYU','2018-04-14','2018-10-16','528.88'),
	('400','USD','2015-08-18','2016-01-31','788.12'),
	('500','JGT','2015-05-25','2016-05-01','1000.00'),
	('600','ORP','2018-07-22','2018-12-10','155.70'),
	('700','NBT','2018-08-05','2019-02-27','328.55'),
	('800','QED','2015-02-05','2015-08-10','202.45'),
	('900','RRF','2015-05-25','2015-11-15','100.00'),
	('999','GUT','2018-04-14','2018-12-15','609.48');

insert into graduates(grad_ssn,age,name,deg_prog,major) 
values
	('350908333','22','Jimmy Smith','M.S.','1'),
	('350908334','23','Fatima Sutton','M.S.','1'),
	('350908335','21','Isma George','M.S.','3'),
	('350908336','25','Libbie Smith','Ph.D.','3'),
	('350908337','24','Ace Neal','Ph.D.','6'),
	('350908338','22','Anas Roche','M.S.','6'),
	('350908339','23','Graham Blackwell','M.S.','7'),
	('350908340','26','Nour Kline','Ph.D.','7'),
	('350908341','21','Roxy Chen','M.S.','10'),
	('350908342','23','Jordan Naylor','M.S.','10');

insert into advisor(senior_ssn,grad_ssn) 
values
	('350908334','350908333'),
	('350908336','350908335'),
	('350908337','350908338'),
	('350908340','350908339'),
	('350908342','350908341'),
	('350908336','350908334'),
	('350908337','350908336'),
	('350908340','350908337'),
	('350908341','350908340'),
	('350908339','350908342');
insert into manages(pid,prof_ssn) 
values
	('100','350908222'),
	('200','350908223'),
	('300','350908224'),
	('400','350908226'),
	('500','350908225'),
	('600','350908227'),
	('700','350908228'),
	('800','350908229'),
	('900','350908230'),
	('999','350908231');

insert into works_in(pid,prof_ssn) 
values
	('100','350908222'),
	('100','350908221'),
	('200','350908223'),
	('200','350908232'),
	('300','350908224'),
	('400','350908226'),
	('500','350908225'),
	('600','350908227'),
	('700','350908228'),
	('800','350908229'),
	('800','350908233'),
	('900','350908230'),
	('999','350908231');

insert into supervises(prof_ssn,grad_ssn,pid) 
values
	('350908222','350908334','100'),
	('350908224','350908336','300'),
	('350908227','350908337','600'),
	('350908228','350908340','700'),
	('350908231','350908342','999');
	
insert into courses(c_name,c_ID,c_department,c_instructor,c_enrolled)
values
	('Database','CS430','Computer Science','Nathanael Powers',0),
	('Networking','CS440','Computer Science','Dakota Harvey',0),
	('Electrical Circuits II','ECE336','Engineering','Sumaiya Rowley',0),
	('Thermal Systems Design','ME406','Engineering','Lyle Gilbert',0),
	('Modern Physics','PHYS305','Physical Science','Lylah Almond',0),
	('Linear Algebra','MATH221','Mathmatics','Liam Fox',0),
	('Auditing','ACCT460','Accounting','Mitchel Archer',0),
	('Evolution','BIO304','Biological Science','Hamish Carr',0),
	('The Air Force Today II','AS102','Aviation Technologies','Adelle Kaufman',0),
	('Political Parites','POL319','Political Science','Zeenat Roman',0),
	('Forest Health','FOR314','Agricultural Sciences','Enrico Brewer',0);

