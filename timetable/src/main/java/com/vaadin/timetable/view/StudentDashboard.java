package com.vaadin.timetable.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.timetable.AppProperties;
import com.vaadin.timetable.backend.DashboardProjectGrid;
import com.vaadin.timetable.backend.DashboardScheduleGrid;
import com.vaadin.timetable.security.MyUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

@PageTitle("Student Dashboard | Timetable")
@Route(value = "student_dashboard",layout = MainView.class)
public class StudentDashboard extends VerticalLayout {

   private static String url = "jdbc:mysql://localhost:3306/liveTimetable";
   private static String user = "dbms";
   private static  String pwd = "Password_123";

    LocalDate today = LocalDate.now();
    int dayOfWeek = today.getDayOfWeek().getValue();
    LocalDate Sun = today.plusDays(-1*dayOfWeek);
    LocalDate Sat = Sun.plusDays(6);

    Grid<DashboardScheduleGrid> scheduleGrid = new Grid<>(DashboardScheduleGrid.class);
    Grid<DashboardProjectGrid> projectGrid = new Grid<>(DashboardProjectGrid.class);


    public StudentDashboard(){

        setDays(dayOfWeek,Sun,Sat);
        H2 heading  = new H2("Dashboard");
        add(heading);
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        int batchNo=0;
        if (principal instanceof MyUserDetails) {
            batchNo = ((MyUserDetails) principal).getBatchNo();
        }

        int[] classCount = getClassCount(batchNo);
        int projectCnt = getProjectCount(batchNo);
        // Add three panels
        dashboardPanel panel1 = new dashboardPanel(classCount[0],"Classes Scheduled");
        dashboardPanel panel2 = new dashboardPanel(classCount[1],"Classes Cancelled");
        dashboardPanel panel3 = new dashboardPanel(projectCnt,"Projects Due");
        HorizontalLayout cards = new HorizontalLayout(panel1,panel2,panel3);
        cards.addClassName("dashboard-card");
        add(cards);

        // Add two grids - scheduled/cancelled classes and projects assigned

        //First Grid
        H4 grid_1 = new H4("Schedules/Cancellations This Week");
        configureScheduleGrid(scheduleGrid);
        fillScheduleGrid(batchNo);
        add(new VerticalLayout(grid_1,scheduleGrid));

        //Second Grid
        H4 grid_2 = new H4("Projects Due This Week");
        configureProjectGrid();
        fillProjectGrid(batchNo);
        add(new VerticalLayout(grid_2,projectGrid));

    }

    private void setDays(int dayOfWeek, LocalDate sun, LocalDate sat) {
        if(dayOfWeek==7){
            Sun = today;
            Sat = Sun.plusDays(6);
        }
    }

    private int getProjectCount(int batchNo) {
        int projectCnt=0;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url,user,pwd);
            Statement stmt = con.createStatement();
            String sql = "select count(*) as cnt from projectAssign where dueDate >= '"+Sun.toString()+"' and dueDate <= '"+Sat.toString()+"' and batchNo = "+batchNo+";";
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next())
                projectCnt = rs.getInt("cnt");
            rs.close();
            con.close();
        }catch (Exception e){
            Notification.show(e.getLocalizedMessage());
        }

        return projectCnt;
    }

    private int[] getClassCount(int batchNo) {
        int[] classCount = new int[2];
        // Notification.show(Sun.toString()+" "+Sat.toString());
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url,user,pwd);
            Statement stmt = con.createStatement();
            String sql = "select count(*) as cnt from updateTimetable where flag = 'S' and date >= '"+ Sun.toString()+"' and date <='"+Sat.toString()+"' and batchNo = "+batchNo+";";
            ResultSet rs = stmt.executeQuery(sql);
            rs.next();
            classCount[0] = rs.getInt("cnt");
            rs.close();
            sql = "select count(*) as cnt from updateTimetable where flag = 'CW' and date >= '"+Sun.toString()+"' and date <='"+Sat.toString()+"' and batchNo = "+batchNo+";";
            rs = stmt.executeQuery(sql);
            rs.next();
            classCount[1] = rs.getInt("cnt");
            rs.close();
            con.close();
        }catch (Exception e){
            Notification.show(e.getLocalizedMessage());
        }
        return classCount;
    }

    private void fillProjectGrid(int batNo) {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url,user,pwd);
            Statement stmt = con.createStatement();
            String sql = "select Sno,postedDate,courseCode,facultyCode,title,batchNo,dueDate,dueTime from projectAssign where dueDate >='"+Sun.toString()+"' and dueDate <= '"+Sat.toString()+"' and batchNo = "+batNo+" order by postedDate desc;";
            ResultSet rs = stmt.executeQuery(sql);
            Collection<DashboardProjectGrid> data = new ArrayList<>();
            while(rs.next()){
                DashboardProjectGrid entry = new DashboardProjectGrid();
                entry.setPno(rs.getInt("Sno"));
                entry.setCourseCode(rs.getString("courseCode"));
                entry.setFaculty(rs.getString("facultyCode"));
                entry.setDate(rs.getString("postedDate"));
                entry.setTitle(rs.getString("title"));
                //get batch
                int batchNo = rs.getInt("batchNo");
                Statement stmtA = con.createStatement();
                String sqlA = "select batchCode,year from batch where batchNo = "+batchNo+";";
                ResultSet rst = stmtA.executeQuery(sqlA);
                rst.next();
                String batch = rst.getString("batchCode")+" "+rst.getString("year").split("-")[0];
                rst.close();
                entry.setBatch(batch);

                entry.setDue(rs.getString("dueDate")+" "+ rs.getString("dueTime"));
                data.add(entry);
            }
            rs.close();
            con.close();
            projectGrid.setItems(data);
        }catch (Exception e){
            Notification.show(e.getLocalizedMessage());
        }

    }

    private void configureProjectGrid() {
        projectGrid.setHeightByRows(true);
        projectGrid.setSortableColumns();

        projectGrid.setColumnOrder(projectGrid.getColumnByKey("pno"),projectGrid.getColumnByKey("date"),projectGrid.getColumnByKey("batch"),
                projectGrid.getColumnByKey("courseCode"),projectGrid.getColumnByKey("faculty"),projectGrid.getColumnByKey("title"),projectGrid.getColumnByKey("due"));

    }

    private void fillScheduleGrid(int batNo) {
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(url,user,pwd);
            Statement stmt = con.createStatement();
            String sql = "select Sno,date,batchNo,courseCode,facultyCode,flag from updateTimetable where date >='"+Sun.toString()+"' and date <='"+Sat.toString()+"' and batchNo = "+batNo+";";
            ResultSet rs = stmt.executeQuery(sql);
            Collection<DashboardScheduleGrid> data = new ArrayList<>();
            while(rs.next()){
                DashboardScheduleGrid entry = new DashboardScheduleGrid();
                entry.setSno(rs.getInt("Sno"));
                entry.setCourseCode(rs.getString("courseCode"));
                entry.setFaculty(rs.getString("facultyCode"));
                entry.setDate(rs.getString("date"));
                entry.setFlag(rs.getString("flag"));
                int batchNo = rs.getInt("batchNo");
                Statement stmtA = con.createStatement();
                String sqlA = "select batchCode,year from batch where batchNo = "+batchNo+";";
                ResultSet rst = stmtA.executeQuery(sqlA);
                rst.next();
                String batch = rst.getString("batchCode")+" "+rst.getString("year").split("-")[0];
                rst.close();
                entry.setBatch(batch);
                data.add(entry);
            }
            rs.close();
            con.close();
            scheduleGrid.setItems(data);
        }catch (Exception e){
            Notification.show(e.getLocalizedMessage());
        }

    }

    private void configureScheduleGrid(Grid<DashboardScheduleGrid> scheduleGrid) {
        scheduleGrid.setColumnOrder(scheduleGrid.getColumnByKey("sno"),scheduleGrid.getColumnByKey("date"),scheduleGrid.getColumnByKey("batch"),
                scheduleGrid.getColumnByKey("courseCode"),scheduleGrid.getColumnByKey("faculty"),scheduleGrid.getColumnByKey("flag"));

        scheduleGrid.getColumnByKey("sno").setHeader("Id");

        scheduleGrid.setSortableColumns();
        scheduleGrid.setHeightByRows(true);
    }
}
