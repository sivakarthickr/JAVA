import java.util.Scanner;

public class MainApp {
    public static void main(String[] args) {
        AcademicDAO dao = new AcademicDAO();
        Scanner sc = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n--- KEC R2024 Evaluation System ---");
            System.out.println("1. Register Student");
            System.out.println("2. Enter Marks (Incremental)");
            System.out.println("3. Generate Mark Sheet");
            System.out.println("4. Exit");
            System.out.print("Choice: ");
            
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid number.");
                sc.next();
                continue;
            }

            int choice = sc.nextInt();
            try {
                switch (choice) {
                    case 1:
                        System.out.print("Roll No: "); 
                        String roll = sc.next();
                        System.out.print("Name: "); 
                        sc.nextLine(); // consume leftover newline
                        String name = sc.nextLine();
                        System.out.print("Dept: "); 
                        String dept = sc.next();
                        System.out.print("Sem: "); 
                        int sem = sc.nextInt();
                        dao.addStudent(roll, name, dept, sem);
                        System.out.println("Student Registered.");
                        break;

                    case 2:
                        System.out.print("Enrollment ID: "); 
                        int eid = sc.nextInt();
                        System.out.print("Phase (CIA1/CIA2/CIA_OTHER/ESE): "); 
                        String phase = sc.next();
                        System.out.print("Marks: "); 
                        double m = sc.nextDouble();
                        dao.updateMarks(eid, phase, m);
                        System.out.println("Marks Updated.");
                        break;

                    case 3:
                        System.out.print("Roll No: "); 
                        String r = sc.next();
                        dao.generateGradeSheet(r);
                        break;

                    case 4:
                        System.out.println("Exiting KEC Evaluation System. Goodbye!");
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice. Please select 1-4.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
