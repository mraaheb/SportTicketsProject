package client;   

import java.io.*;

public class TestReadUsers {
    public static void main(String[] args) {
String filePath = "data/users.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            System.out.println("Reading users from " + filePath + "...\n");

            while ((line = br.readLine()) != null) {
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String username = parts[0];
                    String password = parts[1];
                    System.out.println("User: " + username + " | Password: " + password);
                }
            }

            System.out.println("\n✅ File read successfully!");
        } catch (FileNotFoundException e) {
            System.out.println("❌ File not found! data.");
        } catch (IOException e) {
            System.out.println("❌ Error reading file: " + e.getMessage());
        }
    }
}
