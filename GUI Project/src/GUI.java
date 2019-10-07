//	Brady Sprinkle
//	CS 430
//	School Search Database Project: GUI

//GUI.java implements all GUI components, communicates with mysql server, holds main()
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.*;


public class GUI extends Application 
{
	ConnectSQL conn = new ConnectSQL();

	Stage window;
	Scene login_scene, staff_scene, student_scene, admin_scene;
	
	//Login Components
	Label warning_lbl = new Label();
	Label logged_on = new Label();
	
	Button submit_btn;
	PasswordField id_login;							//SSN associated with admin, staff, or student
	ComboBox<String> login_cb = new ComboBox<>();	//Select between admin, staff, or student. Determines program capabillity, as well as id_login acceptor
	RadioButton modify_rb = new RadioButton(); 		//Whether add,update,delete is selected
	
	//For capturing ResltSets from mysql queries
	ResultSet results = null;
	ResultSetMetaData rsmd = null;
	
	
	
	//Admin_Staff Components
	Button add_btn, delete_btn, update_btn, search_btn, submit2_btn, logout_btn;
	TextArea input_txt, output_txt;
	Label input_lbl, func_lbl, output_lbl, info_lbl;
	final String[] relations = {"professors(prof_ssn,name,age,rank,speciality)",
								 "depts (dno, dname, office)",
								 "runs (dno, prof_ssn)",
								 "work_dept (dno, prof_ssn, pc_time)", 
								 "projects (pid, sponsor, start_date, end_date, budget)", 
								 "graduates (grad_ssn, age, name, deg_prog, major)", 
								 "advisor (senior_ssn, grad_ssn)", 
								 "manages (pid, prof_ssn)", 
								 "works_in (pid, prof_ssn)",
								 "supervises (prof_ssn, grad_ssn, pid)",
								 "courses (c_name, c_ID, c_department, c_instructor, c_enrolled)", 
								 "enrollment (c_ID, c_name, s_name)", 
								 "current_grade (c_ID, s_name, grade)"};

	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		window = primaryStage;
		window.setTitle("School Search"); 
	
		//Login layout
		BorderPane outline = new BorderPane();
		outline.setCenter(addGridPane());
		login_scene = new Scene(outline, 650, 480);
			
		//Student layoutv1.0
		BorderPane student_outline = new BorderPane();
		TabPane tp_student = addTabPane();
		
		student_outline.setTop(tp_student);
		student_outline.setBottom(addAnchorPane());
		student_scene = new Scene(student_outline, 825, 575);
		
		//Admin layout v2.0
		BorderPane admin_outline = new BorderPane();
		TabPane tp_admin = addTabPane_a();
		admin_outline.setCenter(tp_admin);
		admin_outline.setBottom(addAnchorPane());
		admin_scene = new Scene(admin_outline,650,480);
		
		//Staff layout v1.0
		BorderPane staff_outline = new BorderPane();
		TabPane tp_staff = addTabPane_s();
		staff_outline.setCenter(tp_staff);
		staff_outline.setBottom(addAnchorPane());
		staff_scene = new Scene(staff_outline,650,480);
		
		window.setScene(student_scene);

		window.setResizable(false);	//Cheap fix for scalability
		window.setMinWidth(650);
		window.setMinHeight(480);
		
		
		
		window.show();
	}
//***Login Scene***\\
	public GridPane addGridPane() 
	{
		GridPane grid = new GridPane();
		
		grid.setHgap(35);
		grid.setVgap(10);
		grid.setPadding(new Insets(10,35,35,35));
		
		grid.setAlignment(Pos.CENTER);
		
		//All of the components within the GridPane
		id_login = new PasswordField();
		submit_btn = new Button("Submit");
		
		//ComboBox attributes
		login_cb.getItems().addAll("Admin", "Staff", "Student");
		login_cb.setMaxWidth(Double.MAX_VALUE);
		login_cb.setMinWidth(Control.USE_PREF_SIZE);
		login_cb.setPromptText("Who are you?");

		//Depending on who logs in, adjust system
		grid.add(login_cb, 0, 2);
		
		id_login.setPrefWidth(125);
		id_login.setPromptText("ID Number");
		
		grid.add(id_login, 1, 2);
		
		//Submit Button Attribute
		submit_btn.disableProperty().bind(login_cb.valueProperty().isNull());

		submit_btn.setMaxWidth(Double.MAX_VALUE);
		submit_btn.setMinWidth(Control.USE_PREF_SIZE);
		submit_btn.setOnAction(e -> 
		{
			try 
			{
				loginCheck();
		
			} catch (Exception e1) {
				
				e1.printStackTrace();
			}
		});	
		
		grid.add(submit_btn, 1, 3);
		
		//Warning Label if needed
		warning_lbl.setText("");
		grid.add(warning_lbl, 1, 4);
		
		
		return grid;
	}
	
//***Admin Scene***\\
	public TabPane addTabPane_a() throws Exception
	{
		TabPane tabpane = new TabPane();
		
		Tab add = new Tab("Add");
		add.setContent(addLayout());
		Tab update = new Tab("Update");
		update.setContent(updateLayout());
		Tab delete = new Tab("Delete");
		delete.setContent(deleteLayout());
		Tab search = new Tab("Search");
		search.setContent(searchLayout());
		
		tabpane.getTabs().addAll(add,update,delete,search);
		tabpane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		
		
		return tabpane;
	}
	//Admin add layout
	public Accordion addLayout() throws Exception
	{
		
		final double vgap = 5;
		final double hgap = 5;
		
		BorderPane prof_border = new BorderPane();
		VBox prof_submit_vbox = new VBox();
		
		TitledPane prof = new TitledPane();
		prof.setText("Professors");
		
		GridPane prof_grid = new GridPane();
		prof_grid.setVgap(vgap);
		prof_grid.setHgap(hgap);
		prof_grid.setPadding(new Insets(10,10,0,10));

		Label prof_ssnlbl = new Label("Prof SSN");
		prof_grid.add(prof_ssnlbl, 0, 0);
		TextField prof_ssn = new TextField();
		prof_ssn.setPromptText("000000000");
		prof_grid.add(prof_ssn, 1, 0);
		
		Label namelbl = new Label("Name");
		prof_grid.add(namelbl, 0, 1);
		TextField name= new TextField();
		name.setPromptText("John Smith");
		prof_grid.add(name, 1, 1);
		
		Label agelbl = new Label("Age");
		prof_grid.add(agelbl, 0, 2);
		TextField age = new TextField();
		age.setPromptText("45");
		prof_grid.add(age, 1, 2);
		
		Label ranklbl = new Label("Rank");
		prof_grid.add(ranklbl, 0, 3);
		TextField rank = new TextField();
		rank.setPromptText("1-5");
		prof_grid.add(rank, 1, 3);
		
		Label focuslbl = new Label("Speciality");
		prof_grid.add(focuslbl, 0, 4);
		TextField focus = new TextField();
		focus.setPromptText("Linux Systems");
		prof_grid.add(focus, 1, 4);
		
		Label prof_successlbl = new Label();
		prof_successlbl.setVisible(false);

		Button submit_prof = new Button("Submit");
		
		//Disables the submit button until all textfields have something in them
		BooleanBinding areEmpty = prof_ssn.textProperty().isEmpty().or(name.textProperty().isEmpty()).or(age.textProperty().isEmpty()).or(rank.textProperty().isEmpty()).or(focus.textProperty().isEmpty());
		submit_prof.disableProperty().bind(areEmpty);
		
		TextField[] prof_tfs = {prof_ssn, name, age, rank, focus};
		submit_prof.setOnAction(e -> 
					{

						try
						{
							if(conn.insert(collectData(prof_tfs), relations[0]))
							{
								prof_successlbl.setVisible(true);
								prof_successlbl.setText("Your entry was added succesfully");
								prof_successlbl.setTextFill(Color.GREEN);
							}
							
						}
						catch(Exception e2)
						{
							AlertBox.printError("Insert Error", "Error inserting data: \n" + e2);
							System.err.println("Error adding professor data: "+ e2);

						}
						
						for(int i = 0; i<prof_tfs.length;i++)
						{
							prof_tfs[i].clear();
						}
						});
		


		prof_submit_vbox.setSpacing(10);
		prof_submit_vbox.setPadding(new Insets(10));
		prof_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		prof_submit_vbox.getChildren().addAll(prof_successlbl,submit_prof);
		
		
		prof_border.setLeft(prof_grid);
		prof_border.setRight(prof_submit_vbox);
		prof.setContent(prof_border);
	//****************************************\\	
		
		TitledPane depts = new TitledPane();
		depts.setText("Departments");
		
		BorderPane depts_border = new BorderPane();
		VBox depts_submit_vbox = new VBox();
		
		GridPane depts_grid = new GridPane();
		depts_grid.setVgap(10);
		depts_grid.setHgap(10);
		depts_grid.setPadding(new Insets(5));
		
		Label dnolbl = new Label("Dept. Number");
		depts_grid.add(dnolbl, 0, 0);
		TextField dno = new TextField();
		dno.setPromptText("1-99");
		depts_grid.add(dno, 1, 0);
		
		Label dnamelbl = new Label("Dept. Name");
		depts_grid.add(dnamelbl, 0, 1);
		TextField dname = new TextField();
		dname.setPromptText("Computer Science");
		depts_grid.add(dname, 1, 1);
		
		Label officelbl = new Label("Office");
		depts_grid.add(officelbl, 0, 2);
		TextField office = new TextField();
		office.setPromptText("EGRA");
		depts_grid.add(office, 1, 2);
		
		Label depts_successlbl = new Label();
		depts_successlbl.setVisible(false);
		
		Button submit_dept = new Button("Submit");
		
		//Disables the submit button until all textfields have something in them
		BooleanBinding areEmpty_2 = dno.textProperty().isEmpty().or(dname.textProperty().isEmpty()).or(office.textProperty().isEmpty());
		submit_dept.disableProperty().bind(areEmpty_2);
		
		TextField[] depts_tfs = {dno, dname, office};
		
		submit_dept.setOnAction(e -> 
		{

			try
			{
				if(conn.insert(collectData(depts_tfs), relations[1]))
				{
					depts_successlbl.setVisible(true);
					depts_successlbl.setText("Your entry was added succesfully");
					depts_successlbl.setTextFill(Color.GREEN);
				}
								
			}
			catch(Exception e2)
			{
				AlertBox.printError("Insert Error", "Error inserting data: \n" + e2);
				System.err.println("Error adding department data: "+ e2);

			}
							
			for(int i = 0; i<depts_tfs.length;i++)
			{
				depts_tfs[i].clear();
			}
		});


		depts_submit_vbox.setSpacing(10);
		depts_submit_vbox.setPadding(new Insets(10));
		depts_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);

		depts_submit_vbox.getChildren().addAll(depts_successlbl,submit_dept);
		
		
		depts_border.setLeft(depts_grid);
		depts_border.setRight(depts_submit_vbox);
		
		depts.setContent(depts_border);
	//******************************************\\
	
		TitledPane projects = new TitledPane();
		projects.setText("Projects");
		GridPane projects_grid = new GridPane();
		projects_grid.setVgap(10);
		projects_grid.setHgap(10);
		projects_grid.setPadding(new Insets(5));
		
		BorderPane projects_border = new BorderPane();
		VBox projects_submit_vbox = new VBox();
		
		//projects(pid, sponsor, start_date, end_date, budget)
		Label pidlbl = new Label("PID");
		projects_grid.add(pidlbl, 0, 0);
		TextField pid = new TextField();
		pid.setPromptText("000-999");
		projects_grid.add(pid, 1, 0);
		
		Label sponsorlbl = new Label("Sponsor");
		projects_grid.add(sponsorlbl, 0, 1);
		TextField sponsor = new TextField();
		sponsor.setPromptText("CFS");
		projects_grid.add(sponsor, 1, 1);
		
		Label startlbl = new Label("Start Date");
		projects_grid.add(startlbl, 0, 2);
		TextField start = new TextField();
		start.setPromptText("YYYY-MM-DD"); 
		projects_grid.add(start, 1, 2);
		
		Label endlbl = new Label("End Date");
		projects_grid.add(endlbl, 0, 3);
		TextField end = new TextField();
		end.setPromptText("YYYY-MM-DD");
		projects_grid.add(end, 1, 3);
		
		Label budgetlbl = new Label("Budget");
		projects_grid.add(budgetlbl, 0, 4);
		TextField budget = new TextField();
		budget.setPromptText("150.56");
		projects_grid.add(budget, 1, 4);
		
		Label projects_successlbl = new Label();
		projects_successlbl.setVisible(false);
		
		
		Button submit_project = new Button("Submit");
		
		//Disables the submit button until all textfields have something in them
		BooleanBinding areEmpty_3 = pid.textProperty().isEmpty().or(sponsor.textProperty().isEmpty()).or(start.textProperty().isEmpty()).or(end.textProperty().isEmpty()).or(budget.textProperty().isEmpty());
		submit_project.disableProperty().bind(areEmpty_3);
		
		TextField[] projects_tfs = {pid, sponsor, start, end, budget};
		
		submit_project.setOnAction(e -> 
					{
						try
						{
							if(conn.insert(collectData(projects_tfs), relations[4]))
							{
								projects_successlbl.setVisible(true);
								projects_successlbl.setText("Your entry was added succesfully");
								projects_successlbl.setTextFill(Color.GREEN);
							}
											
						}
						catch(Exception e2)
						{
							AlertBox.printError("Insert Error", "Error inserting data: \n" + e2);
							System.err.println("Error adding project data: "+ e2);

						}
										
						for(int i = 0; i<projects_tfs.length;i++)
						{
							projects_tfs[i].clear();
						}
					});
		

		projects_submit_vbox.setSpacing(10);
		projects_submit_vbox.setPadding(new Insets(10));
		projects_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);

		projects_submit_vbox.getChildren().addAll(projects_successlbl,submit_project);
		
	
		projects_border.setLeft(projects_grid);
		projects_border.setRight(projects_submit_vbox);
		
		projects.setContent(projects_border);
		//******************************************\\
		
		TitledPane students = new TitledPane();
		students.setText("Graduates");
		GridPane students_grid = new GridPane();
		students_grid.setVgap(10);
		students_grid.setHgap(10);
		students_grid.setPadding(new Insets(5));
		
		BorderPane students_border = new BorderPane();
		VBox students_submit_vbox = new VBox();
		
		//graduates(grad_ssn, age, name, deg_prog, major)
		Label grad_ssnlbl = new Label("Graduate SSN");
		students_grid.add(grad_ssnlbl, 0, 0);
		TextField grad_ssn = new TextField();
		grad_ssn.setPromptText("000000000");
		students_grid.add(grad_ssn, 1, 0);
		
		Label s_agelbl = new Label("Age");
		students_grid.add(s_agelbl, 0, 1);
		TextField s_age = new TextField(); 
		s_age.setPromptText("22");
		students_grid.add(s_age, 1, 1);
		
		Label s_namelbl = new Label("Name");
		students_grid.add(s_namelbl, 0, 2);
		TextField s_name = new TextField();
		s_name.setPromptText("Jimmy Smith");
		students_grid.add(s_name, 1, 2);
		
		Label degreelbl = new Label("Degree Program");
		students_grid.add(degreelbl, 0, 3);
		TextField degree = new TextField();
		degree.setPromptText("M.S. or Ph.D.");
		students_grid.add(degree, 1, 3);
		
		Label majorlbl = new Label("Major");
		students_grid.add(majorlbl, 0, 4);
		TextField major = new TextField();
		major.setPromptText("Department Number");
		students_grid.add(major, 1, 4);
		
		Label students_successlbl = new Label();
		students_successlbl.setVisible(false);
		
		Button s_submit = new Button("Submit");
		
		//Disables the submit button until all textfields have something in them
		BooleanBinding areEmpty_4 = grad_ssn.textProperty().isEmpty().or(s_age.textProperty().isEmpty()).or(s_name.textProperty().isEmpty()).or(degree.textProperty().isEmpty()).or(major.textProperty().isEmpty());
		s_submit.disableProperty().bind(areEmpty_4);
		
		TextField[] students_tfs = {grad_ssn, s_age, s_name, degree, major};
		
		s_submit.setOnAction(e -> 
					{
						try
						{
							if(conn.insert(collectData(students_tfs), relations[5]))
							{
								students_successlbl.setVisible(true);
								students_successlbl.setText("Your entry was added succesfully");
								students_successlbl.setTextFill(Color.GREEN);
							}
											
						}
						catch(Exception e2)
						{
							AlertBox.printError("Insert Error", "Error inserting data: \n" + e2);
							System.err.println("Error adding project data: "+ e2);

						}
										
						for(int i = 0; i<students_tfs.length;i++)
						{
							students_tfs[i].clear();
						}
					});
		
		

		students_submit_vbox.setSpacing(10);
		students_submit_vbox.setPadding(new Insets(10));
		students_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		students_submit_vbox.getChildren().addAll(students_successlbl, s_submit);
		
		
		students_border.setLeft(students_grid);
		students_border.setRight(students_submit_vbox);
		
		students.setContent(students_border);
		//******************************************\\
		
		TitledPane courses = new TitledPane();
		courses.setText("Courses");
		GridPane courses_grid = new GridPane();
		courses_grid.setVgap(10);
		courses_grid.setHgap(10);
		courses_grid.setPadding(new Insets(5));
		
		BorderPane courses_border = new BorderPane();
		VBox courses_submit_vbox = new VBox();
		
		//courses(c_name, c_ID, c_department, c_instructor, c_enrolled)
		Label c_namelbl = new Label("Name");
		courses_grid.add(c_namelbl, 0, 0);
		TextField c_name = new TextField();
		c_name.setPromptText("Database Management");
		courses_grid.add(c_name, 1, 0);
		
		Label c_idlbl = new Label("ID");
		courses_grid.add(c_idlbl, 0, 1);
		TextField c_id = new TextField();
		c_id.setPromptText("CS430");
		courses_grid.add(c_id, 1, 1);
		
		Label c_deptlbl = new Label("Department");
		courses_grid.add(c_deptlbl, 0, 2);
		TextField c_dept = new TextField();
		c_dept.setPromptText("Computer Science");
		courses_grid.add(c_dept, 1, 2);
		
		Label c_proflbl = new Label("Instructor");
		courses_grid.add(c_proflbl, 0, 3);
		TextField c_prof = new TextField();
		c_prof.setPromptText("John Smith");
		courses_grid.add(c_prof, 1, 3);
		
		Label c_enrolledlbl = new Label("Enrolled");
		courses_grid.add(c_enrolledlbl, 0, 4); 
		TextField c_enroll = new TextField("");
		c_enroll.setEditable(false);
		c_enroll.setPromptText("This is currently broken, do not submit");
		courses_grid.add(c_enroll, 1, 4); 
		
		Label courses_successlbl = new Label();
		courses_successlbl.setVisible(false);
		
		Button c_submit = new Button("Submit");		
		
		//Disables the submit button until all textfields have something in them
		BooleanBinding areEmpty_5 = c_name.textProperty().isEmpty().or(c_id.textProperty().isEmpty()).or(c_dept.textProperty().isEmpty()).or(c_prof.textProperty().isEmpty());
		c_submit.disableProperty().bind(areEmpty_5);
		
		TextField[] courses_tfs = {c_name, c_id, c_dept, c_prof, c_enroll};
		
		c_submit.setOnAction(e -> 
					{
						try
						{
							if(conn.insert(collectData(courses_tfs), relations[10]))
							{
								courses_successlbl.setVisible(true);
								courses_successlbl.setText("Your entry was added succesfully");
								courses_successlbl.setTextFill(Color.GREEN);
							}
											
						}
						catch(Exception e2)
						{
							AlertBox.printError("Insert Error", "Error inserting data: \n" + e2);
							System.err.println("Error adding project data: "+ e2);

						}
										
						for(int i = 0; i<courses_tfs.length;i++)
						{
							courses_tfs[i].clear();
						}
					});
		
		
		courses_submit_vbox.setSpacing(10);
		courses_submit_vbox.setPadding(new Insets(10));
		courses_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		courses_submit_vbox.getChildren().addAll(courses_successlbl,c_submit);
		
		courses_border.setLeft(courses_grid);
		courses_border.setRight(courses_submit_vbox);
		
		courses.setContent(courses_border);
		
		Accordion ac = new Accordion();
		ac.setMaxHeight(100);
		ac.getPanes().addAll(prof,depts,projects,students,courses);
		
		return ac;
	}
	
	//Admin update layout
	public Accordion updateLayout() throws Exception
	{
		final double vgap = 10;
		final double hgap = 10;
		
		TitledPane prof = new TitledPane();
		prof.setText("Professors");
		
		BorderPane prof_border = new BorderPane();
		VBox prof_submit_vbox = new VBox();
		HBox prof_title_hbox = new HBox();
		
		GridPane prof_grid = new GridPane();
		prof_grid.setVgap(vgap);
		prof_grid.setHgap(hgap); 
		prof_grid.setPadding(new Insets(10));
		
		ToggleGroup prof_setGroup = new ToggleGroup();
		ToggleGroup prof_whereGroup = new ToggleGroup();

		RadioButton prof_ssnrb = new RadioButton("Prof SSN");
		prof_ssnrb.setToggleGroup(prof_setGroup); 
		prof_ssnrb.setUserData("prof_ssn");
		prof_grid.add(prof_ssnrb, 0, 0); 
		RadioButton prof_ssnrb_where = new RadioButton("Prof SSN");
		prof_ssnrb_where.setToggleGroup(prof_whereGroup); 
		prof_ssnrb_where.setUserData("prof_ssn");
		prof_grid.add(prof_ssnrb_where, 5, 0); 
		TextField prof_ssntf = new TextField();
		prof_ssntf.setMaxWidth(130);
		prof_ssntf.setVisible(false);
		prof_grid.add(prof_ssntf, 3, 0); 
		TextField prof_ssntf_where = new TextField();
		prof_ssntf_where.setMaxWidth(130);
		prof_ssntf_where.setVisible(false);
		prof_grid.add(prof_ssntf_where, 6, 0);
		
		RadioButton prof_namerb = new RadioButton("Name");
		prof_namerb.setToggleGroup(prof_setGroup); 
		prof_namerb.setUserData("name");
		prof_grid.add(prof_namerb, 0, 1); 
		RadioButton prof_namerb_where = new RadioButton("Name");
		prof_namerb_where.setToggleGroup(prof_whereGroup); 
		prof_namerb_where.setUserData("name");
		prof_grid.add(prof_namerb_where, 5, 1); 
		TextField prof_nametf = new TextField();
		prof_nametf.setMaxWidth(130);
		prof_nametf.setVisible(false);
		prof_grid.add(prof_nametf, 3, 1); 
		TextField prof_nametf_where = new TextField();
		prof_nametf_where.setMaxWidth(130);
		prof_nametf_where.setVisible(false);
		prof_grid.add(prof_nametf_where, 6, 1);

		RadioButton prof_agerb = new RadioButton("Age");
		prof_agerb.setToggleGroup(prof_setGroup);
		prof_agerb.setUserData("age");
		prof_grid.add(prof_agerb, 0, 2); 
		RadioButton prof_agerb_where = new RadioButton("Age");
		prof_agerb_where.setToggleGroup(prof_whereGroup); 
		prof_agerb_where.setUserData("age");
		prof_grid.add(prof_agerb_where, 5, 2); 
		TextField prof_agetf = new TextField();
		prof_agetf.setMaxWidth(130);
		prof_agetf.setVisible(false);
		prof_grid.add(prof_agetf, 3, 2); 
		TextField prof_agetf_where = new TextField();
		prof_agetf_where.setMaxWidth(130);
		prof_agetf_where.setVisible(false);
		prof_grid.add(prof_agetf_where, 6, 2);

		RadioButton prof_rankrb = new RadioButton("Rank");
		prof_rankrb.setToggleGroup(prof_setGroup); 
		prof_rankrb.setUserData("rank");
		prof_grid.add(prof_rankrb, 0, 3); 
		RadioButton prof_rankrb_where = new RadioButton("Rank");
		prof_rankrb_where.setToggleGroup(prof_whereGroup); 
		prof_rankrb_where.setUserData("rank");
		prof_grid.add(prof_rankrb_where, 5, 3); 
		TextField prof_ranktf = new TextField();
		prof_ranktf.setMaxWidth(130);
		prof_ranktf.setVisible(false);
		prof_grid.add(prof_ranktf, 3, 3); 
		TextField prof_ranktf_where = new TextField();
		prof_ranktf_where.setMaxWidth(130);
		prof_ranktf_where.setVisible(false);
		prof_grid.add(prof_ranktf_where, 6, 3);

		RadioButton prof_focusrb = new RadioButton("Speciality");
		prof_focusrb.setToggleGroup(prof_setGroup); 
		prof_focusrb.setUserData("speciality");
		prof_grid.add(prof_focusrb, 0, 4); 
		RadioButton prof_focusrb_where = new RadioButton("Speciality");
		prof_focusrb_where.setToggleGroup(prof_whereGroup); 
		prof_focusrb_where.setUserData("speciality");
		prof_grid.add(prof_focusrb_where, 5, 4); 
		TextField prof_focustf = new TextField();
		prof_focustf.setMaxWidth(130);
		prof_focustf.setVisible(false);
		prof_grid.add(prof_focustf, 3, 4); 
		TextField prof_focustf_where = new TextField();
		prof_focustf_where.setMaxWidth(130);
		prof_focustf_where.setVisible(false);
		prof_grid.add(prof_focustf_where, 6, 4);
		
		Label prof_successlbl = new Label();
		prof_successlbl.setVisible(false);
		
		TextField[] set_tf = {prof_ssntf, prof_nametf, prof_agetf, prof_ranktf, prof_focustf};
		TextField[] where_tf = {prof_ssntf_where, prof_nametf_where, prof_agetf_where, prof_ranktf_where, prof_focustf_where};
		
		//When a radio button is selected, enable the proper textfield to use
		//This prevents multiple textfields being editied at once
		//"Set" group first
		prof_setGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
				{
					public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
					{
						if(prof_setGroup.getSelectedToggle() != null)
						{
							if(prof_setGroup.getSelectedToggle() == prof_ssnrb)
							{
								prof_ssntf.setVisible(true);
								prof_nametf.setVisible(false);
								prof_agetf.setVisible(false);
								prof_ranktf.setVisible(false);
								prof_focustf.setVisible(false);
		
							}
							else if(prof_setGroup.getSelectedToggle() == prof_namerb)
							{
								prof_ssntf.setVisible(false);
								prof_nametf.setVisible(true);
								prof_agetf.setVisible(false);
								prof_ranktf.setVisible(false);
								prof_focustf.setVisible(false);
								
							}
							else if(prof_setGroup.getSelectedToggle() == prof_agerb)
							{
								prof_ssntf.setVisible(false);
								prof_nametf.setVisible(false);
								prof_agetf.setVisible(true);
								prof_ranktf.setVisible(false);
								prof_focustf.setVisible(false);
							}
							else if(prof_setGroup.getSelectedToggle() == prof_rankrb)
							{
								prof_ssntf.setVisible(false);
								prof_nametf.setVisible(false);
								prof_agetf.setVisible(false);
								prof_ranktf.setVisible(true);
								prof_focustf.setVisible(false);
							}
							else if(prof_setGroup.getSelectedToggle() == prof_focusrb)
							{
								prof_ssntf.setVisible(false);
								prof_nametf.setVisible(false);
								prof_agetf.setVisible(false);
								prof_ranktf.setVisible(false);
								prof_focustf.setVisible(true);
							}
						}
					}
			
				});
		//"where" group second
		prof_whereGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
				{
					public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
					{
						if(prof_whereGroup.getSelectedToggle() != null)
						{
							if(prof_whereGroup.getSelectedToggle() == prof_ssnrb_where)
							{
								prof_ssntf_where.setVisible(true);
								prof_nametf_where.setVisible(false);
								prof_agetf_where.setVisible(false);
								prof_ranktf_where.setVisible(false);
								prof_focustf_where.setVisible(false);
							}
							else if(prof_whereGroup.getSelectedToggle() == prof_namerb_where)
							{
								prof_ssntf_where.setVisible(false);
								prof_nametf_where.setVisible(true);
								prof_agetf_where.setVisible(false);
								prof_ranktf_where.setVisible(false);
								prof_focustf_where.setVisible(false);
								
							}
							else if(prof_whereGroup.getSelectedToggle() == prof_agerb_where)
							{
								prof_ssntf_where.setVisible(false);
								prof_nametf_where.setVisible(false);
								prof_agetf_where.setVisible(true);
								prof_ranktf_where.setVisible(false);
								prof_focustf_where.setVisible(false);
							}
							else if(prof_whereGroup.getSelectedToggle() == prof_rankrb_where)
							{
								prof_ssntf_where.setVisible(false);
								prof_nametf_where.setVisible(false);
								prof_agetf_where.setVisible(false);
								prof_ranktf_where.setVisible(true);
								prof_focustf_where.setVisible(false);
							}
							else if(prof_whereGroup.getSelectedToggle() == prof_focusrb_where)
							{
								prof_ssntf_where.setVisible(false);
								prof_nametf_where.setVisible(false);
								prof_agetf_where.setVisible(false);
								prof_ranktf_where.setVisible(false);
								prof_focustf_where.setVisible(true);
							}
						}
					
					}
				});
		
		
		
		Button submit_prof = new Button("Submit");
		submit_prof.setOnAction(e-> 
								{
									RadioButton set_chk = (RadioButton)prof_setGroup.getSelectedToggle();
									RadioButton where_chk = (RadioButton)prof_whereGroup.getSelectedToggle();
								
									String set = set_chk.getUserData().toString();
									String where = where_chk.getUserData().toString();

								try
									{
										if(conn.update("professors", set, where, updateInput(set_tf), updateInput(where_tf)))
										{
											prof_successlbl.setVisible(true);
											prof_successlbl.setText("Success!");
											prof_successlbl.setTextFill(Color.GREEN);
										}
									}
									catch(Exception e2)
									{
										
									}
								for(int i = 0; i < set_tf.length; i++)
								{
									set_tf[i].clear();
								}
								for(int i = 0; i < where_tf.length; i++)
								{
									where_tf[i].clear();
								}
								});
		
		
		Label setAtts = new Label("Set Attribute");
		setAtts.setUnderline(true);
		Label newVal = new Label("New Value");
		newVal.setUnderline(true);
		Label whereAtts = new Label("Where Attribute");
		whereAtts.setUnderline(true);
		Label oldVal = new Label("Old Value");
		oldVal.setUnderline(true);
		
		prof_title_hbox.setSpacing(50);
		prof_title_hbox.setPadding(new Insets(10));
		prof_title_hbox.getChildren().addAll(setAtts,newVal,whereAtts,oldVal);
		
		prof_submit_vbox.setSpacing(10);
		prof_submit_vbox.setPadding(new Insets(10));
		prof_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		prof_submit_vbox.getChildren().addAll(prof_successlbl, submit_prof);
		
		prof_border.setTop(prof_title_hbox);
		prof_border.setLeft(prof_grid);
		prof_border.setRight(prof_submit_vbox);
		prof.setContent(prof_border);
		
//*****************************************\\		
		TitledPane depts = new TitledPane();
		depts.setText("Departments");
		
		BorderPane depts_border = new BorderPane();
		VBox depts_submit_vbox = new VBox();
		HBox depts_title_hbox = new HBox();
		
		GridPane depts_grid = new GridPane();
		depts_grid.setVgap(vgap);
		depts_grid.setHgap(hgap); 
		depts_grid.setPadding(new Insets(10));
		
		ToggleGroup depts_setGroup = new ToggleGroup();
		ToggleGroup depts_whereGroup = new ToggleGroup();
		
		RadioButton dnorb = new RadioButton("Dept. No.");
		dnorb.setToggleGroup(depts_setGroup); 
		dnorb.setUserData("dno");
		depts_grid.add(dnorb, 0, 0); 
		RadioButton dnorb_where = new RadioButton("Dept. No.");
		dnorb_where.setToggleGroup(depts_whereGroup); 
		dnorb_where.setUserData("dno");
		depts_grid.add(dnorb_where, 3, 0); 
		TextField dnotf = new TextField();
		dnotf.setMaxWidth(130);
		dnotf.setVisible(false);
		depts_grid.add(dnotf, 1, 0); 
		TextField dnotf_where = new TextField();
		dnotf_where.setMaxWidth(130);
		dnotf_where.setVisible(false);
		depts_grid.add(dnotf_where, 4, 0);
		
		RadioButton dnamerb = new RadioButton("Dept. Name");
		dnamerb.setToggleGroup(depts_setGroup); 
		dnorb.setUserData("dname");
		depts_grid.add(dnamerb, 0, 1); 
		RadioButton dnamerb_where = new RadioButton("Dept. Name");
		dnamerb_where.setToggleGroup(depts_whereGroup); 
		dnamerb_where.setUserData("dname");
		depts_grid.add(dnamerb_where, 3, 1); 
		TextField dnametf = new TextField();
		dnametf.setMaxWidth(130);
		dnametf.setVisible(false);
		depts_grid.add(dnametf, 1, 1); 
		TextField dnametf_where = new TextField();
		dnametf_where.setMaxWidth(130);
		dnametf_where.setVisible(false);
		depts_grid.add(dnametf_where, 4, 1);
		
		RadioButton officerb = new RadioButton("Office");
		officerb.setToggleGroup(depts_setGroup); 
		officerb.setUserData("office");
		depts_grid.add(officerb, 0, 2); 
		RadioButton officerb_where = new RadioButton("Office");
		officerb_where.setToggleGroup(depts_whereGroup); 
		officerb_where.setUserData("office");
		depts_grid.add(officerb_where, 3, 2); 
		TextField officetf = new TextField();
		officetf.setMaxWidth(130);
		officetf.setVisible(false);
		depts_grid.add(officetf, 1, 2); 
		TextField officetf_where = new TextField();
		officetf_where.setMaxWidth(130);
		officetf_where.setVisible(false);
		depts_grid.add(officetf_where, 4, 2);
		
		TextField[] depts_set_tf = {dnotf,dnametf,officetf};
		TextField[] depts_where_tf = {dnotf_where,dnametf_where,officetf_where};
		//When a radio button is selected, enable the proper textfield to use
		//This prevents multiple textfields being editied at once
		//"Set" group first
		depts_setGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
				{
					public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
					{
						if(depts_setGroup.getSelectedToggle() != null)
						{
							if(depts_setGroup.getSelectedToggle() == dnorb)
							{
								dnotf.setVisible(true);
								dnametf.setVisible(false);
								officetf.setVisible(false);
		
							}
							else if(depts_setGroup.getSelectedToggle() == dnamerb)
							{
								dnotf.setVisible(false);
								dnametf.setVisible(true);
								officetf.setVisible(false);
								
							}
							else if(depts_setGroup.getSelectedToggle() == officerb)
							{
								dnotf.setVisible(false);
								dnametf.setVisible(false);
								officetf.setVisible(true);
							}
						}
					}
			
				});
		//"where" group second
		depts_whereGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(depts_whereGroup.getSelectedToggle() != null)
				{
					if(depts_whereGroup.getSelectedToggle() == dnorb_where)
					{
						dnotf_where.setVisible(true);
						dnametf_where.setVisible(false);
						officetf_where.setVisible(false);

					}
					else if(depts_whereGroup.getSelectedToggle() == dnamerb_where)
					{
						dnotf_where.setVisible(false);
						dnametf_where.setVisible(true);
						officetf_where.setVisible(false);
						
					}
					else if(depts_whereGroup.getSelectedToggle() == officerb_where)
					{
						dnotf_where.setVisible(false);
						dnametf_where.setVisible(false);
						officetf_where.setVisible(true);
					}
				}
			}
	
		});
		
		Label depts_successlbl = new Label();
		depts_successlbl.setVisible(false);
		
		Button submit_depts = new Button("Submit");
		submit_depts.setOnAction(e-> 
								{
									RadioButton set_chk = (RadioButton)depts_setGroup.getSelectedToggle();
									RadioButton where_chk = (RadioButton)depts_whereGroup.getSelectedToggle();
								
									String set = set_chk.getUserData().toString();
									String where = where_chk.getUserData().toString();
								try
									{
										if(conn.update("depts", set, where, updateInput(depts_set_tf), updateInput(depts_where_tf)))
										{
											depts_successlbl.setVisible(true);
											depts_successlbl.setText("Success!");
											depts_successlbl.setTextFill(Color.GREEN);
										}
									}
									catch(Exception e2)
									{
										
									}
								for(int i = 0; i < depts_set_tf.length; i++)
								{
									depts_set_tf[i].clear();
								}
								for(int i = 0; i < where_tf.length; i++)
								{
									depts_where_tf[i].clear();
								}
								});
		
		Label depts_setAtts = new Label("Set Attribute");
		depts_setAtts.setUnderline(true);
		Label depts_newVal = new Label("New Value");
		depts_newVal.setUnderline(true);
		Label depts_whereAtts = new Label("Where Attribute");
		depts_whereAtts.setUnderline(true);
		Label depts_oldVal = new Label("Old Value");
		depts_oldVal.setUnderline(true);
		
		depts_title_hbox.setSpacing(50);
		depts_title_hbox.setPadding(new Insets(10));
		depts_title_hbox.getChildren().addAll(depts_setAtts,depts_newVal,depts_whereAtts,depts_oldVal);
		
		depts_submit_vbox.setSpacing(10);
		depts_submit_vbox.setPadding(new Insets(10));
		depts_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		depts_submit_vbox.getChildren().addAll(depts_successlbl,submit_depts);
		
		depts_border.setTop(depts_title_hbox);
		depts_border.setLeft(depts_grid);
		depts_border.setRight(depts_submit_vbox);
		depts.setContent(depts_border);
		
//*****************************************\\			
		TitledPane projects = new TitledPane();
		projects.setText("Projects");
		
		BorderPane projects_border = new BorderPane();
		VBox projects_submit_vbox = new VBox();
		HBox projects_title_hbox = new HBox();
		
		GridPane projects_grid = new GridPane();
		projects_grid.setVgap(vgap);
		projects_grid.setHgap(hgap); 
		projects_grid.setPadding(new Insets(10));
		
		ToggleGroup projects_setGroup = new ToggleGroup();
		ToggleGroup projects_whereGroup = new ToggleGroup();
		
		RadioButton pidrb = new RadioButton("PID");
		pidrb.setToggleGroup(projects_setGroup); 
		pidrb.setUserData("pid");
		projects_grid.add(pidrb, 0, 0); 
		RadioButton pidrb_where = new RadioButton("PID");
		pidrb_where.setToggleGroup(projects_whereGroup); 
		pidrb_where.setUserData("pid");
		projects_grid.add(pidrb_where, 3, 0); 
		TextField pidtf = new TextField();
		pidtf.setMaxWidth(130);
		pidtf.setVisible(false);
		projects_grid.add(pidtf, 1, 0); 
		TextField pidtf_where = new TextField();
		pidtf_where.setMaxWidth(130);
		pidtf_where.setVisible(false);
		projects_grid.add(pidtf_where, 4, 0);
		
		RadioButton sponsorrb = new RadioButton("Sponsor");
		sponsorrb.setToggleGroup(projects_setGroup); 
		sponsorrb.setUserData("sponsor");
		projects_grid.add(sponsorrb, 0, 1); 
		RadioButton sponsorrb_where = new RadioButton("Sponsor");
		sponsorrb_where.setToggleGroup(projects_whereGroup); 
		sponsorrb_where.setUserData("sponsor");
		projects_grid.add(sponsorrb_where, 3, 1); 
		TextField sponsortf = new TextField();
		sponsortf.setMaxWidth(130);
		sponsortf.setVisible(false);
		projects_grid.add(sponsortf, 1, 1); 
		TextField sponsortf_where = new TextField();
		sponsortf_where.setMaxWidth(130);
		sponsortf_where.setVisible(false);
		projects_grid.add(sponsortf_where, 4, 1);
		
		RadioButton startrb = new RadioButton("Start Date");
		startrb.setToggleGroup(projects_setGroup); 
		startrb.setUserData("start_date");
		projects_grid.add(startrb, 0, 2); 
		RadioButton startrb_where = new RadioButton("Start Date");
		startrb_where.setToggleGroup(projects_whereGroup); 
		startrb_where.setUserData("start_date");
		projects_grid.add(startrb_where, 3, 2); 
		TextField starttf = new TextField();
		starttf.setMaxWidth(130);
		starttf.setVisible(false);
		projects_grid.add(starttf, 1, 2); 
		TextField starttf_where = new TextField();
		starttf_where.setMaxWidth(130);
		starttf_where.setVisible(false);
		projects_grid.add(starttf_where, 4, 2);
		
		RadioButton endrb = new RadioButton("End Date");
		endrb.setToggleGroup(projects_setGroup); 
		endrb.setUserData("end_date");
		projects_grid.add(endrb, 0, 3); 
		RadioButton endrb_where = new RadioButton("End Date");
		endrb_where.setToggleGroup(projects_whereGroup); 
		endrb_where.setUserData("end_date");
		projects_grid.add(endrb_where, 3, 3); 
		TextField endtf = new TextField();
		endtf.setMaxWidth(130);
		endtf.setVisible(false);
		projects_grid.add(endtf, 1, 3); 
		TextField endtf_where = new TextField();
		endtf_where.setMaxWidth(130);
		endtf_where.setVisible(false);
		projects_grid.add(endtf_where, 4, 3);
		
		RadioButton budgetrb = new RadioButton("Budget");
		budgetrb.setToggleGroup(projects_setGroup); 
		budgetrb.setUserData("budget");
		projects_grid.add(budgetrb, 0, 4); 
		RadioButton budgetrb_where = new RadioButton("Budget");
		budgetrb_where.setToggleGroup(projects_whereGroup); 
		budgetrb_where.setUserData("budget");
		projects_grid.add(budgetrb_where, 3, 4); 
		TextField budgettf = new TextField();
		budgettf.setMaxWidth(130);
		budgettf.setVisible(false);
		projects_grid.add(budgettf, 1, 4); 
		TextField budgettf_where = new TextField();
		budgettf_where.setMaxWidth(130);
		budgettf_where.setVisible(false);
		projects_grid.add(budgettf_where, 4, 4);
		
		TextField[] projects_set_tf = {pidtf,sponsortf,starttf,endtf,budgettf};
		TextField[] projects_where_tf = {pidtf_where,sponsortf_where,starttf_where,endtf_where,budgettf_where};
		//When a radio button is selected, enable the proper textfield to use
		//This prevents multiple textfields being editied at once
		//"Set" group first
		projects_setGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
				{
					public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
					{
						if(projects_setGroup.getSelectedToggle() != null)
						{
							if(projects_setGroup.getSelectedToggle() == pidrb)
							{
								pidtf.setVisible(true);
								sponsortf.setVisible(false);
								starttf.setVisible(false);
								endtf.setVisible(false);
								budgettf.setVisible(false);
		
							}
							else if(projects_setGroup.getSelectedToggle() == sponsorrb)
							{
								pidtf.setVisible(false);
								sponsortf.setVisible(true);
								starttf.setVisible(false);
								endtf.setVisible(false);
								budgettf.setVisible(false);
								
							}
							else if(projects_setGroup.getSelectedToggle() == startrb)
							{
								pidtf.setVisible(false);
								sponsortf.setVisible(false);
								starttf.setVisible(true);
								endtf.setVisible(false);
								budgettf.setVisible(false);
							}
							else if(projects_setGroup.getSelectedToggle() == endrb)
							{
								pidtf.setVisible(false);
								sponsortf.setVisible(false);
								starttf.setVisible(false);
								endtf.setVisible(true);
								budgettf.setVisible(false);
							}
							else if(projects_setGroup.getSelectedToggle() == budgetrb)
							{
								pidtf.setVisible(false);
								sponsortf.setVisible(false);
								starttf.setVisible(false);
								endtf.setVisible(false);
								budgettf.setVisible(true);
							}
						}
					}
			
				});
		//"where" group second
		projects_whereGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(projects_whereGroup.getSelectedToggle() != null)
				{
					if(projects_whereGroup.getSelectedToggle() == pidrb_where)
					{
						pidtf_where.setVisible(true);
						sponsortf_where.setVisible(false);
						starttf_where.setVisible(false);
						endtf_where.setVisible(false);
						budgettf_where.setVisible(false);

					}
					else if(projects_whereGroup.getSelectedToggle() == sponsorrb_where)
					{
						pidtf_where.setVisible(false);
						sponsortf_where.setVisible(true);
						starttf_where.setVisible(false);
						endtf_where.setVisible(false);
						budgettf_where.setVisible(false);
						
					}
					else if(projects_whereGroup.getSelectedToggle() == startrb_where)
					{
						pidtf_where.setVisible(false);
						sponsortf_where.setVisible(false);
						starttf_where.setVisible(true);
						endtf_where.setVisible(false);
						budgettf_where.setVisible(false);
					}
					else if(projects_whereGroup.getSelectedToggle() == endrb_where)
					{
						pidtf_where.setVisible(false);
						sponsortf_where.setVisible(false);
						starttf_where.setVisible(false);
						endtf_where.setVisible(true);
						budgettf_where.setVisible(false);
					}
					else if(projects_whereGroup.getSelectedToggle() == budgetrb_where)
					{
						pidtf_where.setVisible(false);
						sponsortf_where.setVisible(false);
						starttf_where.setVisible(false);
						endtf_where.setVisible(false);
						budgettf_where.setVisible(true);
					}
				}
			}
	
		});
		
		Label projects_successlbl = new Label();
		projects_successlbl.setVisible(false);
		
		Button submit_projects = new Button("Submit");
		submit_projects.setOnAction(e-> 
								{
									RadioButton set_chk = (RadioButton)projects_setGroup.getSelectedToggle();
									RadioButton where_chk = (RadioButton)projects_whereGroup.getSelectedToggle();
								
									String set = set_chk.getUserData().toString();
									String where = where_chk.getUserData().toString();
								try
									{
										if(conn.update("projects", set, where, updateInput(projects_set_tf), updateInput(projects_where_tf)))
										{
											projects_successlbl.setVisible(true);
											projects_successlbl.setText("Success!");
											projects_successlbl.setTextFill(Color.GREEN);
										}
									}
									catch(Exception e2)
									{
										
									}
								for(int i = 0; i < projects_set_tf.length; i++)
								{
									projects_set_tf[i].clear();
								}
								for(int i = 0; i < projects_where_tf.length; i++)
								{
									projects_where_tf[i].clear();
								}
								});
		
		
		
		Label projects_setAtts = new Label("Set Attribute");
		projects_setAtts.setUnderline(true);
		Label projects_newVal = new Label("New Value");
		projects_newVal.setUnderline(true);
		Label projects_whereAtts = new Label("Where Attribute");
		projects_whereAtts.setUnderline(true);
		Label projects_oldVal = new Label("Old Value");
		projects_oldVal.setUnderline(true);
		
		projects_title_hbox.setSpacing(50);
		projects_title_hbox.setPadding(new Insets(10));
		projects_title_hbox.getChildren().addAll(projects_setAtts,projects_newVal,projects_whereAtts,projects_oldVal);
		
		projects_submit_vbox.setSpacing(10);
		projects_submit_vbox.setPadding(new Insets(10));
		projects_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		projects_submit_vbox.getChildren().addAll(projects_successlbl,submit_projects);
		
		projects_border.setTop(projects_title_hbox);
		projects_border.setLeft(projects_grid);
		projects_border.setRight(projects_submit_vbox);
		projects.setContent(projects_border);
//*****************************************\\	
		TitledPane students = new TitledPane();
		students.setText("Graduates");
		

		BorderPane students_border = new BorderPane();
		VBox students_submit_vbox = new VBox();
		HBox students_title_hbox = new HBox();
		
		GridPane students_grid = new GridPane();
		students_grid.setVgap(vgap);
		students_grid.setHgap(hgap); 
		students_grid.setPadding(new Insets(10));
		
		ToggleGroup students_setGroup = new ToggleGroup();
		ToggleGroup students_whereGroup = new ToggleGroup();
		
		RadioButton grad_ssnrb = new RadioButton("Grad. SSN");
		grad_ssnrb.setToggleGroup(students_setGroup); 
		grad_ssnrb.setUserData("grad_ssn");
		students_grid.add(grad_ssnrb, 0, 0); 
		RadioButton grad_ssnrb_where = new RadioButton("Grad. SSN");
		grad_ssnrb_where.setToggleGroup(students_whereGroup); 
		grad_ssnrb_where.setUserData("grad_ssn");
		students_grid.add(grad_ssnrb_where, 3, 0); 
		TextField grad_ssntf = new TextField();
		grad_ssntf.setMaxWidth(130);
		grad_ssntf.setVisible(false);
		students_grid.add(grad_ssntf, 1, 0); 
		TextField grad_ssntf_where = new TextField();
		grad_ssntf_where.setMaxWidth(130);
		grad_ssntf_where.setVisible(false);
		students_grid.add(grad_ssntf_where, 4, 0);
		
		RadioButton agerb = new RadioButton("Age");
		agerb.setToggleGroup(students_setGroup); 
		agerb.setUserData("age");
		students_grid.add(agerb, 0, 1); 
		RadioButton agerb_where = new RadioButton("Age");
		agerb_where.setToggleGroup(students_whereGroup); 
		agerb_where.setUserData("age");
		students_grid.add(agerb_where, 3, 1); 
		TextField agetf = new TextField();
		agetf.setMaxWidth(130);
		agetf.setVisible(false);
		students_grid.add(agetf, 1, 1); 
		TextField agetf_where = new TextField();
		agetf_where.setMaxWidth(130);
		agetf_where.setVisible(false);
		students_grid.add(agetf_where, 4, 1);
		
		RadioButton namerb = new RadioButton("Name");
		namerb.setToggleGroup(students_setGroup); 
		namerb.setUserData("name");
		students_grid.add(namerb, 0, 2); 
		RadioButton namerb_where = new RadioButton("Name");
		namerb_where.setToggleGroup(students_whereGroup); 
		namerb_where.setUserData("name");
		students_grid.add(namerb_where, 3, 2); 
		TextField nametf = new TextField();
		nametf.setMaxWidth(130);
		nametf.setVisible(false);
		students_grid.add(nametf, 1, 2); 
		TextField nametf_where = new TextField();
		nametf_where.setMaxWidth(130);
		nametf_where.setVisible(false);
		students_grid.add(nametf_where, 4, 2);
		
		RadioButton degreerb = new RadioButton("Degree");
		degreerb.setToggleGroup(students_setGroup); 
		degreerb.setUserData("deg_prog");
		students_grid.add(degreerb, 0, 3); 
		RadioButton degreerb_where = new RadioButton("Degree");
		degreerb_where.setToggleGroup(students_whereGroup); 
		degreerb_where.setUserData("deg_prog");
		students_grid.add(degreerb_where, 3, 3); 
		TextField degreetf = new TextField();
		degreetf.setMaxWidth(130);
		degreetf.setVisible(false);
		students_grid.add(degreetf, 1, 3); 
		TextField degreetf_where = new TextField();
		degreetf_where.setMaxWidth(130);
		degreetf_where.setVisible(false);
		students_grid.add(degreetf_where, 4, 3);
		
		RadioButton majorrb = new RadioButton("Major");
		majorrb.setToggleGroup(students_setGroup); 
		majorrb.setUserData("major");
		students_grid.add(majorrb, 0, 4); 
		RadioButton majorrb_where = new RadioButton("Major");
		majorrb_where.setToggleGroup(students_whereGroup); 
		majorrb_where.setUserData("major");
		students_grid.add(majorrb_where, 3, 4); 
		TextField majortf = new TextField();
		majortf.setMaxWidth(130);
		majortf.setVisible(false);
		students_grid.add(majortf, 1, 4); 
		TextField majortf_where = new TextField();
		majortf_where.setMaxWidth(130);
		majortf_where.setVisible(false);
		students_grid.add(majortf_where, 4, 4);
		
		TextField[] students_set_tf = {grad_ssntf,agetf,nametf,degreetf,majortf};
		TextField[] students_where_tf = {grad_ssntf_where,agetf_where,nametf_where,degreetf_where,majortf_where};
		//When a radio button is selected, enable the proper textfield to use
		//This prevents multiple textfields being editied at once
		//"Set" group first
		students_setGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(students_setGroup.getSelectedToggle() != null)
				{
					if(students_setGroup.getSelectedToggle() == grad_ssnrb)
					{
						grad_ssntf.setVisible(true);
						agetf.setVisible(false);
						nametf.setVisible(false);
						degreetf.setVisible(false);
						majortf.setVisible(false);
					}
					else if(students_setGroup.getSelectedToggle() == agerb)
					{
						grad_ssntf.setVisible(false);
						agetf.setVisible(true);
						nametf.setVisible(false);
						degreetf.setVisible(false);
						majortf.setVisible(false);
					}
					else if(students_setGroup.getSelectedToggle() == namerb)
					{
						grad_ssntf.setVisible(false);
						agetf.setVisible(false);
						nametf.setVisible(true);
						degreetf.setVisible(false);
						majortf.setVisible(false);
					}
					else if(students_setGroup.getSelectedToggle() == degreerb)
					{
						grad_ssntf.setVisible(false);
						agetf.setVisible(false);
						nametf.setVisible(false);
						degreetf.setVisible(true);
						majortf.setVisible(false);
					}
					else if(students_setGroup.getSelectedToggle() == majorrb)
					{
						grad_ssntf.setVisible(false);
						agetf.setVisible(false);
						nametf.setVisible(false);
						degreetf.setVisible(false);
						majortf.setVisible(true);
					}
				}
			}
		});
		//"where" group second
		students_whereGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(students_whereGroup.getSelectedToggle() != null)
				{
					if(students_whereGroup.getSelectedToggle() == grad_ssnrb_where)
					{
						grad_ssntf_where.setVisible(true);
						agetf_where.setVisible(false);
						nametf_where.setVisible(false);
						degreetf_where.setVisible(false);
						majortf_where.setVisible(false);
					}
					else if(students_whereGroup.getSelectedToggle() == agerb_where)
					{
						grad_ssntf_where.setVisible(false);
						agetf_where.setVisible(true);
						nametf_where.setVisible(false);
						degreetf_where.setVisible(false);
						majortf_where.setVisible(false);
					}
					else if(students_whereGroup.getSelectedToggle() == namerb_where)
					{
						grad_ssntf_where.setVisible(false);
						agetf_where.setVisible(false);
						nametf_where.setVisible(true);
						degreetf_where.setVisible(false);
						majortf_where.setVisible(false);
					}
					else if(students_whereGroup.getSelectedToggle() == degreerb_where)
					{
						grad_ssntf_where.setVisible(false);
						agetf_where.setVisible(false);
						nametf_where.setVisible(false);
						degreetf_where.setVisible(true);
						majortf_where.setVisible(false);
					}
					else if(students_whereGroup.getSelectedToggle() == majorrb_where)
					{
						grad_ssntf_where.setVisible(false);
						agetf_where.setVisible(false);
						nametf_where.setVisible(false);
						degreetf_where.setVisible(false);
						majortf_where.setVisible(true);
					}
				}
			}
		});
		
		Label students_successlbl = new Label();
		students_successlbl.setVisible(false);
		
		Button submit_students = new Button("Submit");
		submit_students.setOnAction(e-> 
								{
									RadioButton set_chk = (RadioButton)students_setGroup.getSelectedToggle();
									RadioButton where_chk = (RadioButton)students_whereGroup.getSelectedToggle();
								
									String set = set_chk.getUserData().toString();
									String where = where_chk.getUserData().toString();
								try
									{
										if(conn.update("graduates", set, where, updateInput(students_set_tf), updateInput(students_where_tf)))
										{
											students_successlbl.setVisible(true);
											students_successlbl.setText("Success!");
											students_successlbl.setTextFill(Color.GREEN);
										}
									}
									catch(Exception e2)
									{
										
									}
								for(int i = 0; i <students_set_tf.length; i++)
								{
									students_set_tf[i].clear();
								}
								for(int i = 0; i < students_where_tf.length; i++)
								{
									students_where_tf[i].clear();
								}
								});
		
		Label students_setAtts = new Label("Set Attribute");
		students_setAtts.setUnderline(true);
		Label students_newVal = new Label("New Value");
		students_newVal.setUnderline(true);
		Label students_whereAtts = new Label("Where Attribute");
		students_whereAtts.setUnderline(true);
		Label students_oldVal = new Label("Old Value");
		students_oldVal.setUnderline(true);
		
		students_title_hbox.setSpacing(50);
		students_title_hbox.setPadding(new Insets(10));
		students_title_hbox.getChildren().addAll(students_setAtts,students_newVal,students_whereAtts,students_oldVal);
		
		students_submit_vbox.setSpacing(10);
		students_submit_vbox.setPadding(new Insets(10));
		students_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		students_submit_vbox.getChildren().addAll(students_successlbl,submit_students);
		
		students_border.setTop(students_title_hbox);
		students_border.setLeft(students_grid);
		students_border.setRight(students_submit_vbox);
		students.setContent(students_border);
//*****************************************\\	
		TitledPane courses = new TitledPane();
		courses.setText("Courses");
		

		BorderPane courses_border = new BorderPane();
		VBox courses_submit_vbox = new VBox();
		HBox courses_title_hbox = new HBox();
		
		GridPane courses_grid = new GridPane();
		courses_grid.setVgap(vgap);
		courses_grid.setHgap(hgap); 
		courses_grid.setPadding(new Insets(10));
		
		ToggleGroup courses_setGroup = new ToggleGroup();
		ToggleGroup courses_whereGroup = new ToggleGroup();
		
		RadioButton c_namerb = new RadioButton("Name");
		c_namerb.setToggleGroup(courses_setGroup); 
		c_namerb.setUserData("c_name");
		courses_grid.add(c_namerb, 0, 0); 
		RadioButton c_namerb_where = new RadioButton("Name");
		c_namerb_where.setToggleGroup(courses_whereGroup); 
		c_namerb_where.setUserData("c_name");
		courses_grid.add(c_namerb_where, 3, 0); 
		TextField c_nametf = new TextField();
		c_nametf.setMaxWidth(130);
		c_nametf.setVisible(false);
		courses_grid.add(c_nametf, 1, 0); 
		TextField c_nametf_where = new TextField();
		c_nametf_where.setMaxWidth(130);
		c_nametf_where.setVisible(false);
		courses_grid.add(c_nametf_where, 4, 0);
		
		RadioButton c_idrb = new RadioButton("ID");
		c_idrb.setToggleGroup(courses_setGroup); 
		c_idrb.setUserData("c_ID");
		courses_grid.add(c_idrb, 0, 1); 
		RadioButton c_idrb_where = new RadioButton("ID");
		c_idrb_where.setToggleGroup(courses_whereGroup); 
		c_idrb_where.setUserData("c_ID");
		courses_grid.add(c_idrb_where, 3, 1); 
		TextField c_idtf = new TextField();
		c_idtf.setMaxWidth(130);
		c_idtf.setVisible(false);
		courses_grid.add(c_idtf, 1, 1); 
		TextField c_idtf_where = new TextField();
		c_idtf_where.setMaxWidth(130);
		c_idtf_where.setVisible(false);
		courses_grid.add(c_idtf_where, 4, 1);
		
		RadioButton c_deptrb = new RadioButton("Department");
		c_deptrb.setToggleGroup(courses_setGroup); 
		c_deptrb.setUserData("c_department");
		courses_grid.add(c_deptrb, 0, 2); 
		RadioButton c_deptrb_where = new RadioButton("Department");
		c_deptrb_where.setToggleGroup(courses_whereGroup); 
		c_deptrb_where.setUserData("c_department");
		courses_grid.add(c_deptrb_where, 3, 2); 
		TextField c_depttf = new TextField();
		c_depttf.setMaxWidth(130);
		c_depttf.setVisible(false);
		courses_grid.add(c_depttf, 1, 2); 
		TextField c_depttf_where = new TextField();
		c_depttf_where.setMaxWidth(130);
		c_depttf_where.setVisible(false);
		courses_grid.add(c_depttf_where, 4, 2);
		
		RadioButton c_instructorrb = new RadioButton("Instructor");
		c_instructorrb.setToggleGroup(courses_setGroup); 
		c_instructorrb.setUserData("c_instructor");
		courses_grid.add(c_instructorrb, 0, 3); 
		RadioButton c_instructorrb_where = new RadioButton("Instructor");
		c_instructorrb_where.setToggleGroup(courses_whereGroup); 
		c_instructorrb_where.setUserData("c_instructor");
		courses_grid.add(c_instructorrb_where, 3, 3); 
		TextField c_instructortf = new TextField();
		c_instructortf.setMaxWidth(130);
		c_instructortf.setVisible(false);
		courses_grid.add(c_instructortf, 1, 3); 
		TextField c_instructortf_where = new TextField();
		c_instructortf_where.setMaxWidth(130);
		c_instructortf_where.setVisible(false);
		courses_grid.add(c_instructortf_where, 4, 3);
		
		
		TextField[] courses_set_tf = {c_nametf,c_idtf,c_depttf,c_instructortf};
		TextField[] courses_where_tf = {c_nametf_where,c_idtf_where,c_depttf_where,c_instructortf_where};
		
		//When a radio button is selected, enable the proper textfield to use
		//This prevents multiple textfields being editied at once
		//"Set" group first
		courses_setGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(courses_setGroup.getSelectedToggle() != null)
				{
					if(courses_setGroup.getSelectedToggle() == c_namerb)
					{
						c_nametf.setVisible(true);
						c_idtf.setVisible(false);
						c_depttf.setVisible(false);
						c_instructortf.setVisible(false);
					}
					else if(courses_setGroup.getSelectedToggle() == c_idrb)
					{
						c_nametf.setVisible(false);
						c_idtf.setVisible(true);
						c_depttf.setVisible(false);
						c_instructortf.setVisible(false);

					}
					else if(courses_setGroup.getSelectedToggle() == c_deptrb)
					{
						c_nametf.setVisible(false);
						c_idtf.setVisible(false);
						c_depttf.setVisible(true);
						c_instructortf.setVisible(false);

					}
					else if(courses_setGroup.getSelectedToggle() == c_instructorrb)
					{
						c_nametf.setVisible(false);
						c_idtf.setVisible(false);
						c_depttf.setVisible(false);
						c_instructortf.setVisible(true);

					}

				}
			}
		});	
		//"where" group second
		courses_whereGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(courses_whereGroup.getSelectedToggle() != null)
				{
					if(courses_whereGroup.getSelectedToggle() == c_namerb_where)
					{
						c_nametf_where.setVisible(true);
						c_idtf_where.setVisible(false);
						c_depttf_where.setVisible(false);
						c_instructortf_where.setVisible(false);
					}
					else if(courses_whereGroup.getSelectedToggle() == c_idrb_where)
					{
						c_nametf_where.setVisible(false);
						c_idtf_where.setVisible(true);
						c_depttf_where.setVisible(false);
						c_instructortf_where.setVisible(false);

					}
					else if(courses_whereGroup.getSelectedToggle() == c_deptrb_where)
					{
						c_nametf_where.setVisible(false);
						c_idtf_where.setVisible(false);
						c_depttf_where.setVisible(true);
						c_instructortf_where.setVisible(false);

					}
					else if(courses_whereGroup.getSelectedToggle() == c_instructorrb_where)
					{
						c_nametf_where.setVisible(false);
						c_idtf_where.setVisible(false);
						c_depttf_where.setVisible(false);
						c_instructortf_where.setVisible(true);

					}

				}
			}
		});

		Label courses_successlbl = new Label();
		courses_successlbl.setVisible(false);
		
		Button submit_courses= new Button("Submit");
		submit_courses.setOnAction(e-> 
								{
									RadioButton set_chk = (RadioButton)courses_setGroup.getSelectedToggle();
									RadioButton where_chk = (RadioButton)courses_whereGroup.getSelectedToggle();
								
									String set = set_chk.getUserData().toString();
									String where = where_chk.getUserData().toString();
								try
									{
										if(conn.update("courses", set, where, updateInput(courses_set_tf), updateInput(courses_where_tf)))
										{
											courses_successlbl.setVisible(true);
											courses_successlbl.setText("Success!");
											courses_successlbl.setTextFill(Color.GREEN);
										}
									}
									catch(Exception e2)
									{
										
									}
								for(int i = 0; i <courses_set_tf.length; i++)
								{
									courses_set_tf[i].clear();
								}
								for(int i = 0; i < courses_where_tf.length; i++)
								{
									courses_where_tf[i].clear();
								}
								});
		
		
		Label courses_setAtts = new Label("Set Attribute");
		courses_setAtts.setUnderline(true);
		Label courses_newVal = new Label("New Value");
		courses_newVal.setUnderline(true);
		Label courses_whereAtts = new Label("Where Attribute");
		courses_whereAtts.setUnderline(true);
		Label courses_oldVal = new Label("Old Value");
		courses_oldVal.setUnderline(true);
		
		courses_title_hbox.setSpacing(50);
		courses_title_hbox.setPadding(new Insets(10));
		courses_title_hbox.getChildren().addAll(courses_setAtts,courses_newVal,courses_whereAtts,courses_oldVal);
		
		courses_submit_vbox.setSpacing(10);
		courses_submit_vbox.setPadding(new Insets(10));
		courses_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		courses_submit_vbox.getChildren().addAll(courses_successlbl,submit_courses);
		
		courses_border.setTop(courses_title_hbox);
		courses_border.setLeft(courses_grid);
		courses_border.setRight(courses_submit_vbox);
		courses.setContent(courses_border);
		
		
		
		Accordion ac = new Accordion();
		ac.setMaxHeight(100);
		ac.getPanes().addAll(prof,depts,projects,students,courses);
		
		return ac;
	}
	
	//Admin delete layout
	public Accordion deleteLayout() throws Exception
	{
		final double vgap = 10;
		final double hgap = 10;
		TitledPane prof = new TitledPane();
		prof.setText("Professors");
		

		BorderPane prof_border = new BorderPane();
		VBox prof_submit_vbox = new VBox();
		HBox prof_title_hbox = new HBox();
		
		GridPane prof_grid = new GridPane();
		prof_grid.setVgap(vgap);
		prof_grid.setHgap(hgap); 
		prof_grid.setPadding(new Insets(10));
		
		ToggleGroup prof_Group = new ToggleGroup();

		RadioButton prof_ssnrb = new RadioButton("SSN");
		prof_ssnrb.setToggleGroup(prof_Group); 
		prof_ssnrb.setUserData("prof_ssn");
		prof_grid.add(prof_ssnrb, 0, 0); 
		TextField prof_ssntf = new TextField();
		prof_ssntf.setMaxWidth(130);
		prof_ssntf.setVisible(false);
		prof_grid.add(prof_ssntf, 1, 0); 

		RadioButton namerb = new RadioButton("Name");
		namerb.setToggleGroup(prof_Group); 
		namerb.setUserData("name");
		prof_grid.add(namerb, 0, 1); 
		TextField nametf = new TextField();
		nametf.setMaxWidth(130);
		nametf.setVisible(false);
		prof_grid.add(nametf, 1, 1); 
		
		RadioButton agerb = new RadioButton("Age");
		agerb.setToggleGroup(prof_Group); 
		agerb.setUserData("name");
		prof_grid.add(agerb, 0, 2); 
		TextField agetf = new TextField();
		agetf.setMaxWidth(130);
		agetf.setVisible(false);
		prof_grid.add(agetf, 1, 2); 
		
		RadioButton rankrb = new RadioButton("Rank");
		rankrb.setToggleGroup(prof_Group); 
		rankrb.setUserData("rank");
		prof_grid.add(rankrb, 0, 3); 
		TextField ranktf = new TextField();
		ranktf.setMaxWidth(130);
		ranktf.setVisible(false);
		prof_grid.add(ranktf, 1, 3); 
		
		RadioButton focusrb = new RadioButton("Speciality");
		focusrb.setToggleGroup(prof_Group); 
		focusrb.setUserData("speciality");
		prof_grid.add(focusrb, 0, 4); 
		TextField focustf = new TextField();
		focustf.setMaxWidth(130);
		focustf.setVisible(false);
		prof_grid.add(focustf, 1, 4); 
		
		prof_Group.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(prof_Group.getSelectedToggle() != null)
				{
					if(prof_Group.getSelectedToggle() == prof_ssnrb)
					{
						prof_ssntf.setVisible(true);
						nametf.setVisible(false);
						agetf.setVisible(false);
						ranktf.setVisible(false);
						focustf.setVisible(false);
					}
					else if(prof_Group.getSelectedToggle() == namerb)
					{
						prof_ssntf.setVisible(false);
						nametf.setVisible(true);
						agetf.setVisible(false);
						ranktf.setVisible(false);
						focustf.setVisible(false);
					}
					else if(prof_Group.getSelectedToggle() == agerb)
					{
						prof_ssntf.setVisible(false);
						nametf.setVisible(false);
						agetf.setVisible(true);
						ranktf.setVisible(false);
						focustf.setVisible(false);
					}
					else if(prof_Group.getSelectedToggle() == rankrb)
					{
						prof_ssntf.setVisible(false);
						nametf.setVisible(false);
						agetf.setVisible(false);
						ranktf.setVisible(true);
						focustf.setVisible(false);
					}
					else if(prof_Group.getSelectedToggle() == focusrb)
					{
						prof_ssntf.setVisible(false);
						nametf.setVisible(false);
						agetf.setVisible(false);
						ranktf.setVisible(false);
						focustf.setVisible(true);
						
					}

				}
			}
		});	
		
		TextField[] prof_tf = {prof_ssntf,nametf,agetf,ranktf,focustf};
		
		Label prof_successlbl = new Label();
		prof_successlbl.setVisible(false);
		
		Button submit_prof= new Button("Submit");
		submit_prof.setOnAction(e -> 
							{
								RadioButton chk = (RadioButton)prof_Group.getSelectedToggle();
								String prof_att = chk.getUserData().toString();
								try
								{
									if(conn.delete("professors", prof_att, updateInput(prof_tf)))
									{
										prof_successlbl.setVisible(true);
										prof_successlbl.setText("Success!");
										prof_successlbl.setTextFill(Color.GREEN);
									}
								}
								catch(Exception e2)
								{
									
								}
								for(int i = 0; i <prof_tf.length; i++)
								{
									prof_tf[i].clear();
								}
								
							});
		
		Label prof_Att = new Label("Attribute");
		prof_Att.setUnderline(true);
		Label prof_val = new Label("Value");
		prof_val.setUnderline(true);
		
		prof_title_hbox.setSpacing(50);
		prof_title_hbox.setPadding(new Insets(10));
		prof_title_hbox.getChildren().addAll(prof_Att,prof_val);
		
		prof_submit_vbox.setSpacing(10);
		prof_submit_vbox.setPadding(new Insets(10));
		prof_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		prof_submit_vbox.getChildren().addAll(prof_successlbl,submit_prof);
		
		prof_border.setTop(prof_title_hbox);
		prof_border.setLeft(prof_grid);
		prof_border.setRight(prof_submit_vbox);
		prof.setContent(prof_border);
		
//******************************************\\	
		TitledPane depts = new TitledPane();
		depts.setText("Departments");
		
		BorderPane depts_border = new BorderPane();
		VBox depts_submit_vbox = new VBox();
		HBox depts_title_hbox = new HBox();
		
		GridPane depts_grid = new GridPane();
		depts_grid.setVgap(vgap);
		depts_grid.setHgap(hgap); 
		depts_grid.setPadding(new Insets(10));
		
		ToggleGroup depts_Group = new ToggleGroup();
		

		RadioButton dnorb = new RadioButton("Number");
		dnorb.setToggleGroup(depts_Group); 
		dnorb.setUserData("dno");
		depts_grid.add( dnorb, 0, 0); 
		TextField  dnotf = new TextField();
		dnotf.setMaxWidth(130);
		dnotf.setVisible(false);
		depts_grid.add( dnotf, 1, 0); 
		
		RadioButton dnamerb = new RadioButton("Name");
		dnamerb.setToggleGroup(depts_Group); 
		dnamerb.setUserData("dname");
		depts_grid.add(dnamerb, 0, 1); 
		TextField dnametf = new TextField();
		dnametf.setMaxWidth(130);
		dnametf.setVisible(false);
		depts_grid.add(dnametf, 1, 1); 
		
		RadioButton officerb = new RadioButton("Office");
		officerb.setToggleGroup(depts_Group); 
		officerb.setUserData("office");
		depts_grid.add(officerb, 0, 2); 
		TextField officetf = new TextField();
		officetf.setMaxWidth(130);
		officetf.setVisible(false);
		depts_grid.add(officetf, 1, 2); 
		
		depts_Group.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(depts_Group.getSelectedToggle() != null)
				{
					if(depts_Group.getSelectedToggle() == dnorb)
					{
						dnotf.setVisible(true);
						dnametf.setVisible(false);
						officetf.setVisible(false);
					}
					else if(depts_Group.getSelectedToggle() == dnamerb)
					{
						dnotf.setVisible(false);
						dnametf.setVisible(true);
						officetf.setVisible(false);

					}
					else if(depts_Group.getSelectedToggle() == officerb)
					{
						dnotf.setVisible(false);
						dnametf.setVisible(false);
						officetf.setVisible(true);

					}

				}
			}
		});	
		
		TextField[] depts_tf = {dnotf,dnametf,officetf};
		
		Label depts_successlbl = new Label();
		depts_successlbl.setVisible(false);
		
		Button submit_depts= new Button("Submit");
		submit_depts.setOnAction(e -> 
							{
								RadioButton chk = (RadioButton)depts_Group.getSelectedToggle();
								String depts_att = chk.getUserData().toString();
								try
								{
									if(conn.delete("depts", depts_att, updateInput(depts_tf)))
									{
										depts_successlbl.setVisible(true);
										depts_successlbl.setText("Success!");
										depts_successlbl.setTextFill(Color.GREEN);
									}
								}
								catch(Exception e2)
								{
									
								}
								for(int i = 0; i <depts_tf.length; i++)
								{
									depts_tf[i].clear();
								}
								
							});
		
		Label depts_Att = new Label("Attribute");
		depts_Att.setUnderline(true);
		Label depts_val = new Label("Value");
		depts_val.setUnderline(true);
		
		depts_title_hbox.setSpacing(50);
		depts_title_hbox.setPadding(new Insets(10));
		depts_title_hbox.getChildren().addAll(depts_Att,depts_val);
		
		depts_submit_vbox.setSpacing(10);
		depts_submit_vbox.setPadding(new Insets(10));
		depts_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		depts_submit_vbox.getChildren().addAll(depts_successlbl,submit_depts);
		
		depts_border.setTop(depts_title_hbox);
		depts_border.setLeft(depts_grid);
		depts_border.setRight(depts_submit_vbox);
		depts.setContent(depts_border);
//******************************************\\			
		TitledPane projects = new TitledPane();
		projects.setText("Projects");
		
		BorderPane projects_border = new BorderPane();
		VBox projects_submit_vbox = new VBox();
		HBox projects_title_hbox = new HBox();
		
		GridPane projects_grid = new GridPane();
		projects_grid.setVgap(vgap);
		projects_grid.setHgap(hgap); 
		projects_grid.setPadding(new Insets(10));
		
		ToggleGroup projects_Group = new ToggleGroup();
		

		RadioButton pidrb = new RadioButton("PID");
		pidrb.setToggleGroup(projects_Group); 
		pidrb.setUserData("pid");
		projects_grid.add(pidrb, 0, 0); 
		TextField pidtf = new TextField();
		pidtf.setMaxWidth(130);
		pidtf.setVisible(false);
		projects_grid.add(pidtf, 1, 0); 
		
		RadioButton sponsorrb = new RadioButton("Sponsor");
		sponsorrb.setToggleGroup(projects_Group); 
		sponsorrb.setUserData("sponsor");
		projects_grid.add(sponsorrb, 0, 1); 
		TextField sponsortf = new TextField();
		sponsortf.setMaxWidth(130);
		sponsortf.setVisible(false);
		projects_grid.add(sponsortf, 1, 1); 
		
		RadioButton startrb = new RadioButton("Start Date");
		startrb.setToggleGroup(projects_Group); 
		startrb.setUserData("start_date");
		projects_grid.add(startrb, 0, 2); 
		TextField starttf = new TextField();
		starttf.setMaxWidth(130);
		starttf.setVisible(false);
		projects_grid.add(starttf, 1, 2); 
		
		RadioButton endrb = new RadioButton("End Date");
		endrb.setToggleGroup(projects_Group); 
		endrb.setUserData("end_date");
		projects_grid.add(endrb, 0, 3); 
		TextField endtf = new TextField();
		endtf.setMaxWidth(130);
		endtf.setVisible(false);
		projects_grid.add(endtf, 1, 3); 
		
		RadioButton budgetrb = new RadioButton("Budget");
		budgetrb.setToggleGroup(projects_Group); 
		budgetrb.setUserData("budget");
		projects_grid.add(budgetrb, 0, 4); 
		TextField budgettf = new TextField();
		budgettf.setMaxWidth(130);
		budgettf.setVisible(false);
		projects_grid.add(budgettf, 1, 4); 
	
		
		projects_Group.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(projects_Group.getSelectedToggle() != null)
				{
					if(projects_Group.getSelectedToggle() == pidrb)
					{
						pidtf.setVisible(true);
						sponsortf.setVisible(false);
						starttf.setVisible(false);
						endtf.setVisible(false);
						budgettf.setVisible(false);
					}
					else if(projects_Group.getSelectedToggle() == sponsorrb)
					{
						pidtf.setVisible(false);
						sponsortf.setVisible(true);
						starttf.setVisible(false);
						endtf.setVisible(false);
						budgettf.setVisible(false);

					}
					else if(projects_Group.getSelectedToggle() == startrb)
					{
						pidtf.setVisible(false);
						sponsortf.setVisible(false);
						starttf.setVisible(true);
						endtf.setVisible(false);
						budgettf.setVisible(false);

					}
					else if(projects_Group.getSelectedToggle() == endrb)
					{
						pidtf.setVisible(false);
						sponsortf.setVisible(false);
						starttf.setVisible(false);
						endtf.setVisible(true);
						budgettf.setVisible(false);
					}
					else if(projects_Group.getSelectedToggle() == budgetrb)
					{
						pidtf.setVisible(false);
						sponsortf.setVisible(false);
						starttf.setVisible(false);
						endtf.setVisible(false);
						budgettf.setVisible(true);

					}

				}
			}
		});	
		
		TextField[] projects_tf = {pidtf,sponsortf,starttf,endtf,budgettf};
		
		Label projects_successlbl = new Label();
		projects_successlbl.setVisible(false);
		
		Button submit_projects= new Button("Submit");
		submit_projects.setOnAction(e -> 
							{
								RadioButton chk = (RadioButton)projects_Group.getSelectedToggle();
								String projects_att = chk.getUserData().toString();
								try
								{
									if(conn.delete("projects", projects_att, updateInput(projects_tf)))
									{
										projects_successlbl.setVisible(true);
										projects_successlbl.setText("Success!");
										projects_successlbl.setTextFill(Color.GREEN);
									}
								}
								catch(Exception e2)
								{
									
								}
								for(int i = 0; i <projects_tf.length; i++)
								{
									projects_tf[i].clear();
								}
								
							});
		
		Label projects_Att = new Label("Attribute");
		projects_Att.setUnderline(true);
		Label projects_val = new Label("Value");
		projects_val.setUnderline(true);
		
		projects_title_hbox.setSpacing(50);
		projects_title_hbox.setPadding(new Insets(10));
		projects_title_hbox.getChildren().addAll(projects_Att,projects_val);
		
		projects_submit_vbox.setSpacing(10);
		projects_submit_vbox.setPadding(new Insets(10));
		projects_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		projects_submit_vbox.getChildren().addAll(projects_successlbl,submit_projects);
		
		projects_border.setTop(projects_title_hbox);
		projects_border.setLeft(projects_grid);
		projects_border.setRight(projects_submit_vbox);
		projects.setContent(projects_border);
//******************************************\\	
		TitledPane students = new TitledPane();
		students.setText("Graduates");
		
		BorderPane students_border = new BorderPane();
		VBox students_submit_vbox = new VBox();
		HBox students_title_hbox = new HBox();
		
		GridPane students_grid = new GridPane();
		students_grid.setVgap(vgap);
		students_grid.setHgap(hgap); 
		students_grid.setPadding(new Insets(10));
		
		ToggleGroup students_Group = new ToggleGroup();
		
		RadioButton grad_ssnrb = new RadioButton("SSN");
		grad_ssnrb.setToggleGroup(students_Group); 
		grad_ssnrb.setUserData("grad_ssn");
		students_grid.add(grad_ssnrb, 0, 0); 
		TextField grad_ssntf = new TextField();
		grad_ssntf.setMaxWidth(130);
		grad_ssntf.setVisible(false);
		students_grid.add(grad_ssntf, 1, 0); 
		
		RadioButton grad_agerb = new RadioButton("Age");
		grad_agerb.setToggleGroup(students_Group); 
		grad_agerb.setUserData("age");
		students_grid.add(grad_agerb, 0, 1); 
		TextField grad_agetf = new TextField();
		grad_agetf.setMaxWidth(130);
		grad_agetf.setVisible(false);
		students_grid.add(grad_agetf, 1, 1); 
		
		RadioButton grad_namerb = new RadioButton("Name");
		grad_namerb.setToggleGroup(students_Group); 
		grad_namerb.setUserData("name");
		students_grid.add(grad_namerb, 0, 2); 
		TextField grad_nametf = new TextField();
		grad_nametf.setMaxWidth(130);
		grad_nametf.setVisible(false);
		students_grid.add(grad_nametf, 1, 2); 
		
		RadioButton degreerb = new RadioButton("Degree");
		degreerb.setToggleGroup(students_Group); 
		degreerb.setUserData("deg_prog");
		students_grid.add(degreerb, 0, 3); 
		TextField degreetf = new TextField();
		degreetf.setMaxWidth(130);
		degreetf.setVisible(false);
		students_grid.add(degreetf, 1, 3); 
		
		RadioButton majorrb = new RadioButton("Major");
		majorrb.setToggleGroup(students_Group); 
		majorrb.setUserData("major");
		students_grid.add(majorrb, 0, 4); 
		TextField majortf = new TextField();
		majortf.setMaxWidth(130);
		majortf.setVisible(false);
		students_grid.add(majortf, 1, 4); 
	
		
		students_Group.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(students_Group.getSelectedToggle() != null)
				{
					if(students_Group.getSelectedToggle() == grad_ssnrb)
					{
						grad_ssntf.setVisible(true);
						grad_agetf.setVisible(false);
						grad_nametf.setVisible(false);
						degreetf.setVisible(false);
						majortf.setVisible(false);
					}
					else if(students_Group.getSelectedToggle() == grad_agerb)
					{
						grad_ssntf.setVisible(false);
						grad_agetf.setVisible(true);
						grad_nametf.setVisible(false);
						degreetf.setVisible(false);
						majortf.setVisible(false);

					}
					else if(students_Group.getSelectedToggle() == grad_namerb)
					{
						grad_ssntf.setVisible(false);
						grad_agetf.setVisible(false);
						grad_nametf.setVisible(true);
						degreetf.setVisible(false);
						majortf.setVisible(false);

					}
					else if(students_Group.getSelectedToggle() == degreerb)
					{
						grad_ssntf.setVisible(false);
						grad_agetf.setVisible(false);
						grad_nametf.setVisible(false);
						degreetf.setVisible(true);
						majortf.setVisible(false);

					}
					else if(students_Group.getSelectedToggle() == majorrb)
					{
						grad_ssntf.setVisible(false);
						grad_agetf.setVisible(false);
						grad_nametf.setVisible(false);
						degreetf.setVisible(false);
						majortf.setVisible(true);

					}

				}
			}
		});	
		
		TextField[] students_tf = {grad_ssntf,grad_agetf,grad_nametf,degreetf,majortf};
		
		Label students_successlbl = new Label();
		students_successlbl.setVisible(false);
		
		Button submit_students= new Button("Submit");
		submit_students.setOnAction(e -> 
							{
								RadioButton chk = (RadioButton)students_Group.getSelectedToggle();
								String grad_att = chk.getUserData().toString();
								try
								{
									if(conn.delete("graduates", grad_att, updateInput(students_tf)))
									{
							
										students_successlbl.setVisible(true);
										students_successlbl.setText("Success!");
										students_successlbl.setTextFill(Color.GREEN);
									}
								}
								catch(Exception e2)
								{
									
								}
								for(int i = 0; i <students_tf.length; i++)
								{
									students_tf[i].clear();
								}
								
							});
		
		Label students_Att = new Label("Attribute");
		students_Att.setUnderline(true);
		Label students_val = new Label("Value");
		students_val.setUnderline(true);
		
		students_title_hbox.setSpacing(50);
		students_title_hbox.setPadding(new Insets(10));
		students_title_hbox.getChildren().addAll(students_Att,students_val);
		
		students_submit_vbox.setSpacing(10);
		students_submit_vbox.setPadding(new Insets(10));
		students_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		students_submit_vbox.getChildren().addAll(students_successlbl,submit_students);
		
		students_border.setTop(students_title_hbox);
		students_border.setLeft(students_grid);
		students_border.setRight(students_submit_vbox);
		students.setContent(students_border);
//******************************************\\	
		TitledPane courses = new TitledPane();
		courses.setText("Courses");
		
		BorderPane courses_border = new BorderPane();
		VBox courses_submit_vbox = new VBox();
		HBox courses_title_hbox = new HBox();
		
		GridPane courses_grid = new GridPane();
		courses_grid.setVgap(vgap);
		courses_grid.setHgap(hgap); 
		courses_grid.setPadding(new Insets(10));
		
		ToggleGroup courses_Group = new ToggleGroup();
		
		RadioButton c_namerb = new RadioButton("Name");
		c_namerb.setToggleGroup(courses_Group); 
		c_namerb.setUserData("c_name");
		courses_grid.add(c_namerb, 0, 0); 
		TextField c_nametf = new TextField();
		c_nametf.setMaxWidth(130);
		c_nametf.setVisible(false);
		courses_grid.add(c_nametf, 1, 0); 
		
		RadioButton c_idrb = new RadioButton("ID");
		c_idrb.setToggleGroup(courses_Group); 
		c_idrb.setUserData("c_ID");
		courses_grid.add(c_idrb, 0, 1); 
		TextField c_idtf = new TextField();
		c_idtf.setMaxWidth(130);
		c_idtf.setVisible(false);
		courses_grid.add(c_idtf, 1, 1); 
		
		RadioButton c_deptsrb = new RadioButton("Department");
		c_deptsrb.setToggleGroup(courses_Group); 
		c_deptsrb.setUserData("c_department");
		courses_grid.add(c_deptsrb, 0, 2); 
		TextField c_deptstf = new TextField();
		c_deptstf.setMaxWidth(130);
		c_deptstf.setVisible(false);
		courses_grid.add(c_deptstf, 1, 2); 
		
		RadioButton c_instructorrb = new RadioButton("Instructor");
		c_instructorrb.setToggleGroup(courses_Group); 
		c_instructorrb.setUserData("c_instructor");
		courses_grid.add(c_instructorrb, 0, 3); 
		TextField c_instructortf = new TextField();
		c_instructortf.setMaxWidth(130);
		c_instructortf.setVisible(false);
		courses_grid.add(c_instructortf, 1, 3); 
		
	
		
		courses_Group.selectedToggleProperty().addListener(new ChangeListener<Toggle>()
		{
			public void changed(ObservableValue<? extends Toggle> ov, Toggle oldToggle, Toggle newToggle)
			{
				if(courses_Group.getSelectedToggle() != null)
				{
					if(courses_Group.getSelectedToggle() == c_namerb)
					{
						c_nametf.setVisible(true);
						c_idtf.setVisible(false);
						c_deptstf.setVisible(false);
						c_instructortf.setVisible(false);

					}
					else if(courses_Group.getSelectedToggle() == c_idrb)
					{
						c_nametf.setVisible(false);
						c_idtf.setVisible(true);
						c_deptstf.setVisible(false);
						c_instructortf.setVisible(false);

					}
					else if(courses_Group.getSelectedToggle() == c_deptsrb)
					{
						c_nametf.setVisible(false);
						c_idtf.setVisible(false);
						c_deptstf.setVisible(true);
						c_instructortf.setVisible(false);

					}
					else if(courses_Group.getSelectedToggle() == c_instructorrb)
					{
						c_nametf.setVisible(false);
						c_idtf.setVisible(false);
						c_deptstf.setVisible(false);
						c_instructortf.setVisible(true);

					}
				

				}
			}
		});	
		
		TextField[] courses_tf = {c_nametf,c_idtf,c_deptstf,c_instructortf};
		
		Label courses_successlbl = new Label();
		courses_successlbl.setVisible(false);
		
		Button submit_courses= new Button("Submit");
		submit_courses.setOnAction(e -> 
							{
								RadioButton chk = (RadioButton)courses_Group.getSelectedToggle();
								String course_att = chk.getUserData().toString();
								try
								{
									if(conn.delete("courses", course_att, updateInput(courses_tf)))
									{
										courses_successlbl.setVisible(true);
										courses_successlbl.setText("Success!");
										courses_successlbl.setTextFill(Color.GREEN);
									}
								}
								catch(Exception e2)
								{
									
								}
								for(int i = 0; i <courses_tf.length; i++)
								{
									courses_tf[i].clear();
								}
								
							});
		
		Label courses_Att = new Label("Attribute");
		courses_Att.setUnderline(true);
		Label courses_val = new Label("Value");
		courses_val.setUnderline(true);
		
		courses_title_hbox.setSpacing(50);
		courses_title_hbox.setPadding(new Insets(10));
		courses_title_hbox.getChildren().addAll(courses_Att,courses_val);
		
		courses_submit_vbox.setSpacing(10);
		courses_submit_vbox.setPadding(new Insets(10));
		courses_submit_vbox.setAlignment(Pos.BOTTOM_RIGHT);
		courses_submit_vbox.getChildren().addAll(courses_successlbl,submit_courses);
		
		courses_border.setTop(courses_title_hbox);
		courses_border.setLeft(courses_grid);
		courses_border.setRight(courses_submit_vbox);
		courses.setContent(courses_border);
		
		Accordion ac = new Accordion();
		ac.setMaxHeight(100);
		ac.getPanes().addAll(prof,depts,projects,students,courses);
		
		return ac;
	}
	
	//Admin search layout
	public BorderPane searchLayout() throws Exception
	{
		final double width = 300;
		
		BorderPane bp = new BorderPane();
		GridPane gp = new GridPane();
		VBox vbox = new VBox();
		
		Label select = new Label("Select");
		gp.add(select, 0, 0);
		TextField select_tf = new TextField();
		select_tf.setMaxWidth(width);
		select_tf.setPromptText("e.g. name");
		gp.add(select_tf, 1, 0); 
		
		Label from = new Label("From");
		gp.add(from, 0, 1); 
		TextField from_tf = new TextField();
		from_tf.setMaxWidth(width);
		from_tf.setPromptText("e.g. professors");
		gp.add(from_tf, 1, 1); 
		
		Label where = new Label("Where");
		gp.add(where, 0, 2); 
		TextField where_tf = new TextField();
		where_tf.setMaxWidth(width);
		where_tf.setPromptText("e.g. age=45");
		gp.add(where_tf, 1, 2); 
		
		Label output = new Label("Output");
		TextArea output_ta = new TextArea();
		output_ta.setPrefColumnCount(12);
		output_ta.setMaxHeight(250);
		
		Button submit = new Button("Submit");
		gp.add(submit, 3, 4); 
		
		BooleanBinding areEmpty = select_tf.textProperty().isEmpty().or(from_tf.textProperty().isEmpty());
		submit.disableProperty().bind(areEmpty);
		
		submit.setOnAction(e -> 
					{
						try
						{
							if(where_tf.getText() == null || where_tf.getText().trim().isEmpty())
							{
								StringBuilder sb = new StringBuilder();
								
								results = conn.searchBasic(select_tf.getText(), from_tf.getText());
								rsmd = results.getMetaData();
								int colNum = rsmd.getColumnCount();
								while(results.next())
								{
									for(int i = 1; i <=colNum; i++)
									{
										String colVal = results.getString(i)+"\n";
										String row = sb.append(colVal).toString();
										output_ta.setText(row);
										
										
									}
									
									
								}
							}
							else
							{
								StringBuilder sb = new StringBuilder();
								
								results = conn.searchWhere(select_tf.getText(), from_tf.getText(), where_tf.getText());
								rsmd = results.getMetaData();
								int colNum = rsmd.getColumnCount();
								while(results.next())
								{
									for(int i = 1; i <=colNum; i++)
									{
										String colVal = results.getString(i)+"\n";
										String row = sb.append(colVal).toString();
										output_ta.setText(row);
										
										
									}
								}
							}
							
						}

						catch(Exception e2)
						{
							
						}
					});
		
		

		
		vbox.setSpacing(10);
		vbox.setPadding(new Insets(0,50,75,0));
		vbox.getChildren().addAll(output,output_ta);
		vbox.setAlignment(Pos.CENTER);
		
		gp.setVgap(25);
		gp.setHgap(10);
		gp.setPadding(new Insets(0,0,0,25));
		gp.setAlignment(Pos.CENTER_LEFT);
		

		bp.setRight(vbox);
		bp.setCenter(gp);
		
		return bp;
	}
	
//***Staff Scene***\\\	
	public TabPane addTabPane_s() throws Exception
	{
		TabPane tp = new TabPane();
		Tab search = new Tab("Search");
		search.setContent(addStaffSearch());
	
		tp.getTabs().addAll(search);
		tp.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		
		return tp;
	}
	

	public BorderPane addStaffSearch()
	{
		final double width = 300;
		
		BorderPane bp = new BorderPane();
		GridPane gp = new GridPane();
		VBox vbox = new VBox();

		
		
		Label select = new Label("Select");
		gp.add(select, 0, 0);
		TextField select_tf = new TextField();
		select_tf.setMaxWidth(width);
		select_tf.setPromptText("e.g. name");
		gp.add(select_tf, 1, 0); 
		
		Label from = new Label("From");
		gp.add(from, 0, 1); 
		TextField from_tf = new TextField();
		from_tf.setMaxWidth(width);
		from_tf.setPromptText("e.g. professors");
		gp.add(from_tf, 1, 1); 
		
		Label where = new Label("Where");
		gp.add(where, 0, 2); 
		TextField where_tf = new TextField();
		where_tf.setMaxWidth(width);
		where_tf.setPromptText("e.g. age=45");
		gp.add(where_tf, 1, 2); 
		
		Label output = new Label("Output");
		TextArea output_ta = new TextArea();
		output_ta.setPrefColumnCount(12);
		output_ta.setMaxHeight(250);
		
		Button submit = new Button("Submit");
		gp.add(submit, 3, 4); 
		
		BooleanBinding areEmpty = select_tf.textProperty().isEmpty().or(from_tf.textProperty().isEmpty());
		submit.disableProperty().bind(areEmpty);
		
		submit.setOnAction(e -> 
					{
						try
						{
							if(where_tf.getText() == null || where_tf.getText().trim().isEmpty())
							{
								StringBuilder sb = new StringBuilder();
						
								results = conn.searchBasic(select_tf.getText(), from_tf.getText());
								rsmd = results.getMetaData();
								int colNum = rsmd.getColumnCount();
								while(results.next())
								{
									for(int i = 1; i <=colNum; i++)
									{
										String colVal = results.getString(i)+"\n";
										String row = sb.append(colVal).toString();
										output_ta.setText(row);
										
									}
									
								}
							}
							else
							{
								StringBuilder sb = new StringBuilder();
								
								results = conn.searchWhere(select_tf.getText(), from_tf.getText(), where_tf.getText());
								rsmd = results.getMetaData();
								int colNum = rsmd.getColumnCount();
								while(results.next())
								{
									for(int i = 1; i <=colNum; i++)
									{
										String colVal = results.getString(i)+"\n";
										String row = sb.append(colVal).toString();
										output_ta.setText(row);
										
										
									}
								}
							}
							
						}

						catch(Exception e2)
						{
							
						}
					});
		
		


		vbox.setSpacing(10);
		vbox.setPadding(new Insets(0,50,75,0));
		vbox.getChildren().addAll(output,output_ta);
		vbox.setAlignment(Pos.CENTER);
		
		gp.setVgap(25);
		gp.setHgap(10);
		gp.setPadding(new Insets(0,0,0,25));
		gp.setAlignment(Pos.CENTER_LEFT);
		
		bp.setRight(vbox);
		bp.setCenter(gp);
		
		return bp;
		
	}
	
	//Searches through all of the textfield strings and concats them for JDBC use
	public String updateInput(TextField[] tf)
	{
		String value = "";
		for(int i = 0; i < tf.length; i++)
		{
			if(tf[i].isVisible())
			{
				value = tf[i].getText().toString();
				return value;
			}
		}
		
		return value;
	}
	
	//Concat all of the data to be added in JDBC language
	public String collectData(TextField[] textfields)
	{
		StringJoiner sj = new StringJoiner("',","(","')"); 
		String str = "";
		for(int i = 0; i < textfields.length; i++)
		{
			textfields[i].insertText(0, "'");
			sj.add(textfields[i].getText());
		}
		str = sj.toString();
		System.out.println(str); 
		return str;
	}
	
	
//***Student Scene***
	public TabPane addTabPane()
	{
		TabPane tabpane = new TabPane();
	
		Tab enroll = new Tab("Enrollment");
		enroll.setContent(enrollLayout());
		
		
		Tab search = new Tab("Search");
		search.setContent(addStudentSearch());
		
		tabpane.getTabs().addAll(enroll,search);
		tabpane.setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		
		
		return tabpane;
	}
	
	//Enrollment Tab Layout
	public BorderPane enrollLayout()
	{
		double vgap = 20;
		double hgap = 20;
		
		BorderPane bp = new BorderPane();
		VBox vbox = new VBox();
		HBox hbox = new HBox();
		GridPane grid = new GridPane();
		
		Label courses_lbl = new Label("Courses");
		courses_lbl.setUnderline(true);
		Label id_lbl = new Label("ID");
		id_lbl.setUnderline(true);
		Label instructor_lbl = new Label("Instructor");
		instructor_lbl.setUnderline(true);
		grid.add(courses_lbl, 0, 0);
		grid.add(id_lbl, 1, 0);
		grid.add(instructor_lbl, 2, 0);
		 
		CheckBox database_cb = new CheckBox("Database");
		Label database_lbl = new Label("CS430");
		Label instr_430_lbl = new Label("Nathanael Powers");
		grid.add(database_cb, 0, 1); 
		grid.add(database_lbl, 1, 1); 
		grid.add(instr_430_lbl, 2, 1); 
		CheckBox networking_cb = new CheckBox("Networking");
		Label networking_lbl = new Label("CS440");
		Label instr_440_lbl = new Label("Dakota Harvey");
		grid.add(networking_cb, 0, 2); 
		grid.add(networking_lbl, 1, 2); 
		grid.add(instr_440_lbl, 2, 2); 
		CheckBox electCircuits_cb = new CheckBox("Electrical Circuits II");
		Label electCircuits_lbl = new Label("ECE336");
		Label instr_336_lbl = new Label("Sumaiya Rowley");
		grid.add(electCircuits_cb, 0, 3); 
		grid.add(electCircuits_lbl, 1, 3);
		grid.add(instr_336_lbl, 2, 3); 
		CheckBox thermalDesign_cb = new CheckBox("Thermal Systems Design");
		Label thermalDesign_lbl = new Label("ME406");
		Label instr_406_lbl = new Label("Lyle Gilbert");
		grid.add(thermalDesign_cb, 0, 4); 
		grid.add(thermalDesign_lbl, 1, 4);
		grid.add(instr_406_lbl, 2, 4); 
		CheckBox modernPhysics_cb = new CheckBox("Modern Physics");
		Label modernPhysics_lbl = new Label("PHYS305");
		Label instr_305_lbl = new Label("Lylah Almond");
		grid.add(modernPhysics_cb, 0, 5); 
		grid.add(modernPhysics_lbl, 1, 5);
		grid.add(instr_305_lbl, 2, 5); 
		CheckBox linearAlg_cb = new CheckBox("Linear Algebra");
		Label linearAlg_lbl = new Label("MATH221");
		Label instr_221_lbl = new Label("Liam Fox");
		grid.add(linearAlg_cb, 0, 6); 
		grid.add(linearAlg_lbl, 1, 6);
		grid.add(instr_221_lbl, 2, 6); 
		CheckBox auditing_cb = new CheckBox("Auditing");
		Label auditing_lbl = new Label("ACCT460");
		Label instr_460_lbl = new Label("Mitchel Archer");
		grid.add(auditing_cb, 0, 7); 
		grid.add(auditing_lbl, 1, 7);
		grid.add(instr_460_lbl, 2, 7); 
		CheckBox evolution_cb = new CheckBox("Evolution");
		Label evolution_lbl = new Label("BIO304");
		Label instr_304_lbl = new Label("Hamish Carr");
		grid.add(evolution_cb, 0, 8); 
		grid.add(evolution_lbl, 1, 8);
		grid.add(instr_304_lbl, 2, 8); 
		CheckBox airForce_cb = new CheckBox("The Air Force Today II");
		Label airForce_lbl = new Label("AS102");
		Label instr_102_lbl = new Label("Adelle Kaufman");
		grid.add(airForce_cb, 0, 9); 
		grid.add(airForce_lbl, 1, 9);
		grid.add(instr_102_lbl, 2, 9); 
		CheckBox political_cb = new CheckBox("Political Parties");
		Label political_lbl = new Label("POL319");
		Label instr_319_lbl = new Label("Zeenat Roman");
		grid.add(political_cb, 0, 10); 
		grid.add(political_lbl, 1, 10);
		grid.add(instr_319_lbl, 2, 10); 
		CheckBox forest_cb = new CheckBox("Forest Health");
		Label forest_lbl = new Label("FOR314");
		Label instr_314_lbl = new Label("Enrico Brewer"); 
		grid.add(forest_cb, 0, 11); 
		grid.add(forest_lbl, 1, 11);
		grid.add(instr_314_lbl, 2, 11); 
			
		CheckBox[] cbs = {database_cb,networking_cb,electCircuits_cb,thermalDesign_cb,modernPhysics_cb,linearAlg_cb,auditing_cb,evolution_cb,airForce_cb,political_cb,forest_cb};
		Label[] lbl = {database_lbl,networking_lbl,electCircuits_lbl,thermalDesign_lbl,modernPhysics_lbl,linearAlg_lbl,auditing_lbl,evolution_lbl,airForce_lbl,political_lbl,forest_lbl};
		
		Button enroll_btn = new Button("Enroll");
		enroll_btn.setMinWidth(100);
		enroll_btn.setOnAction(e -> 
							{
								try
								{
									if(cbs[0].isSelected())
									{	
										conn.enrollUpdate(cbs[0].getText());
										conn.enrollInsert(collectEnrollData(lbl[0],cbs[0],logged_on), relations[11]);
									}
									if(cbs[1].isSelected())
									{
										conn.enrollUpdate(cbs[1].getText());
										conn.enrollInsert(collectEnrollData(lbl[1],cbs[1],logged_on), relations[11]);
									}
									if(cbs[2].isSelected())
									{
										conn.enrollUpdate(cbs[2].getText());
										conn.enrollInsert(collectEnrollData(lbl[2],cbs[2],logged_on), relations[11]);
									}
									if(cbs[3].isSelected())
									{
										conn.enrollUpdate(cbs[3].getText());
										conn.enrollInsert(collectEnrollData(lbl[3],cbs[3],logged_on), relations[11]);
									}
									if(cbs[4].isSelected())
									{
										conn.enrollUpdate(cbs[4].getText());
										conn.enrollInsert(collectEnrollData(lbl[4],cbs[4],logged_on), relations[11]);
									}
									if(cbs[5].isSelected())
									{
										conn.enrollUpdate(cbs[5].getText());
										conn.enrollInsert(collectEnrollData(lbl[5],cbs[5],logged_on), relations[11]);
									}
									if(cbs[6].isSelected())
									{
										conn.enrollUpdate(cbs[6].getText());
										conn.enrollInsert(collectEnrollData(lbl[6],cbs[6],logged_on), relations[11]);
									}
									if(cbs[7].isSelected())
									{
										conn.enrollUpdate(cbs[7].getText());
										conn.enrollInsert(collectEnrollData(lbl[7],cbs[7],logged_on), relations[11]);
									}
									if(cbs[8].isSelected())
									{
										conn.enrollUpdate(cbs[8].getText());
										conn.enrollInsert(collectEnrollData(lbl[8],cbs[8],logged_on), relations[11]);
									}
									if(cbs[9].isSelected())
									{
										conn.enrollUpdate(cbs[9].getText());
										conn.enrollInsert(collectEnrollData(lbl[9],cbs[9],logged_on), relations[11]);
									}
									if(cbs[10].isSelected())
									{
										conn.enrollUpdate(cbs[10].getText());
										conn.enrollInsert(collectEnrollData(lbl[10],cbs[10],logged_on), relations[11]);
									}
									
								}
								catch(Exception e2)
								{
									System.out.println("Error" + e2);
								}
								
							}); 

		
		hbox.setSpacing(10);
		hbox.setPadding(new Insets(25,10,0,125));
		hbox.getChildren().add(enroll_btn);
		hbox.setAlignment(Pos.BOTTOM_LEFT);
		
		vbox.setSpacing(10);
		vbox.setPadding(new Insets(10));
		
		grid.setHgap(hgap);
		grid.setVgap(vgap);
		grid.setPadding(new Insets(10));
		
		bp.setLeft(grid);
		bp.setBottom(hbox);
		
		return bp;
	}
	
	
	
	//Concats necessary strings for enrollemnt actions
	public String collectEnrollData(Label c_id, CheckBox cbs, Label logged_in)
	{
		StringJoiner sj = new StringJoiner("','","('","')");
		String str = "";
		
		sj.add(c_id.getText());
		sj.add(cbs.getText());
		sj.add(logged_on.getText());

		str =sj.toString();
		return str;
	}
	
	public BorderPane addStudentSearch()
	{
		final double width = 300;
		
		BorderPane bp = new BorderPane();
		GridPane gp = new GridPane();
		VBox vbox = new VBox();

		Label select = new Label("Select");
		gp.add(select, 0, 0);
		TextField select_tf = new TextField();
		select_tf.setMaxWidth(width);
		select_tf.setPromptText("e.g. name");
		gp.add(select_tf, 1, 0); 
		
		Label from = new Label("From");
		gp.add(from, 0, 1); 
		TextField from_tf = new TextField();
		from_tf.setMaxWidth(width);
		from_tf.setPromptText("e.g. professors");
		gp.add(from_tf, 1, 1); 
		
		Label where = new Label("Where");
		gp.add(where, 0, 2); 
		TextField where_tf = new TextField();
		where_tf.setMaxWidth(width);
		where_tf.setPromptText("e.g. age=45");
		gp.add(where_tf, 1, 2); 
		
		Label output = new Label("Output");
		TextArea output_ta = new TextArea();
		output_ta.setPrefColumnCount(12);
		output_ta.setMaxHeight(250);
		
		Button submit = new Button("Submit");
		gp.add(submit, 3, 4); 
		
		BooleanBinding areEmpty = select_tf.textProperty().isEmpty().or(from_tf.textProperty().isEmpty());
		submit.disableProperty().bind(areEmpty);
		
		submit.setOnAction(e -> 
					{
						try
						{
							if(where_tf.getText() == null || where_tf.getText().trim().isEmpty())
							{
								StringBuilder sb = new StringBuilder();
		
								results = conn.searchBasic(select_tf.getText(), from_tf.getText());
								rsmd = results.getMetaData();
								int colNum = rsmd.getColumnCount();
								while(results.next())
								{
									for(int i = 1; i <=colNum; i++)
									{
										String colVal = results.getString(i)+"\n";
										String row = sb.append(colVal).toString();
										output_ta.setText(row);
										
										
									}
									
									
								}
							}
							else
							{
								StringBuilder sb = new StringBuilder();
							
								results = conn.searchWhere(select_tf.getText(), from_tf.getText(), where_tf.getText());
								rsmd = results.getMetaData();
								int colNum = rsmd.getColumnCount();
								while(results.next())
								{
									for(int i = 1; i <=colNum; i++)
									{
										String colVal = results.getString(i)+"\n";
										String row = sb.append(colVal).toString();
										output_ta.setText(row);
										
									
									}
								}
							}
							
						}

						catch(Exception e2)
						{
							
						}
					});
		
		


		vbox.setSpacing(10);
		vbox.setPadding(new Insets(0,50,75,0));
		vbox.getChildren().addAll(output,output_ta);
		vbox.setAlignment(Pos.CENTER);
		
		gp.setVgap(25);
		gp.setHgap(10);
		gp.setPadding(new Insets(0,0,0,25));
		gp.setAlignment(Pos.CENTER_LEFT);
		
		bp.setRight(vbox);
		bp.setCenter(gp);
		
		return bp;
		
	}
	
	//Holds the logout button as well as the logged on label for each scene
	public AnchorPane addAnchorPane()
	{
		AnchorPane anchor = new AnchorPane();
		
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(0,15,25,0));
		hbox.setSpacing(10);
		logout_btn = new Button();
		logout_btn.setText("Logout");
		logout_btn.setPrefWidth(75);
		logout_btn.setOnAction(e -> 
		{
			boolean result = AlertBox.display("Logout Warning");
			if(result)
			{
				window.setScene(login_scene);
				conn.close();
			}
		});

		hbox.setAlignment(Pos.BOTTOM_RIGHT);
		hbox.getChildren().addAll(logged_on,logout_btn);
		
		anchor.getChildren().add(hbox);
		AnchorPane.setBottomAnchor(hbox, 5.0);
		AnchorPane.setRightAnchor(hbox, 5.0);
		return anchor;
	}
	
	



	//Checks ID number with database system
	public void loginCheck() throws Exception
	{
		String staff ="prof";
		String p_relation ="professors";
		String student ="grad";
		String g_relation ="graduates";
		
		
		

		if(login_cb.getValue().equals("Admin"))
		{
			window.setScene(admin_scene);
			
		}
		else if(login_cb.getValue().equals("Staff"))
		{
			if(conn.ssnValidator(staff, id_login.getText(), p_relation) == false)	
			{
				warning_lbl.setText("No ID found!");
				warning_lbl.setTextFill(Color.RED);
				
				
			}
			else
			{

				try
				{
					results = conn.loggedOn(id_login.getText(), p_relation, "prof_ssn");
					while(results.next())
					{
						logged_on.setText(results.getString("name")+" is logged on");
						logged_on.setFont(Font.font("Verdana",12));
					}
					
					
				}
				catch(Exception e)
				{
					System.err.println("Error: " + e); 
					conn.close();
				}
				
				conn.close();
				window.setScene(staff_scene);
				
			} 
		}
		else if(login_cb.getValue().equals("Student"))
		{
			if(conn.ssnValidator(student, id_login.getText(), g_relation) == false)	
			{
				warning_lbl.setText("No ID found!");
				warning_lbl.setTextFill(Color.RED);
				
				
			}
			else
			{

				try
				{
					results = conn.loggedOn(id_login.getText(), g_relation, "grad_ssn");
					while(results.next())
					{
				
						logged_on.setText(results.getString("name"));
						logged_on.setFont(Font.font("Verdana",12));
						
					}
					
					
				}
				catch(Exception e)
				{
					System.err.println("Error: " + e); 
					conn.close();
				}
				
				conn.close();
				window.setScene(student_scene);
			} 
		}
	
		id_login.clear();
	}
}
