import  java.util.Scanner;

public class ElectionSimulator {
    private static final int MAX = 20;
    private static int[] pStatus = new int[MAX];
    private static int n = 0;
    private static int coordinator = 0;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.print("Enter number of processes: ");
        n = scanner.nextInt();
        for (int i = 1; i <= n; i++) {
            System.out.print("Is process " + i + " alive? (0/1): ");
            pStatus[i] = scanner.nextInt();
            if (pStatus[i] == 1) {
                coordinator = i;
            }
        }
        display();
        choice();
    }

    private static void choice() {
        while (true) {
            System.out.println("---------------------------------------------");
            System.out.println("1. Bully Algorithm\n2. Ring Algorithm\n3. Display\n4. Exit");
            System.out.println("---------------------------------------------");
            System.out.print("Enter your choice: ");
            int fchoice = scanner.nextInt();
            switch (fchoice) {
                case 1:
                    bully();
                    break;
                case 2:
                    ring();
                    break;
                case 3:
                    display();
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("Please, enter a valid choice!");
            }
        }
    }

    private static void bully() {
        boolean condition = true;
        while (condition) {
            System.out.println("---------------------------------------------");
            System.out.println("1. Crash\n2. Activate\n3. Display\n4. Exit");
            System.out.println("---------------------------------------------");
            System.out.print("Enter your choice: ");
            int schoice = scanner.nextInt();

            switch (schoice) {
                case 1:
                    System.out.print("Enter process to crash: ");
                    int crash = scanner.nextInt();
                    if (pStatus[crash] != 0) {
                        pStatus[crash] = 0;
                    } else {
                        System.out.println("Process " + crash + " is already dead!");
                        break;
                    }
                    int gid;
                    while (true) {
                        System.out.print("Enter election generator id: ");
                        gid = scanner.nextInt();
                        if (gid == coordinator || pStatus[gid] == 0) {
                            System.out.println("Enter a valid generator id!");
                        } else {
                            break;
                        }
                    }
                    int flag = 0;
                    int subcoordinator = 0;
                    if (crash == coordinator) {
                        for (int i = gid + 1; i <= n; i++) {
                            System.out.println("Message is sent from " + gid + " to " + i);
                            if (pStatus[i] != 0) {
                                subcoordinator = i;
                                System.out.println("Response is sent from " + i + " to " + gid);
                                flag = 1;
                            }
                        }
                        if (flag == 1) {
                            coordinator = subcoordinator;
                        } else {
                            coordinator = gid;
                        }
                    }
                    display();
                    break;

                case 2:
                    System.out.print("Enter Process ID to be activated: ");
                    int activate = scanner.nextInt();
                    if (pStatus[activate] == 0) {
                        pStatus[activate] = 1;
                    } else {
                        System.out.println("Process " + activate + " is already alive!");
                        break;
                    }
                    if (activate == n) {
                        coordinator = n;
                        break;
                    }
                    flag = 0;
                    subcoordinator = 0;
                    for (int i = activate + 1; i <= n; i++) {
                        System.out.println("Message is sent from " + activate + " to " + i);
                        if (pStatus[i] != 0) {
                            subcoordinator = i;
                            System.out.println("Response is sent from " + i + " to " + activate);
                            flag = 1;
                        }
                    }
                    if (flag == 1) {
                        coordinator = subcoordinator;
                    } else {
                        coordinator = activate;
                    }
                    display();
                    break;

                case 3:
                    display();
                    break;

                case 4:
                    condition = false;
                    break;

                default:
                    System.out.println("Please, enter a valid choice!");
            }
        }
    }

    private static void ring() {
        boolean condition = true;
        while (condition) {
            System.out.println("---------------------------------------------");
            System.out.println("1. Crash\n2. Activate\n3. Display\n4. Exit");
            System.out.println("---------------------------------------------");
            System.out.print("Enter your choice: ");
            int tchoice = scanner.nextInt();

            switch (tchoice) {
                case 1:
                    System.out.print("Enter process to crash: ");
                    int crash = scanner.nextInt();
                    if (pStatus[crash] != 0) {
                        pStatus[crash] = 0;
                    } else {
                        System.out.println("Process " + crash + " is already dead!");
                        break;
                    }
                    int gid;
                    while (true) {
                        System.out.print("Enter election generator id: ");
                        gid = scanner.nextInt();
                        if (gid == coordinator) {
                            System.out.println("Please, enter a valid generator id!");
                        } else {
                            break;
                        }
                    }
                    if (crash == coordinator) {
                        int subcoordinator = 1;
                        for (int i = 0; i <= n; i++) {
                            int pid = (i + gid) % (n + 1);
                            if (pid != 0) {
                                if (pStatus[pid] != 0 && subcoordinator < pid) {
                                    subcoordinator = pid;
                                }
                                System.out.println("Election message passed from " + pid + ": #Msg " + subcoordinator);
                            }
                        }
                        coordinator = subcoordinator;
                    }
                    display();
                    break;

                case 2:
                    System.out.print("Enter Process ID to be activated: ");
                    int activate = scanner.nextInt();
                    if (pStatus[activate] == 0) {
                        pStatus[activate] = 1;
                    } else {
                        System.out.println("Process " + activate + " is already alive!");
                        break;
                    }
                    int subcoordinator = activate;
                    for (int i = 0; i <= n; i++) {
                        int pid = (i + activate) % (n + 1);
                        if (pid != 0) {
                            if (pStatus[pid] != 0 && subcoordinator < pid) {
                                subcoordinator = pid;
                            }
                            System.out.println("Election message passed from " + pid + ": #Msg " + subcoordinator);
                        }
                    }
                    coordinator = subcoordinator;
                    display();
                    break;

                case 3:
                    display();
                    break;

                case 4:
                    condition = false;
                    break;

                default:
                    System.out.println("Please, enter a valid choice!");
            }
        }
    }

    private static void display() {
        System.out.println("---------------------------------------------");
        System.out.print("PROCESS:\t");
        for (int i = 1; i <= n; i++) {
            System.out.print(i + "\t");
        }
        System.out.print("\nALIVE:\t\t");
        for (int i = 1; i <= n; i++) {
            System.out.print(pStatus[i] + "\t");
        }
        System.out.println("\n---------------------------------------------");
        System.out.println("COORDINATOR IS " + coordinator);
    }
}
