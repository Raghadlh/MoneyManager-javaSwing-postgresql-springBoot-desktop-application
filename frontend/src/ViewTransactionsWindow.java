import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Vector;

public class ViewTransactionsWindow {
    private JFrame frame;
    private final Color PRIMARY_DARK = new Color(18, 12, 48);
    private final Color PRIMARY_LIGHT = new Color(33, 147, 176);
    private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 32);
    private final Font TABLE_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);

    /**
     * A window to view, search, edit, delete, and export transactions.
     */
    public ViewTransactionsWindow(String username) {
        frame = new JFrame("Transactions");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Gradient Background
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), getHeight(), PRIMARY_LIGHT);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setLayout(new BorderLayout(30, 30));
        gradientPanel.setBorder(new EmptyBorder(40, 60, 40, 60));

        // Title
        JLabel title = new JLabel("Transaction History", SwingConstants.CENTER);
        title.setFont(TITLE_FONT);
        title.setForeground(Color.WHITE);
        gradientPanel.add(title, BorderLayout.NORTH);

        // Table Panel
        JPanel tableCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.fillRoundRect(5, 5, getWidth()-8, getHeight()-8, 20, 20);

                // Card
                g2d.setColor(new Color(33, 147, 176, 97));
                g2d.fillRoundRect(0, 0, getWidth()-5, getHeight()-5, 20, 20);
            }
        };
        tableCard.setLayout(new BorderLayout(15, 15));
        tableCard.setOpaque(false);
        tableCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTable table = new JTable() {
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    comp.setBackground(row % 2 == 0 ? new Color(255, 255, 255, 80) : new Color(255, 255, 255, 30));
                } else {
                    comp.setBackground(new Color(22, 98, 117, 180));
                }
                if (comp instanceof JComponent) {
                    ((JComponent) comp).setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
                }
                comp.setForeground(Color.WHITE);
                return comp;
            }
        };

        table.setFont(TABLE_FONT);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(22, 98, 117, 180));
        table.setSelectionForeground(Color.WHITE);
        table.setOpaque(false);
        table.setAutoCreateRowSorter(true);

        // Table Header Styling
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(),  40));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBackground(new Color(211, 244, 247));
                label.setForeground(new Color(22, 98, 117));
                label.setFont(HEADER_FONT);
                label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

                JTableHeader header = table.getTableHeader();
                if (table.getRowSorter() != null) {
                    java.util.List<? extends RowSorter.SortKey> sortKeys = table.getRowSorter().getSortKeys();
                    if (!sortKeys.isEmpty() && sortKeys.get(0).getColumn() == column) {
                        SortOrder order = sortKeys.get(0).getSortOrder();
                        if (order == SortOrder.ASCENDING) {
                            label.setIcon(UIManager.getIcon("Table.ascendingSortIcon"));
                        } else if (order == SortOrder.DESCENDING) {
                            label.setIcon(UIManager.getIcon("Table.descendingSortIcon"));
                        } else {
                            label.setIcon(null);
                        }
                    } else {
                        label.setIcon(null);
                    }
                }

                return label;
            }
        });

        // Search Field Panel
        JTextField searchField = new JTextField("Search...");
        searchField.setForeground(Color.GRAY);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (searchField.getText().equals("Search...")) {
                    searchField.setText("");
                    searchField.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Search...");
                    searchField.setForeground(Color.GRAY);
                }
            }
        });

        // Table filter logic
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = searchField.getText();
                if (text.equals("Search...")) text = "";
                TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
                table.setRowSorter(sorter);
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });

        // Scroll Pane
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        table.setOpaque(false);

        // Styled Search Field Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JButton exportButton = createActionButton("Export","");

        searchField.setMaximumSize(new Dimension(300, 35));
        searchField.setBackground(new Color(255, 255, 255, 180));
        searchField.setCaretColor(Color.DARK_GRAY);
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        searchField.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintSafely(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(searchField.getBackground());
                g2.fillRoundRect(0, 0, searchField.getWidth(), searchField.getHeight(), 15, 15);
                g2.dispose();
                super.paintSafely(g);
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(exportButton);

        tableCard.add(searchPanel, BorderLayout.NORTH);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton addButton = createActionButton("Add", "✚");
        JButton editButton = createActionButton("Edit", "✎");
        JButton deleteButton = createActionButton("Delete", "✖");
        JButton backButton = createActionButton("Back", "←");


        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        tableCard.add(buttonPanel, BorderLayout.SOUTH);
        gradientPanel.add(tableCard, BorderLayout.CENTER);

        // Add action listeners
        addButton.addActionListener(e -> {
            new AddTransactionForm(username);
            frame.dispose();
        });

        exportButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save as CSV");
            int userSelection = chooser.showSaveDialog(frame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                try (PrintWriter pw = new PrintWriter(chooser.getSelectedFile().getAbsolutePath() + ".csv")) {
                    TableModel model = table.getModel();
                    // Write header
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        pw.print(model.getColumnName(i));
                        if (i != model.getColumnCount() - 1) pw.print(",");
                    }
                    pw.println();
                    // Write rows
                    for (int row = 0; row < model.getRowCount(); row++) {
                        for (int col = 0; col < model.getColumnCount(); col++) {
                            Object value = model.getValueAt(row, col);
                            pw.print(value != null ? value.toString() : "");
                            if (col != model.getColumnCount() - 1) pw.print(",");
                        }
                        pw.println();
                    }

                    ToastMessage toast = new ToastMessage("Exported successfully to CSV!", 3000);
                    int x = frame.getX() + (frame.getWidth() - toast.getWidth()) / 2;
                    int y = frame.getY() + frame.getHeight() - toast.getHeight() - 50;
                    toast.showToast(x, y);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "⚠ Error exporting file: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });


        backButton.addActionListener(e -> {
            new DashboardWindow(username);
            frame.dispose();
        });

        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                String id = table.getModel().getValueAt(modelRow, 0).toString();

                int confirm = JOptionPane.showConfirmDialog(frame, "Are you sure you want to delete this transaction?");
                if (confirm == JOptionPane.YES_OPTION) {
                    deleteTransaction(id, () -> fetchTransactions(table));
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a row to delete.");
            }

        });

        editButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int modelRow = table.convertRowIndexToModel(selectedRow);
                Long id = Long.parseLong(table.getModel().getValueAt(modelRow, 0).toString());
                String type = table.getModel().getValueAt(modelRow, 1).toString();
                String category = table.getModel().getValueAt(modelRow, 2).toString();
                String amount = table.getModel().getValueAt(modelRow, 3).toString();
                String date = table.getModel().getValueAt(modelRow, 4).toString();
                String note = table.getModel().getValueAt(modelRow, 5).toString();
                boolean isSaving = table.getModel().getValueAt(modelRow, 6).toString().equals("Yes");

                new EditTransactionForm(username, id, type, category, amount, date, note, isSaving);

                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Please select a row to edit.");
            }
        });
        // Load data
        fetchTransactions(table);

        frame.add(gradientPanel);
        frame.setVisible(true);
    }

    private JButton createActionButton(String text, String icon) {
        JButton button = new JButton(icon + " " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(PRIMARY_LIGHT.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(PRIMARY_LIGHT);
                } else {
                    g2d.setColor(PRIMARY_LIGHT);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    private void fetchTransactions(JTable table) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/transactions"))
                .header("Cookie", AuthClient.sessionCookie)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200) {
                        SwingUtilities.invokeLater(() -> populateTable(table, response.body()));
                    } else {
                        SwingUtilities.invokeLater(() ->
                                JOptionPane.showMessageDialog(frame,
                                        "Server error (" + response.statusCode() + "): " + response.body(),
                                        "Loading Failed", JOptionPane.ERROR_MESSAGE)
                        );
                    }
                }).exceptionally(ex -> {
                    SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame,
                                    "Network issue: " + ex.getMessage(),
                                    "Connection Error", JOptionPane.ERROR_MESSAGE)
                    );
                    return null;
                });
    }

        private void populateTable(JTable table, String jsonData) {
        try {

            JSONArray array = new JSONArray(jsonData);
            Vector<String> columns = new Vector<>(java.util.List.of("ID", "Type", "Category", "Amount", "Date", "Note","Saving"));
            Vector<Vector<Object>> data = new Vector<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Vector<Object> row = new Vector<>();
                System.out.println("Parsing item: " + obj.toString());

                row.add(obj.getLong("id"));
                row.add(obj.getString("type"));
                row.add(obj.getString("category"));
                row.add(obj.getDouble("amount"));
                row.add(obj.getString("date"));
                row.add(obj.optString("note", ""));
                row.add(obj.getBoolean("saving") ? "Yes" : "No");

                data.add(row);
            }

            SwingUtilities.invokeLater(() -> {
                DefaultTableModel model = new DefaultTableModel(data, columns);
                table.setModel(model);
                table.removeColumn(table.getColumnModel().getColumn(0));
            });

            System.out.println("Table loaded successfully");



        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "⚠ Error loading transactions:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteTransaction(String id, Runnable onSuccess) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/transactions/" + id))
                .header("Cookie", AuthClient.sessionCookie)
                .DELETE()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() == 200 || response.statusCode() == 204) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(null, "Transaction deleted successfully.");
                            onSuccess.run();
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Failed to delete transaction: " + response.body()));
                    }
                });
    }

}
