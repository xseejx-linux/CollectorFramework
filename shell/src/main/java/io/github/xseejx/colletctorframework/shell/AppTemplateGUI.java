package io.github.xseejx.colletctorframework.shell;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.IOException;
import java.util.Scanner;

// Comment out these imports if the classes don't exist yet
// import io.github.xseejx.colletctorframework.core.service.ServiceManager;
// import io.github.xseejx.colletctorframework.core.service.TaskManager;
// import io.github.xseejx.colletctorframework.core.service.TaskModel;

public class AppTemplateGUI {
    
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";
    private static final String BOLD = "\u001B[1m";
    private static final String REVERSE = "\u001B[7m";
    
    // Global scanner
    private static Scanner globalScanner;
    
    // Replace with your actual service manager when available
    private Object serviceManager; // Temporary - replace with ServiceManager
    private Object taskManager;     // Temporary - replace with TaskManager
    private AtomicBoolean running;
    private int currentSelection;
    private List<String> mainMenuItems;
    
    // Static initializer block
    static {
        globalScanner = new Scanner(System.in);
    }
    
    public AppTemplateGUI() {
        // Initialize your actual services here when available
        // this.serviceManager = ServiceManager.begin();
        // this.taskManager = new TaskManager();
        this.serviceManager = null; // Placeholder
        this.taskManager = null;     // Placeholder
        this.running = new AtomicBoolean(true);
        this.currentSelection = 0;
        this.mainMenuItems = List.of(
            "Synchronous Execution",
            "Asynchronous Execution", 
            "Batch Execution",
            "Scheduled Tasks",
            "Service Information",
            "Exit"
        );
    }
    
    public static void main(String[] args) {
        AppTemplateGUI app = new AppTemplateGUI();
        app.start();
    }
    
    public void start() {
        clearScreen();
        
        while (running.get()) {
            drawMainMenu();
            handleMainMenuInput();
        }
        
        shutdown();
    }
    
    private void drawMainMenu() {
        clearScreen();
        printHeader();
        
        System.out.println(YELLOW + BOLD + "┌────────────────────────────────────────────────────────┐" + RESET);
        System.out.println(YELLOW + BOLD + "│                      MAIN MENU                            │" + RESET);
        System.out.println(YELLOW + BOLD + "├────────────────────────────────────────────────────────┤" + RESET);
        
        for (int i = 0; i < mainMenuItems.size(); i++) {
            String prefix = (i == currentSelection) ? REVERSE + " ▶ " : "   ";
            String suffix = (i == currentSelection) ? RESET : "";
            String color = (i == mainMenuItems.size() - 1) ? RED : CYAN;
            
            if (i == currentSelection) {
                System.out.println(color + prefix + String.format("%-50s", mainMenuItems.get(i)) + suffix + color + "│" + RESET);
            } else {
                System.out.println(color + "│  " + prefix + String.format("%-49s", mainMenuItems.get(i)) + "│" + RESET);
            }
        }
        
        System.out.println(YELLOW + BOLD + "└────────────────────────────────────────────────────────┘" + RESET);
        System.out.println();
        System.out.println(CYAN + "Use W/S or ↑/↓ arrows to navigate, " + GREEN + "Enter" + CYAN + " to select, " + RED + "Ctrl+O" + CYAN + " to execute" + RESET);
        System.out.println(CYAN + "Press 'Q' to quit at any time" + RESET);
        System.out.println();
        System.out.println(WHITE + "Current Time: " + YELLOW + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + RESET);
    }
    
    private void handleMainMenuInput() {
        try {
            int key = readKey();
            
            if (key == 113 || key == 81) {
                running.set(false);
                return;
            }
            
            if (key == 'w' || key == 'W' || key == 655 || key == 65) {
                currentSelection--;
                if (currentSelection < 0) currentSelection = mainMenuItems.size() - 1;
            } else if (key == 's' || key == 'S' || key == 656 || key == 66) {
                currentSelection++;
                if (currentSelection >= mainMenuItems.size()) currentSelection = 0;
            } else if (key == 10 || key == 13) {
                executeSelectedOption();
            } else if (key == 15) {
                executeCurrentTask();
            }
        } catch (Exception e) {
            printError("Input error: " + e.getMessage());
        }
    }
    
    private void executeSelectedOption() {
        switch (currentSelection) {
            case 0:
                syncExecutionMenu();
                break;
            case 1:
                asyncExecutionMenu();
                break;
            case 2:
                batchExecutionMenu();
                break;
            case 3:
                scheduledTasksMenu();
                break;
            case 4:
                showServiceInfoWithParams();
                break;
            case 5:
                running.set(false);
                break;
        }
    }
    
    private void executeCurrentTask() {
        printInfo("\nExecuting current operation...");
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        waitForEnter();
    }
    
    private void syncExecutionMenu() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new ServiceSelectorParameter("Service", List.of("generic.test", "hardware.cpu")));
        parameters.add(new StringParameter("Test Message", "Enter test message"));
        parameters.add(new BooleanParameter("Enable Feature", true));
        parameters.add(new BooleanParameter("Include Per-Core Info", true));
        parameters.add(new BooleanParameter("Include Temperature", false));
        
        ParameterScreen screen = new ParameterScreen("Synchronous Execution", parameters);
        boolean executed = screen.show();
        
        if (executed && running.get()) {
            String service = parameters.get(0).getValue().toString();
            
            // Simulate execution since real services might not be available
            printInfo("Executing " + service + " with parameters...");
            
            // When your real services are available, uncomment this:
            /*
            try {
                String result;
                if (service.equals("generic.test")) {
                    result = serviceManager.activateServiceSync("generic.test", Map.of(
                        "value1", parameters.get(2).getValue(),
                        "value2", parameters.get(1).getValue()
                    ));
                } else {
                    result = serviceManager.activateServiceSync("hardware.cpu", Map.of(
                        "includePerCore", parameters.get(3).getValue(),
                        "includeTemperature", parameters.get(4).getValue()
                    ));
                }
                printSuccess("Result: " + result);
            } catch (Exception e) {
                printError("Error: " + e.getMessage());
            }
            */
            
            printSuccess("Operation completed successfully!");
            waitForEnter();
        }
    }
    
    private void asyncExecutionMenu() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new StringParameter("Test Message", "Enter test message"));
        parameters.add(new BooleanParameter("Enable Feature", true));
        
        ParameterScreen screen = new ParameterScreen("Asynchronous Execution", parameters);
        boolean executed = screen.show();
        
        if (executed && running.get()) {
            printInfo("\nStarting asynchronous execution...");
            
            // Simulate async execution
            CompletableFuture.runAsync(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                System.out.println();
                printSuccess("Async operation completed!");
            });
            
            waitForEnter();
        }
    }
    
    private void batchExecutionMenu() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new NumberParameter("Number of Requests", 1, 10, 2));
        
        ParameterScreen screen = new ParameterScreen("Batch Execution Setup", parameters);
        boolean executed = screen.show();
        
        if (executed && running.get()) {
            int numRequests = (Integer) parameters.get(0).getValue();
            printInfo("Executing batch of " + numRequests + " requests...");
            printSuccess("Batch completed successfully!");
            waitForEnter();
        }
    }
    
    private void scheduledTasksMenu() {
        boolean inSchedulerMenu = true;
        
        while (inSchedulerMenu && running.get()) {
            List<String> schedulerOptions = List.of(
                "Create New Scheduled Task",
                "List All Scheduled Tasks",
                "Delete Scheduled Task",
                "Back to Main Menu"
            );
            
            int selection = 0;
            
            while (inSchedulerMenu && running.get()) {
                clearScreen();
                printSubHeader("SCHEDULED TASKS");
                
                System.out.println(YELLOW + BOLD + "┌────────────────────────────────────────────────────────┐" + RESET);
                
                for (int i = 0; i < schedulerOptions.size(); i++) {
                    String prefix = (i == selection) ? REVERSE + " ▶ " : "   ";
                    String suffix = (i == selection) ? RESET : "";
                    
                    if (i == selection) {
                        System.out.println(CYAN + prefix + String.format("%-50s", schedulerOptions.get(i)) + suffix + CYAN + "│" + RESET);
                    } else {
                        System.out.println(CYAN + "│  " + prefix + String.format("%-49s", schedulerOptions.get(i)) + "│" + RESET);
                    }
                }
                
                System.out.println(YELLOW + BOLD + "└────────────────────────────────────────────────────────┘" + RESET);
                System.out.println();
                System.out.println(CYAN + "Use W/S or ↑/↓ arrows to navigate, " + GREEN + "Enter" + CYAN + " to select, " + RED + "Ctrl+O" + CYAN + " to execute" + RESET);
                System.out.println(CYAN + "Press 'Q' to quit" + RESET);
                
                try {
                    int key = readKey();
                    
                    if (key == 113 || key == 81) {
                        running.set(false);
                        return;
                    }
                    
                    if (key == 'w' || key == 'W' || key == 655 || key == 65) {
                        selection--;
                        if (selection < 0) selection = schedulerOptions.size() - 1;
                    } else if (key == 's' || key == 'S' || key == 656 || key == 66) {
                        selection++;
                        if (selection >= schedulerOptions.size()) selection = 0;
                    } else if (key == 10 || key == 13) {
                        switch (selection) {
                            case 0:
                                createScheduledTask();
                                break;
                            case 1:
                                listScheduledTasks();
                                break;
                            case 2:
                                deleteScheduledTask();
                                break;
                            case 3:
                                inSchedulerMenu = false;
                                break;
                        }
                        break;
                    } else if (key == 15) {
                        executeCurrentTask();
                    }
                } catch (Exception e) {
                    printError("Input error: " + e.getMessage());
                }
            }
        }
    }
    
    private void createScheduledTask() {
        if (!running.get()) return;
        
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new ServiceSelectorParameter("Service", List.of("generic.test", "hardware.cpu")));
        parameters.add(new StringParameter("Cron Expression", "Enter cron expression (e.g., * * * * * ?)"));
        parameters.add(new StringParameter("Parameter Key", "Enter parameter key"));
        parameters.add(new StringParameter("Parameter Value", "Enter parameter value"));
        
        ParameterScreen screen = new ParameterScreen("Create Task", parameters);
        boolean executed = screen.show();
        
        if (executed && running.get()) {
            printSuccess("Task created successfully!");
            waitForEnter();
        }
    }
    
    private void listScheduledTasks() {
        if (!running.get()) return;
        
        clearScreen();
        printSubHeader("SCHEDULED TASKS LIST");
        printInfo("\nCurrently active scheduled tasks:");
        printInfo("No tasks scheduled");
        waitForEnter();
    }
    
    private void deleteScheduledTask() {
        if (!running.get()) return;
        
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new StringParameter("Task ID", "Enter Task ID to delete"));
        
        ParameterScreen screen = new ParameterScreen("Delete Task", parameters);
        boolean executed = screen.show();
        
        if (executed && running.get()) {
            printSuccess("Task deleted successfully!");
            waitForEnter();
        }
    }
    
    private void showServiceInfoWithParams() {
        if (!running.get()) return;
        
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new ServiceSelectorParameter("Select Service", List.of("generic.test", "hardware.cpu")));
        
        ParameterScreen screen = new ParameterScreen("Service Details", parameters);
        boolean executed = screen.show();
        
        if (executed && running.get()) {
            String service = parameters.get(0).getValue().toString();
            
            System.out.println(CYAN + BOLD + "\nService: " + GREEN + service + RESET);
            System.out.println(CYAN + BOLD + "Parameters:" + RESET);
            
            if (service.equals("generic.test")) {
                System.out.println(WHITE + "  • value1 (boolean) - Enable feature" + RESET);
                System.out.println(WHITE + "  • value2 (string) - Test message" + RESET);
            } else {
                System.out.println(WHITE + "  • includePerCore (boolean) - Show per-core information" + RESET);
                System.out.println(WHITE + "  • includeTemperature (boolean) - Show CPU temperature" + RESET);
            }
            
            waitForEnter();
        }
    }
    
    // Parameter classes
    abstract static class Parameter {
        String name;
        Object value;
        
        Parameter(String name, Object defaultValue) {
            this.name = name;
            this.value = defaultValue;
        }
        
        abstract String getDisplayValue();
        abstract void edit();
        abstract Object getValue();
    }
    
    static class BooleanParameter extends Parameter {
        BooleanParameter(String name, boolean defaultValue) {
            super(name, defaultValue);
        }
        
        @Override
        String getDisplayValue() {
            return ((Boolean) value) ? GREEN + "true" + RESET : RED + "false" + RESET;
        }
        
        @Override
        void edit() {
            value = !((Boolean) value);
        }
        
        @Override
        Boolean getValue() {
            return (Boolean) value;
        }
    }
    
    static class StringParameter extends Parameter {
        String prompt;
        
        StringParameter(String name, String prompt) {
            super(name, "");
            this.prompt = prompt;
        }
        
        @Override
        String getDisplayValue() {
            String val = value.toString();
            return val.isEmpty() ? CYAN + "<empty>" + RESET : YELLOW + val + RESET;
        }
        
        @Override
        void edit() {
            System.out.print("\n" + CYAN + prompt + ": " + RESET);
            System.out.flush();
            
            String input = globalScanner.nextLine();
            if (!input.isEmpty()) {
                value = input;
            }
            
            System.out.print("\033[1A\033[2K");
            System.out.flush();
        }
        
        @Override
        String getValue() {
            return value.toString();
        }
    }
    
    static class NumberParameter extends Parameter {
        int min, max;
        
        NumberParameter(String name, int min, int max, int defaultValue) {
            super(name, defaultValue);
            this.min = min;
            this.max = max;
        }
        
        @Override
        String getDisplayValue() {
            return CYAN + value.toString() + RESET;
        }
        
        @Override
        void edit() {
            System.out.print("\n" + CYAN + "Enter value (" + min + "-" + max + "): " + RESET);
            System.out.flush();
            
            String input = globalScanner.nextLine();
            try {
                int num = Integer.parseInt(input);
                if (num >= min && num <= max) {
                    value = num;
                }
            } catch (NumberFormatException e) {
                // Keep old value
            }
            
            System.out.print("\033[1A\033[2K");
            System.out.flush();
        }
        
        @Override
        Integer getValue() {
            return (Integer) value;
        }
    }
    
    static class ServiceSelectorParameter extends Parameter {
        List<String> options;
        int selectedIndex;
        
        ServiceSelectorParameter(String name, List<String> options) {
            super(name, options.get(0));
            this.options = options;
            this.selectedIndex = 0;
        }
        
        @Override
        String getDisplayValue() {
            return GREEN + "[" + options.get(selectedIndex) + "]" + RESET;
        }
        
        @Override
        void edit() {
            selectedIndex = (selectedIndex + 1) % options.size();
            value = options.get(selectedIndex);
        }
        
        @Override
        String getValue() {
            return options.get(selectedIndex);
        }
    }
    
    class ParameterScreen {
        private String title;
        private List<Parameter> parameters;
        private int currentParamIndex;
        
        ParameterScreen(String title, List<Parameter> parameters) {
            this.title = title;
            this.parameters = parameters;
            this.currentParamIndex = 0;
        }
        
        boolean show() {
            while (true) {
                clearScreen();
                printSubHeader(title);
                
                System.out.println(YELLOW + BOLD + "┌────────────────────────────────────────────────────────┐" + RESET);
                System.out.println(YELLOW + BOLD + "│                     PARAMETERS                           │" + RESET);
                System.out.println(YELLOW + BOLD + "├────────────────────────────────────────────────────────┤" + RESET);
                
                for (int i = 0; i < parameters.size(); i++) {
                    Parameter param = parameters.get(i);
                    String prefix = (i == currentParamIndex) ? REVERSE + " ▶ " : "   ";
                    String suffix = (i == currentParamIndex) ? RESET : "";
                    String displayValue = param.getDisplayValue();
                    
                    System.out.println(CYAN + "│" + prefix + String.format("%-20s", param.name + ":") + 
                                     String.format("%-28s", displayValue) + suffix + CYAN + "│" + RESET);
                }
                
                System.out.println(YELLOW + BOLD + "├────────────────────────────────────────────────────────┤" + RESET);
                System.out.println(CYAN + "│  " + String.format("%-55s", "Use W/S or ↑/↓ to navigate parameters") + "│" + RESET);
                System.out.println(CYAN + "│  " + String.format("%-55s", "Press Enter to edit/toggle value") + "│" + RESET);
                System.out.println(CYAN + "│  " + String.format("%-55s", "Press " + GREEN + "Ctrl+O" + CYAN + " to execute") + "│" + RESET);
                System.out.println(CYAN + "│  " + String.format("%-55s", "Press 'Q' to quit") + "│" + RESET);
                System.out.println(YELLOW + BOLD + "└────────────────────────────────────────────────────────┘" + RESET);
                
                try {
                    int key = readKey();
                    
                    if (key == 113 || key == 81) {
                        running.set(false);
                        return false;
                    }
                    
                    if (key == 'w' || key == 'W' || key == 655 || key == 65) {
                        currentParamIndex--;
                        if (currentParamIndex < 0) currentParamIndex = parameters.size() - 1;
                    } else if (key == 's' || key == 'S' || key == 656 || key == 66) {
                        currentParamIndex++;
                        if (currentParamIndex >= parameters.size()) currentParamIndex = 0;
                    } else if (key == 10 || key == 13) {
                        Parameter currentParam = parameters.get(currentParamIndex);
                        currentParam.edit();
                    } else if (key == 15) {
                        return true;
                    }
                } catch (Exception e) {
                    printError("Input error: " + e.getMessage());
                }
            }
        }
    }
    
    private int readKey() throws IOException {
        int key = System.in.read();
        
        if (key == 27) {
            int next1 = System.in.read();
            if (next1 == 91) {
                int next2 = System.in.read();
                if (next2 == 65) return 655;
                if (next2 == 66) return 656;
                if (next2 == 67) return 657;
                if (next2 == 68) return 658;
            }
            return key;
        }
        
        return key;
    }
    
    private void printHeader() {
        System.out.println(CYAN + BOLD);
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                 COLLECTOR FRAMEWORK TERMINAL                  ║");
        System.out.println("║                         Version 2.0                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();
    }
    
    private void printSubHeader(String title) {
        System.out.println(CYAN + BOLD + "┌────────────────────────────────────────────────────────┐" + RESET);
        System.out.println(CYAN + BOLD + "│  " + String.format("%-52s", title) + "│" + RESET);
        System.out.println(CYAN + BOLD + "└────────────────────────────────────────────────────────┘" + RESET);
        System.out.println();
    }
    
    private void clearScreen() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
        
        // Windows fallback
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void printInfo(String message) {
        System.out.println(BLUE + "ℹ " + message + RESET);
    }
    
    private void printSuccess(String message) {
        System.out.println(GREEN + "✓ " + message + RESET);
    }
    
    private void printError(String message) {
        System.out.println(RED + "✗ " + message + RESET);
    }
    
    private void waitForEnter() {
        System.out.print(YELLOW + "\nPress Enter to continue..." + RESET);
        try {
            System.in.read();
            System.in.skip(System.in.available());
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void shutdown() {
        clearScreen();
        printInfo("\nShutting down Collector Framework...");
        
        if (globalScanner != null) {
            globalScanner.close();
            printInfo("Scanner closed");
        }
        
        printSuccess("Shutdown complete. Goodbye!");
        System.exit(0);
    }
}