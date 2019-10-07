***Important***
The MYSQL implementation in the project is broken, as I no longer have access to my student account for the database servers. 
If you would like to see what the project looked like originally, please see the video on my website: /*url goes here*/
***************

Brady Sprinkle
CS 430
Database Project
11/27/2018

The folder "GUI Project" is the eclipse project file, opening the file inside of eclipse should load all necessary files. The three classes, GUI.java, ConnectSQL.java, AlertBox.java are the only classes I made for the project and should all be in the src folder for inspection.

I developed the project using Linux ubuntu 18.10 on a virtual machine, as well as the mysql environment. The referenced library version is 5.1.47 in order to be compatible with the school's mysql server version.

Schema.sql is the schema of my database system, and Data.sql is the initial data that populates the database. Feel free to utilize the Data.sql file to test the functionality of the program. Some functions do not work as intended as well as some relations not being used at all, and will be mentioned later. 

Scenes:

-Login Scene
The login screen has three access points; admin, staff and student. Each access requires specific login information in order to access each features. To login as an admin use the password "admin". To login as a staff member, a staff's SSN is required; see Data.sql to get a SSN to use or try this one '350908221'. To login as a student, a student's SSN is required; see Data.sql to get a SSN to use or try this one '350908333'. All scenes may be logged out of, and you will be returned to the login scene. 

-Admin Scene
The admin's functionality comes from the different tabs; Add, Update, Delete, and Search. The add function adds new entries to the different tables in the database. Each table is split into Accordion Panes, with each table's attributes listed inside. Fill in each attribute with desired values and click submit to add the entry; a success/failure message will appear depending on the result of the addition. The format of the elements does matter, and are indicated by the prompt text. Please see the Schema.sql or Data.sql for examples on proper entries. Update is similar to add except it updates a previous entry in the table. The first two columns are the attribute you are updating along with its new value, and the last two columns are the old attribute and value. You cannot update multiple attributes at once, hence the radio button choice. The delete tab deletes table entries based on the attribute and value. You cannot delete multiple attributes at once, similar to update. The search tab returns data from the tables into the output textarea. It operates very similar to command line mysql use. The Output isn't formatted very well though, so reading large amounts of data isn't user-friendly. 

-Staff Scene
As per the rules of the project and data retrieval issues, the staff scene only consists of search functionality and it operates the exact same as the admin's search function. I wanted to do more with this, however had a hard time passing certain data from the database to the GUI and formatting user input in a way mysql could recognize (for example distinguishing from integers and strings provided by a user).

-Student Scene
The student scene has the same search functionality as the other two scenes, with another tab for enrolling in courses. Each course offered is listed along with its course ID and instructor. A student may select the courses they want and enroll in them by clicking the enroll button. This updates the enrolled count for each class, while also adding new entries to the enrollment table. Unfortunately the constraint never worked on an enrollment limit, so the count isn't limited like requested. There is also no limit on the number of courses a student can be enrolled in due to time constraints and GUI limitations. 


Each class has plenty of comments for descriptions of functions however if more information is needed, please feel free to contact me. 


