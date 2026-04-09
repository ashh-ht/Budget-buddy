public boolean depositCash() {
    Scanner sc = new Scanner(System.in);

    if (auth.sessionChecker(acc.getCardNum()) == false) {
         JOptionPane.showOptionDialog(null,
         "<html>" + "Your session has expired. Please login again."
         + "<font color='red'><br>LOGGING OUT...</font></html>" + "\nClick OK to continue",
          "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        return false;
    }

    try {
        System.out.println(Account.Color.VIOLET + Account.Color.BOLD +  "\n=== DEPOSIT CASH ===" + Account.Color.RESET);

        // for getting current deposit balance from database
        String getBalanceQuery = "SELECT balance FROM accounts WHERE cardId = ?";
        PreparedStatement ps1 = conn.prepareStatement(getBalanceQuery);
        ps1.setInt(1, acc.getCardId());

        ResultSet rs = ps1.executeQuery();

        double balance = 0;
        if (rs.next()) {
            balance = rs.getDouble("balance");
        }

        System.out.println("Current Balance: " + balance);

        // deposit input
        System.out.print("Enter amount to deposit: ");
        double deposit = sc.nextDouble();

        if (deposit <= 0) {
            JOptionPane.showMessageDialog(null,
                "<html><font color='red'>Invalid amount.</font></html>");
            return true;
        }

        double newBalance = balance + deposit;

        // for updating balance in database
        String updateQuery = "UPDATE accounts SET balance = ? WHERE cardId = ?";
        PreparedStatement ps2 = conn.prepareStatement(updateQuery);
        ps2.setDouble(1, newBalance);
        ps2.setInt(2, acc.getCardId());
        ps2.executeUpdate();

        System.out.println(Account.Color.GREEN + "Deposit successful!" + Account.Color.RESET);
        System.out.println("Updated Balance: " + newBalance);

        
        insertLog("DEPOSIT", deposit, "Cash Deposit");

        // low balance message
        if (newBalance < 100) {
            JOptionPane.showMessageDialog(null,
                "<html><font color='red'>Your balance still below 100. Please deposit more!</font></html>");
        }

    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
            "<html><font color='red'>Error processing deposit.</font></html>");
    }

    return true;
}
