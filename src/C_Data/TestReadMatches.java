

package C_Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestReadMatches {
    public static void main(String[] args) {
        String filePath = "data/matches.csv";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            System.out.println("📅 Reading matches from: " + filePath + "\n");

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 6) {
                    String matchId = parts[0].trim();
                    String event = parts[1].trim();
                    String date = parts[2].trim();
                    String time = parts[3].trim();
                    String description = parts[4].trim();
                    String available = parts[5].trim();

                    System.out.println("Match ID: " + matchId);
                    System.out.println("Event: " + event);
                    System.out.println("Date: " + date);
                    System.out.println("Time: " + time);
                    System.out.println("Description: " + description);
                    System.out.println("Available Tickets: " + available);
                    System.out.println("---------------------------");
                } else {
                    System.out.println("⚠️ Invalid line format: " + line);
                }
            }

            System.out.println("✅ File read successfully!");

        } catch (IOException e) {
            System.out.println("❌ Error reading file: " + e.getMessage());
        }
    }
}
