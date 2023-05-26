import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.*;
import java.time.*;
import java.util.logging.FileHandler;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.concurrent.atomic.AtomicBoolean;





class Employee {
    private int empID;
    private int projcectID;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private static final Logger logger = Logger.getLogger(Employee.class.getName());

    public Employee(int e, int p, LocalDate dateFrom, LocalDate dateTo) {
        this.empID = e;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.projcectID = p;

    }

    public static void init() {
        /**
         * Initialize logger
         */

        try {
            logger.addHandler(new FileHandler("FinalExam.log", true));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setEmpID(int employee) {
        this.empID = employee;

    }

    public int getEmpID() {
        return this.empID;
    }

    public void setProjcectID(int projcectID) {
        this.projcectID = projcectID;
    }

    public int getProjcectID() {
        return this.projcectID;
    }

    public void setDateFrom(LocalDate date) {
        this.dateFrom = date;
    }

    public LocalDate getDateFrom() {
        return this.dateFrom;
    }

    public void setDateTo(LocalDate date) {
        this.dateTo = date;

    }

    public LocalDate getDateTo() {
        return this.dateTo;
    }


    public static List<Employee> extractData(String filePath) {
        /**
         * Method to extract the data from the csv file and store it in ArrayList
         */

        //Array list to store the employees
        List<Employee> employees = new ArrayList<>();

        //Support date formats
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendOptional(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .toFormatter();
        String employee_data;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            // String employee_data;
            //Scanner sc = new Scanner(filePath);
            while ((employee_data = br.readLine()) != null) {

                String[] splitted_employee_data = employee_data.split(", ");

                //Fields
                int id = Integer.parseInt(splitted_employee_data[0]);
                int projectId = Integer.parseInt(splitted_employee_data[1]);
                LocalDate dateFrom = LocalDate.parse(splitted_employee_data[2], formatter);
                LocalDate dateTo = splitted_employee_data[3].equalsIgnoreCase("NULL") ? LocalDate.now() : LocalDate.parse(splitted_employee_data[3], formatter);


                Employee employee = new Employee(id, projectId, dateFrom, dateTo);

                employees.add(employee);

            }
            //sc.close();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.log(Level.INFO, "Extracting data from csv file");
        return employees;

    }



    private static boolean hasOverlap(Employee firstEmp, Employee secondEmp) {
        /**
         * This method checks if the dates have overlap
         */
        //have overlap if (startA <= endB) and (endA >= startB)
        logger.log(Level.INFO, "Checking for overlap");
        return

                // We need the emp1 dateFrom to be before emp2 dateTo
                (firstEmp.getDateTo().isAfter(secondEmp.getDateFrom())
                        && secondEmp.getDateTo().isAfter(firstEmp.getDateFrom()))


                        // We need emp1 dateTo to be after emp2 dateFrom
                        || (firstEmp.getDateFrom().isAfter(secondEmp.getDateFrom())
                        && firstEmp.getDateTo().isAfter(secondEmp.getDateTo()));

    }

    public static ArrayList<Teams> findCollegsWithOverlap(List<Employee> employees) {
        /**
         * Method to find pairs working on the same project
         */

        ArrayList<Teams> teams = new ArrayList<>();


        for (int i = 0; i < employees.size(); i++) {
            for (int j = i + 1; j < employees.size(); j++) {
                Employee emp1 = employees.get(i);
                Employee emp2 = employees.get(j);
                //Checking if the ids are the same
                if (emp1.getProjcectID() == emp2.getProjcectID() && hasOverlap(emp1, emp2)) {
                    // We get the date that employees started working together
                    LocalDate startDate = emp1.getDateFrom().isAfter(emp2.getDateFrom()) ? emp1.getDateFrom() : emp2.getDateFrom();

                    // We get the date that employees finished working together
                    LocalDate endDate = emp1.getDateTo().isBefore(emp2.getDateTo()) ? emp1.getDateTo() : emp2.getDateTo();


                    if (!startDate.isAfter(endDate)) {
                        long overlapDays = ChronoUnit.DAYS.between(startDate, endDate);
                        Teams pair = new Teams(emp1.getEmpID(), emp2.getEmpID(), overlapDays, emp1.getProjcectID(), startDate, endDate);


                        teams.add(pair);


                    }

                }


            }
        }

        logger.log(Level.INFO, "Store the pairs that worked together");
        return teams;

    }

    public static void processEmployeeData(String csvFilePath) {
        /**
         * Method to print the result
         */

        //Result from extracting the data
        List<Employee> employees = Employee.extractData(csvFilePath);

        //Result from finding pairs
        List<Teams> pairs = Employee.findCollegsWithOverlap(employees);



        Set<String> printedPairs = new HashSet<>();
        Map<String, Integer> totalDaysWorked = new HashMap<>();

        for (Teams t : pairs) {
            String pair1 = t.getEmployee1Id() + "," + t.getEmployee2Id();
            String pair2 = t.getEmployee2Id() + "," + t.getEmployee1Id();
            int days = (int) t.getDaysWorkedTogether();
            totalDaysWorked.put(pair1, totalDaysWorked.getOrDefault(pair1, 0) + days);
            totalDaysWorked.put(pair2, totalDaysWorked.getOrDefault(pair2, 0) + days);

        }



        long currentProjectID = -1;
        for (Teams t : pairs) {
            String pair1 = t.getEmployee1Id() + "," + t.getEmployee2Id();
            String pair2 = t.getEmployee2Id() + "," + t.getEmployee1Id();
            int totalDays = totalDaysWorked.get(pair1);
            if (!printedPairs.contains(pair1) && !printedPairs.contains(pair2)) {
                System.out.println(t.getEmployee1Id() + ", " + t.getEmployee2Id() + ", " + totalDays);
                printedPairs.add(pair1);
            }
            if (t.getProjectIDtogether() != currentProjectID) {
                System.out.print(t.getProjectIDtogether());
                currentProjectID = t.getProjectIDtogether();
            }
            System.out.println(", " + t.getDaysWorkedTogether());
        }
        logger.log(Level.INFO, "Print the result.");
    }

}




class Teams {
    private long employee1Id;
    private long employee2Id;
    private long projectIDtogether;
    private long daysWorkedTogether;

    private LocalDate startDate;

    private LocalDate endDate;

    private long totalDaysWorkedTogether;

    public Teams(long employee1, long employee2, long daysWorkedTogether, long projectIDtogether, LocalDate startDate, LocalDate endDate) {
        this.employee1Id = employee1;
        this.employee2Id = employee2;
        this.daysWorkedTogether = daysWorkedTogether;
        this.projectIDtogether = projectIDtogether;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalDaysWorkedTogether += daysWorkedTogether;


    }

    public long getTotalDaysWorkedTogether() {
        return this.totalDaysWorkedTogether;
    }


    public long getEmployee1Id() {
        return this.employee1Id;
    }

    public long getEmployee2Id() {
        return this.employee2Id;
    }


    public long getDaysWorkedTogether() {
        return this.daysWorkedTogether;
    }

    public long getProjectIDtogether() {
        return this.projectIDtogether;
    }


    public void addOverlap(long overlap) {
        this.daysWorkedTogether += overlap;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

}
    public class Main {
        public static void main(String[] args) {
           Employee.processEmployeeData("inputData.csv");
        }
    }
