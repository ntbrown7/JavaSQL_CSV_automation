import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CSVLoader {

    private static final String SQL_INSERT = "INSERT INTO metagenome (sample, forest, biome, lat, lon, energy2, carbs2, operon_number2, perc_bac, perc_euks, growth_bc, n50, ags_full, gc, ags, perc_genomes, operon_number, growth_frag, growth_contigs, perc_annotated) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String CSV_FILE = "[PUT FILE PATH HERE]";

    private static int countLines(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            int lines = 0;
            while (reader.readLine() != null) lines++;
            return lines;
        }
    }

    public static void main(String[] args) {

        // Update these variables to your database connection details
        String url = "jdbc:mysql://localhost:3306/database_name";
        String user = "username"; 
        String password = "password";

        try {
            // Count the total number of lines in the CSV file
            int totalLines = countLines(CSV_FILE);
            int batchSize = Math.max(1, totalLines / 10); // Adjust the batch size as needed

            // Establish a connection to the database
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(false); // Turn off auto-commit for batch execution

            // Read the CSV file
            BufferedReader lineReader = new BufferedReader(new FileReader(CSV_FILE));
            String lineText;

            // Skip the header line
            lineReader.readLine();

            // Prepare the SQL statement
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT);
            int count = 0;

            while ((lineText = lineReader.readLine()) != null) {
                String[] data = lineText.split(",");
                // Assuming the CSV columns directly map to the database columns
                statement.setString(1, data[0]); // sample
                statement.setString(2, data[1]); // forest
                statement.setString(3, data[2]); // biome
                statement.setDouble(4, Double.parseDouble(data[3])); // lat
                statement.setDouble(5, Double.parseDouble(data[4])); // lon
                statement.setDouble(6, Double.parseDouble(data[5])); // energy2
                statement.setDouble(7, Double.parseDouble(data[6])); // carbs2
                statement.setDouble(8, Double.parseDouble(data[7])); // operon_number2
                statement.setDouble(9, Double.parseDouble(data[8])); // perc_bac
                statement.setDouble(10, Double.parseDouble(data[9])); // perc_euks
                statement.setDouble(11, Double.parseDouble(data[10])); // growth_bc
                statement.setDouble(12, Double.parseDouble(data[11])); // n50
                statement.setDouble(13, Double.parseDouble(data[12])); // ags_full
                statement.setDouble(14, Double.parseDouble(data[13])); // gc
                statement.setDouble(15, Double.parseDouble(data[14])); // ags
                statement.setDouble(16, Double.parseDouble(data[15])); // perc_genomes
                statement.setDouble(17, Double.parseDouble(data[16])); // operon_number
                statement.setDouble(18, Double.parseDouble(data[17])); // growth_frag
                statement.setDouble(19, Double.parseDouble(data[18])); // growth_contigs
                statement.setDouble(20, Double.parseDouble(data[19])); // perc_annotated

                statement.addBatch();

                if (++count % batchSize == 0) {
                    statement.executeBatch(); // Execute per batch size
                    connection.commit(); // Commit the batch
                }
            }

            // Execute the remaining batch
            statement.executeBatch();
            connection.commit(); // Commit the last batch

            // Clean up
            lineReader.close();
            statement.close();
            connection.close();

            System.out.println("Data has been inserted successfully.");
        }
         catch (IOException ex) {
            System.err.println("An IOException was caught!");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.err.println("An SQLException was caught!");
            ex.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }
}
