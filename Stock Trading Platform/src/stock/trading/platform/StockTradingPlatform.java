package stock.trading.platform;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

// Represents a stock available in the market
class Stock {
    String symbol;
    double price;

    Stock(String symbol, double price) {
        this.symbol = symbol.toUpperCase();
        this.price = price;
    }
}

// Represents a buy/sell transaction
class Transaction {
    String symbol;
    int quantity;
    double price;
    String type;

    Transaction(String symbol, int quantity, double price, String type) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }
}

// Represents the user and their portfolio
class User {
    double balance;
    ArrayList<Transaction> transactions;

    User(double balance) {
        this.balance = balance;
        transactions = new ArrayList<>();
    }

    // buy stock if balance is enough
    boolean buy(Stock s, int qty) {
        double total = s.price * qty;
        if (total <= balance) {
            balance -= total;
            transactions.add(new Transaction(s.symbol, qty, s.price, "BUY"));
            return true;
        }
        return false;
    }

    // sell stock if user owns enough
    boolean sell(Stock s, int qty) {
        int owned = 0;

        for (Transaction t : transactions) {
            if (t.symbol.equals(s.symbol)) {
                if (t.type.equals("BUY")) owned += t.quantity;
                else owned -= t.quantity;
            }
        }

        if (qty <= owned) {
            balance += s.price * qty;
            transactions.add(new Transaction(s.symbol, qty, s.price, "SELL"));
            return true;
        }
        return false;
    }

    // create summary for table
    ArrayList<Object[]> getSummary() {
        ArrayList<Object[]> data = new ArrayList<>();
        ArrayList<String> symbols = new ArrayList<>();

        for (Transaction t : transactions) {
            if (!symbols.contains(t.symbol)) symbols.add(t.symbol);
        }

        for (String sym : symbols) {
            int qty = 0;
            double invested = 0;

            for (Transaction t : transactions) {
                if (t.symbol.equals(sym)) {
                    if (t.type.equals("BUY")) {
                        qty += t.quantity;
                        invested += t.quantity * t.price;
                    } else {
                        qty -= t.quantity;
                        invested -= t.quantity * t.price;
                    }
                }
            }

            data.add(new Object[]{sym, qty, invested});
        }

        return data;
    }
}

public class StockTradingPlatform extends JFrame {

    private JTable marketTable, portfolioTable;
    private DefaultTableModel marketModel, portfolioModel;

    private JTextField stockField, quantityField;
    private JLabel balanceLabel;

    private ArrayList<Stock> marketStocks;
    private User user;

    public StockTradingPlatform() {

        setTitle("Stock Trading Platform");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // create user with starting balance
        user = new User(10000);

        // sample market stocks
        marketStocks = new ArrayList<>();
        marketStocks.add(new Stock("AAPL", 150));
        marketStocks.add(new Stock("GOOG", 2800));
        marketStocks.add(new Stock("TSLA", 700));
        marketStocks.add(new Stock("AMZN", 3500));

        // ---------- TOP: MARKET TABLE ----------
        String[] marketCols = {"Stock Symbol", "Price ($)"};
        marketModel = new DefaultTableModel(marketCols, 0);
        marketTable = new JTable(marketModel);
        marketTable.setEnabled(false); // read-only

        for (Stock s : marketStocks) {
            marketModel.addRow(new Object[]{s.symbol, s.price});
        }

        add(new JScrollPane(marketTable), BorderLayout.NORTH);

        // ---------- CENTER: PORTFOLIO ----------
        String[] portCols = {"Stock", "Quantity", "Invested ($)"};
        portfolioModel = new DefaultTableModel(portCols, 0);
        portfolioTable = new JTable(portfolioModel);

        add(new JScrollPane(portfolioTable), BorderLayout.CENTER);

        // ---------- BOTTOM: INPUT + BUTTONS ----------
        JPanel bottomPanel = new JPanel(new FlowLayout());

        stockField = new JTextField(8);
        stockField.setBorder(BorderFactory.createTitledBorder("Stock"));

        quantityField = new JTextField(5);
        quantityField.setBorder(BorderFactory.createTitledBorder("Quantity"));

        JButton buyBtn = new JButton("Buy");
        JButton sellBtn = new JButton("Sell");
        JButton refreshBtn = new JButton("Refresh");

        balanceLabel = new JLabel("Balance: $" + user.balance);

        bottomPanel.add(stockField);
        bottomPanel.add(quantityField);
        bottomPanel.add(buyBtn);
        bottomPanel.add(sellBtn);
        bottomPanel.add(refreshBtn);
        bottomPanel.add(balanceLabel);

        add(bottomPanel, BorderLayout.SOUTH);

        // button actions
        buyBtn.addActionListener(e -> buyStock());
        sellBtn.addActionListener(e -> sellStock());
        refreshBtn.addActionListener(e -> updatePortfolio());
    }

    // find stock from market list
    private Stock findStock(String symbol) {
        for (Stock s : marketStocks) {
            if (s.symbol.equalsIgnoreCase(symbol)) return s;
        }
        return null;
    }

    private void buyStock() {
        String sym = stockField.getText();
        String qtyText = quantityField.getText();

        Stock s = findStock(sym);
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Stock not found");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);

            if (user.buy(s, qty)) {
                JOptionPane.showMessageDialog(this, "Bought successfully");
                balanceLabel.setText("Balance: $" + user.balance);
                updatePortfolio();
            } else {
                JOptionPane.showMessageDialog(this, "Not enough balance");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
        }
    }

    private void sellStock() {
        String sym = stockField.getText();
        String qtyText = quantityField.getText();

        Stock s = findStock(sym);
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Stock not found");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyText);

            if (user.sell(s, qty)) {
                JOptionPane.showMessageDialog(this, "Sold successfully");
                balanceLabel.setText("Balance: $" + user.balance);
                updatePortfolio();
            } else {
                JOptionPane.showMessageDialog(this, "Not enough stock");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid quantity");
        }
    }

    // refresh portfolio table
    private void updatePortfolio() {
        portfolioModel.setRowCount(0);

        for (Object[] row : user.getSummary()) {
            portfolioModel.addRow(row);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new StockTradingPlatform().setVisible(true);
        });
    }
}