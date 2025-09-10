import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileHandlingUtility {
    private static final Scanner SC = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.println("=== File Handling Utility ===");
        while (true) {
            printMenu();
            String choice = SC.nextLine().trim();
            switch (choice) {
                case "1": createFile(); break;
                case "2": writeFile(false); break;     // overwrite
                case "3": writeFile(true); break;      // append
                case "4": readFile(); break;
                case "5": searchAndReplace(); break;
                case "6": deleteLine(); break;
                case "7": renameFile(); break;
                case "8": deleteFile(); break;
                case "9": listFiles(); break;
                case "0":
                    System.out.println("Exiting. Goodbye!");
                    SC.close();
                    return;
                default:
                    System.out.println("Invalid choice — enter a number from the menu.");
            }
            System.out.println(); // blank line between operations
        }
    }

    private static void printMenu() {
        System.out.println("Choose an operation:");
        System.out.println("1) Create a file");
        System.out.println("2) Write to file (overwrite)");
        System.out.println("3) Append to file");
        System.out.println("4) Read/display file");
        System.out.println("5) Search & replace (in file)");
        System.out.println("6) Delete line (by content or line number)");
        System.out.println("7) Rename/move file");
        System.out.println("8) Delete file");
        System.out.println("9) List files in directory");
        System.out.println("0) Exit");
        System.out.print("Enter choice: ");
    }

    private static Path getPathFromUser(String prompt) {
        System.out.print(prompt);
        String input = SC.nextLine().trim();
        return Paths.get(input);
    }

    private static void createFile() {
        Path p = getPathFromUser("Enter path for new file (relative or absolute): ");
        try {
            if (Files.exists(p)) {
                System.out.println("File already exists: " + p.toAbsolutePath());
            } else {
                Files.createDirectories(p.getParent() == null ? Paths.get(".") : p.getParent());
                Files.createFile(p);
                System.out.println("Created file: " + p.toAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("Error creating file: " + e.getMessage());
        }
    }

    private static void writeFile(boolean append) {
        Path p = getPathFromUser("Enter file path: ");
        try {
            Files.createDirectories(p.getParent() == null ? Paths.get(".") : p.getParent());
            System.out.println("Enter text. To finish, enter a single line with only a dot: .");
            StringBuilder sb = new StringBuilder();
            while (true) {
                String line = SC.nextLine();
                if (line.equals(".")) break;
                sb.append(line).append(System.lineSeparator());
            }
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            if (append) {
                Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println("Appended to " + p.toAbsolutePath());
            } else {
                Files.write(p, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Wrote to " + p.toAbsolutePath());
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    private static void readFile() {
        Path p = getPathFromUser("Enter file path to read: ");
        if (!Files.exists(p)) {
            System.out.println("File not found: " + p.toAbsolutePath());
            return;
        }
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            System.out.println("---- File Contents: " + p.toAbsolutePath() + " ----");
            for (int i = 0; i < lines.size(); i++) {
                System.out.printf("%4d: %s%n", i + 1, lines.get(i));
            }
            System.out.println("---- End of file ----");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    private static void searchAndReplace() {
        Path p = getPathFromUser("Enter file path: ");
        if (!Files.exists(p)) {
            System.out.println("File not found.");
            return;
        }
        System.out.print("Enter target text to replace: ");
        String target = SC.nextLine();
        System.out.print("Enter replacement text: ");
        String replacement = SC.nextLine();
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            int replacements = 0;
            List<String> newLines = new ArrayList<>();
            for (String line : lines) {
                String newLine = line.replace(target, replacement);
                if (!newLine.equals(line)) {
                    replacements += countOccurrences(line, target);
                }
                newLines.add(newLine);
            }
            Files.write(p, newLines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Replacements made: " + replacements);
        } catch (IOException e) {
            System.out.println("Error during search & replace: " + e.getMessage());
        }
    }

    private static int countOccurrences(String haystack, String needle) {
        if (needle.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    private static void deleteLine() {
        Path p = getPathFromUser("Enter file path: ");
        if (!Files.exists(p)) {
            System.out.println("File not found.");
            return;
        }
        System.out.println("Delete by: 1) content  2) line number");
        String ch = SC.nextLine().trim();
        try {
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            if (ch.equals("1")) {
                System.out.print("Enter text to delete lines containing it: ");
                String text = SC.nextLine();
                List<String> filtered = lines.stream()
                        .filter(line -> !line.contains(text))
                        .collect(Collectors.toList());
                Files.write(p, filtered, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Removed lines containing: " + text);
            } else if (ch.equals("2")) {
                System.out.print("Enter line number to delete (1-based): ");
                int ln = Integer.parseInt(SC.nextLine().trim());
                if (ln < 1 || ln > lines.size()) {
                    System.out.println("Invalid line number.");
                    return;
                }
                lines.remove(ln - 1);
                Files.write(p, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Deleted line " + ln);
            } else {
                System.out.println("Invalid option.");
            }
        } catch (IOException e) {
            System.out.println("Error modifying file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        }
    }

    private static void renameFile() {
        Path src = getPathFromUser("Enter current file path: ");
        if (!Files.exists(src)) {
            System.out.println("File not found.");
            return;
        }
        System.out.print("Enter new path/name (can be new directory or filename): ");
        Path target = Paths.get(SC.nextLine().trim());
        try {
            Files.createDirectories(target.getParent() == null ? Paths.get(".") : target.getParent());
            Path moved = Files.move(src, target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File moved/renamed to: " + moved.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error renaming/moving file: " + e.getMessage());
        }
    }

    private static void deleteFile() {
        Path p = getPathFromUser("Enter file path to delete: ");
        if (!Files.exists(p)) {
            System.out.println("File not found.");
            return;
        }
        System.out.print("Are you sure? (y/n): ");
        String confirm = SC.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println("Cancelled deletion.");
            return;
        }
        try {
            Files.delete(p);
            System.out.println("Deleted: " + p.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("Error deleting file: " + e.getMessage());
        }
    }

    private static void listFiles() {
        Path dir = getPathFromUser("Enter directory path (or press Enter for current directory): ");
        if (dir.toString().isEmpty()) dir = Paths.get(".");
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            System.out.println("Directory not found: " + dir.toAbsolutePath());
            return;
        }
        try {
            System.out.println("Files in " + dir.toAbsolutePath() + ":");
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir)) {
                for (Path p : ds) {
                    String type = Files.isDirectory(p) ? "<DIR>" : String.valueOf(Files.size(p)) + " bytes";
                    System.out.printf(" - %s    %s%n", p.getFileName(), type);
                }
            }
        } catch (IOException e) {
            System.out.println("Error listing directory: " + e.getMessage());
        }
    }
}
