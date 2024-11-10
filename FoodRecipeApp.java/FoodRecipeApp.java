import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

class Food {
    String name;
    String recipe;
    int rating;

    public Food(String name, String recipe, int rating) {
        this.name = name;
        this.recipe = recipe;
        this.rating = rating;
    }

    @Override
    public String toString() {
        return name + " - Rating: " + rating;
    }
}

public class FoodRecipeApp extends JFrame {

    private static final long serialVersionUID = 1L;

    // UI components
    private JTextField foodNameField;
    private JTextArea recipeArea;
    private JSpinner ratingSpinner;
    private JButton addButton;
    private JButton showButton;
    private JTextArea foodListArea;

    // Database connection variables
    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database_name";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public FoodRecipeApp() {
        setTitle("Food Recipe Sharing and Rating");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize the database and create the table if it doesn't exist
        createTableIfNotExists();

        // Set up layout
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));

        // Food name input
        inputPanel.add(new JLabel("Food Name:"));
        foodNameField = new JTextField();
        inputPanel.add(foodNameField);

        // Recipe input
        inputPanel.add(new JLabel("Recipe:"));
        recipeArea = new JTextArea(4, 20);
        recipeArea.setLineWrap(true);
        recipeArea.setWrapStyleWord(true);
        inputPanel.add(new JScrollPane(recipeArea));

        // Rating input (Spinner for selecting rating)
        inputPanel.add(new JLabel("Rating (1-5):"));
        ratingSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        inputPanel.add(ratingSpinner);

        add(inputPanel, BorderLayout.NORTH);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());

        addButton = new JButton("Add Food");
        addButton.addActionListener(e -> addFoodToDatabase());
        buttonPanel.add(addButton);

        showButton = new JButton("Show Food List");
        showButton.addActionListener(e -> showFoodListFromDatabase());
        buttonPanel.add(showButton);

        add(buttonPanel, BorderLayout.CENTER);

        // Text area to display food list
        foodListArea = new JTextArea(10, 40);
        foodListArea.setEditable(false);
        add(new JScrollPane(foodListArea), BorderLayout.SOUTH);
    }

    // Method to create the database table if it doesn't exist
    private void createTableIfNotExists() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS FoodRecipes (" +
                                    "food_name VARCHAR(255) NOT NULL, " +
                                    "recipe TEXT NOT NULL, " +
                                    "rate_recipe INT CHECK (rate_recipe BETWEEN 1 AND 5))";
            statement.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error setting up database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add food to the database
    private void addFoodToDatabase() {
        String name = foodNameField.getText().trim();
        String recipe = recipeArea.getText().trim();
        int rating = (int) ratingSpinner.getValue();

        if (name.isEmpty() || recipe.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both food name and recipe!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO FoodRecipes (food_name, recipe, rate_recipe) VALUES (?, ?, ?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, recipe);
            preparedStatement.setInt(3, rating);
            preparedStatement.executeUpdate();

            // Clear input fields
            foodNameField.setText("");
            recipeArea.setText("");
            ratingSpinner.setValue(1);

            JOptionPane.showMessageDialog(this, "Food added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding food to database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Display the list of foods from the database
    private void showFoodListFromDatabase() {
        StringBuilder foodInfo = new StringBuilder("Food List:\n");

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM FoodRecipes")) {
            while (resultSet.next()) {
                String name = resultSet.getString("food_name");
                String recipe = resultSet.getString("recipe");
                int rating = resultSet.getInt("rate_recipe");
                foodInfo.append(name).append("\n")
                        .append("Recipe: ").append(recipe).append("\n")
                        .append("Rating: ").append(rating).append("\n\n");
            }

            if (foodInfo.toString().equals("Food List:\n")) {
                JOptionPane.showMessageDialog(this, "No food added yet!", "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                foodListArea.setText(foodInfo.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error retrieving food list from database: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Load JDBC driver (if required by the database)
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            FoodRecipeApp app = new FoodRecipeApp();
            app.setVisible(true);
        });
    }
}
