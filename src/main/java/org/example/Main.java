import java.sql.*;

public class Main {
    private static final String URL = "jdbc:sqlite:shop.db";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL)) {
            createTables(connection);
            insertTestData(connection);

            System.out.println("=== DQL операції SELECT ===");
            selectAllUsers(connection);
            selectUserColumns(connection);
            selectUsersWhere(connection);
            selectUsersOrderBy(connection);
            selectUsersLimit(connection);
            selectUsersWithOrdersJoin(connection);
            selectGroupBy(connection);
            selectAggregateFunctions(connection);
            selectBetween(connection);
            selectSubquery(connection);

            System.out.println("\n=== TCL операції ===");
            transactionCommit(connection);
            transactionRollback(connection);
            transactionSavepoint(connection);

        } catch (SQLException e) {
            System.out.println("Помилка роботи з БД: " + e.getMessage());
        }
    }

    private static void createTables(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        statement.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    age INTEGER NOT NULL,
                    email TEXT NOT NULL
                )
                """);

        statement.execute("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    product TEXT NOT NULL,
                    price REAL NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                )
                """);
    }

    private static void insertTestData(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        statement.execute("DELETE FROM orders");
        statement.execute("DELETE FROM users");

        statement.execute("""
                INSERT INTO users (name, age, email) VALUES
                ('John', 30, 'john@gmail.com'),
                ('Anna', 22, 'anna@gmail.com'),
                ('Mark', 35, 'mark@gmail.com'),
                ('Olena', 27, 'olena@gmail.com'),
                ('Petro', 40, 'petro@gmail.com')
                """);

        statement.execute("""
                INSERT INTO orders (user_id, product, price) VALUES
                (1, 'Laptop', 25000),
                (1, 'Mouse', 500),
                (2, 'Keyboard', 1200),
                (3, 'Monitor', 7000),
                (4, 'Headphones', 2000),
                (5, 'Printer', 5500)
                """);
    }

    // вибірка записів з таблиці
    private static void selectAllUsers(Connection connection) throws SQLException {
        System.out.println("\n1. SELECT * FROM users");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users");

        while (resultSet.next()) {
            System.out.println(
                    resultSet.getInt("id") + " | " +
                            resultSet.getString("name") + " | " +
                            resultSet.getInt("age") + " | " +
                            resultSet.getString("email")
            );
        }
    }

    // вибірка певних стовпців
    private static void selectUserColumns(Connection connection) throws SQLException {
        System.out.println("\n2. SELECT name, email FROM users");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT name, email FROM users");

        while (resultSet.next()) {
            System.out.println(resultSet.getString("name") + " | " + resultSet.getString("email"));
        }
    }

    // вибірка з умовою WHERE
    private static void selectUsersWhere(Connection connection) throws SQLException {
        System.out.println("\n3. SELECT * FROM users WHERE age > 25");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users WHERE age > 25");

        while (resultSet.next()) {
            System.out.println(resultSet.getString("name") + " | " + resultSet.getInt("age"));
        }
    }

    // сортування ORDER BY
    private static void selectUsersOrderBy(Connection connection) throws SQLException {
        System.out.println("\n4. SELECT * FROM users ORDER BY age");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users ORDER BY age");

        while (resultSet.next()) {
            System.out.println(resultSet.getString("name") + " | " + resultSet.getInt("age"));
        }
    }

    // обмеження кількості записів LIMIT
    private static void selectUsersLimit(Connection connection) throws SQLException {
        System.out.println("\n5. SELECT * FROM users LIMIT 3");
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM users LIMIT 3");

        while (resultSet.next()) {
            System.out.println(resultSet.getString("name") + " | " + resultSet.getString("email"));
        }
    }

    // об'єднання таблиць JOIN
    private static void selectUsersWithOrdersJoin(Connection connection) throws SQLException {
        System.out.println("\n6. JOIN users та orders");

        String sql = """
                SELECT users.name, orders.product, orders.price
                FROM users
                JOIN orders ON users.id = orders.user_id
                """;

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            System.out.println(
                    resultSet.getString("name") + " | " +
                            resultSet.getString("product") + " | " +
                            resultSet.getDouble("price")
            );
        }
    }

    // групування GROUP BY
    private static void selectGroupBy(Connection connection) throws SQLException {
        System.out.println("\n7. GROUP BY user_id");

        String sql = """
                SELECT user_id, COUNT(*) AS order_count
                FROM orders
                GROUP BY user_id
                """;

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            System.out.println(
                    "User ID: " + resultSet.getInt("user_id") +
                            " | Кількість замовлень: " + resultSet.getInt("order_count")
            );
        }
    }

    // агрегатні функції
    private static void selectAggregateFunctions(Connection connection) throws SQLException {
        System.out.println("\n8. AVG, SUM, MAX, MIN, COUNT");

        String sql = """
                SELECT
                    AVG(price) AS avg_price,
                    SUM(price) AS total_sum,
                    MAX(price) AS max_price,
                    MIN(price) AS min_price,
                    COUNT(*) AS order_count
                FROM orders
                """;

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        if (resultSet.next()) {
            System.out.println("Середня ціна: " + resultSet.getDouble("avg_price"));
            System.out.println("Загальна сума: " + resultSet.getDouble("total_sum"));
            System.out.println("Максимальна ціна: " + resultSet.getDouble("max_price"));
            System.out.println("Мінімальна ціна: " + resultSet.getDouble("min_price"));
            System.out.println("Кількість замовлень: " + resultSet.getInt("order_count"));
        }
    }

    // BETWEEN
    private static void selectBetween(Connection connection) throws SQLException {
        System.out.println("\n9. SELECT * FROM orders WHERE price BETWEEN 1000 AND 7000");

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(
                "SELECT * FROM orders WHERE price BETWEEN 1000 AND 7000"
        );

        while (resultSet.next()) {
            System.out.println(
                    resultSet.getString("product") + " | " +
                            resultSet.getDouble("price")
            );
        }
    }

    // підзапит
    private static void selectSubquery(Connection connection) throws SQLException {
        System.out.println("\n10. Subquery");

        String sql = """
                SELECT name, email
                FROM users
                WHERE id IN (
                    SELECT user_id
                    FROM orders
                    WHERE price > 5000
                )
                """;

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);

        while (resultSet.next()) {
            System.out.println(resultSet.getString("name") + " | " + resultSet.getString("email"));
        }
    }

    // TCL: COMMIT
    private static void transactionCommit(Connection connection) throws SQLException {
        System.out.println("\nTCL 1. COMMIT");

        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        statement.execute("INSERT INTO users (name, age, email) VALUES ('CommitUser', 28, 'commit@gmail.com')");
        connection.commit();
        connection.setAutoCommit(true);

        System.out.println("Дані успішно додані та збережені за допомогою COMMIT.");
    }

    // TCL: ROLLBACK
    private static void transactionRollback(Connection connection) throws SQLException {
        System.out.println("\nTCL 2. ROLLBACK");

        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        statement.execute("INSERT INTO users (name, age, email) VALUES ('RollbackUser', 33, 'rollback@gmail.com')");
        connection.rollback();
        connection.setAutoCommit(true);

        System.out.println("Операція скасована за допомогою ROLLBACK. Користувач не був збережений.");
    }

    // TCL: SAVEPOINT
    private static void transactionSavepoint(Connection connection) throws SQLException {
        System.out.println("\nTCL 3. SAVEPOINT");

        connection.setAutoCommit(false);
        Statement statement = connection.createStatement();
        statement.execute("INSERT INTO users (name, age, email) VALUES ('SavepointUser1', 21, 'save1@gmail.com')");
        Savepoint savepoint = connection.setSavepoint("first_savepoint");
        statement.execute("INSERT INTO users (name, age, email) VALUES ('SavepointUser2', 25, 'save2@gmail.com')");
        connection.rollback(savepoint);
        connection.commit();
        connection.setAutoCommit(true);

        System.out.println("Перший користувач збережений, другий скасований через SAVEPOINT.");
    }
}