public boolean FinancialLog() {
    Scanner sc = new Scanner(System.in);

    if (!auth.sessionChecker(acc.getCardNum())) {
        return false;
    }

    System.out.println(Account.Color.VIOLET + Account.Color.BOLD + "\n=== FINANCIAL LOG ===" + Account.Color.RESET);
    System.out.println("1. View All Logs");
    System.out.println("2. Filter by Category");
    System.out.println("3. Filter by Time");
    System.out.println("4. Add Log");
    System.out.print("Choice: ");

    int choice = sc.nextInt();
    sc.nextLine();

    try {
        String query = "";
        PreparedStatement ps;

        switch (choice) {

            // view the logs
            case 1:
                query = "SELECT * FROM logs WHERE card_id = ?";
                ps = conn.prepareStatement(query);
                ps.setInt(1, acc.getCardId());
                break;

            // category filter
            case 2:
                System.out.println("1. Essentials");
                System.out.println("2. Treats");
                System.out.print("Choose: ");
                int catChoice = sc.nextInt();
                sc.nextLine();

                String category = (catChoice == 1) ? "ESSENTIALS" : "TREATS";

                query = "SELECT * FROM logs WHERE card_id = ? AND category = ?";
                ps = conn.prepareStatement(query);
                ps.setInt(1, acc.getCardId());
                ps.setString(2, category);
                break;

            // time filter
            case 3:
                System.out.println("1. Today");
                System.out.println("2. Last 7 Days");
                System.out.println("3. Last Month");
                System.out.print("Choose: ");
                int timeChoice = sc.nextInt();

                String condition = "";
                switch (timeChoice) {
                    case 1:
                        condition = "DATE(timestamp) = CURRENTDATE()";
                        break;
                    case 2:
                        condition = "timestamp >= NOW() - INTERVAL 7 DAY";
                        break;
                    case 3:
                        condition = "timestamp >= NOW() - INTERVAL 1 MONTH";
                        break;
                    default:
                        System.out.println("Error. Please choose a proper option.");
                        return true;
                }

                query = "SELECT * FROM logs WHERE card_id = ? AND " + condition;
                ps = conn.prepareStatement(query);
                ps.setInt(1, acc.getCardId());
                break;

            // log adding
            case 4:
                System.out.println("1. Essentials");
                System.out.println("2. Treats");
                System.out.print("Choose category: ");
                int addCat = sc.nextInt();
                sc.nextLine();

                String addCategory = (addCat == 1) ? "ESSENTIALS" : "TREATS";

                System.out.print("Enter amount: ");
                double amount = sc.nextDouble();
                sc.nextLine();

                String insertQuery = "INSERT INTO logs (card_id, type, amount, category) VALUES (?, ?, ?, ?)";
                PreparedStatement insertPS = conn.prepareStatement(insertQuery);

                insertPS.setInt(1, acc.getCardId());
                insertPS.setString(2, "EXPENSE");
                insertPS.setDouble(3, amount);
                insertPS.setString(4, addCategory);

                insertPS.executeUpdate();

                System.out.println(Account.Color.GREEN + "Log added successfully!" + Account.Color.RESET);
                return true;

            default:
                System.out.println("Error.");
                return true;
        }

        // execute query then display the log
        ResultSet rs = ps.executeQuery();
        boolean hasData = false;

        while (rs.next()) {
            hasData = true;

            System.out.println("----------------------------");
            System.out.println("Type: " + rs.getString("type"));
            System.out.println("Amount: " + rs.getDouble("amount"));
            System.out.println("Category: " + rs.getString("category"));
            System.out.println("Date: " + rs.getTimestamp("timestamp"));
        }

        if (!hasData) {
            System.out.println("There is no data found.");
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
            "<html><font color='red'>Error in Financial Log.</font></html>");
    }

    return true;
}
