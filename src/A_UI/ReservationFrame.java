package A_UI;

import E_ClientConn.ClientConnection;
import D_Protocol.Protocol;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ReservationFrame extends JFrame {

    private final ClientConnection conn;
    private final String currentUser;

    // UI
    private JComboBox<String> cbEvent;
    private JComboBox<String> cbDate;
    private JComboBox<String> cbMatch;
    private JLabel lblLeft;
    private JSpinner spQty;
    private JButton btnBook;
    private JLabel msg;
    private boolean isLoading=false;

    // cache: {id,time,desc,left}
    private final List<String[]> currentRows = new ArrayList<>();

    // hard lock to avoid re-entrant listeners during model updates
    private int suspendEvents = 0;

    public ReservationFrame(ClientConnection c, String user) {
        super("Reservation");
        this.conn = c;
        this.currentUser = user;

        buildUi();
        wireEvents();

        // initial load
        SwingUtilities.invokeLater(this::loadDatesForSelectedEvent);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(740, 360);
        setLocationRelativeTo(null);
    }

    // =============== UI ===============
    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JPanel row1 = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill = GridBagConstraints.HORIZONTAL;

        cbEvent = new JComboBox<>(new String[]{"football", "formula", "boxing"});
        addLabeled(row1, g, 0, "Event", cbEvent, 1.0);

        JPanel row2 = new JPanel(new GridBagLayout());
        GridBagConstraints g2 = new GridBagConstraints();
        g2.insets = new Insets(6, 6, 6, 6);
        g2.fill = GridBagConstraints.HORIZONTAL;

        cbDate = new JComboBox<>();
        cbDate.setEnabled(false);
        addLabeled(row2, g2, 0, "Date", cbDate, 1.0);

        JPanel row3 = new JPanel(new GridBagLayout());
        GridBagConstraints g3 = new GridBagConstraints();
        g3.insets = new Insets(6, 6, 6, 6);
        g3.fill = GridBagConstraints.HORIZONTAL;

        cbMatch = new JComboBox<>();
        cbMatch.setEnabled(false);
        addLabeled(row3, g3, 0, "Match", cbMatch, 1.0);

        JPanel row4 = new JPanel(new GridBagLayout());
        GridBagConstraints g4 = new GridBagConstraints();
        g4.insets = new Insets(6, 6, 6, 6);
        g4.anchor = GridBagConstraints.WEST;

        lblLeft = new JLabel("Left: -");
        spQty = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        btnBook = new JButton("Book");
        btnBook.setEnabled(false);

        g4.gridx = 0; g4.gridy = 0; row4.add(lblLeft, g4);
        g4.gridx = 1; g4.gridy = 0; g4.insets = new Insets(6, 20, 6, 6); row4.add(new JLabel("Tickets"), g4);
        g4.gridx = 2; g4.gridy = 0; g4.insets = new Insets(6, 6, 6, 6); row4.add(spQty, g4);
        g4.gridx = 3; g4.gridy = 0; g4.insets = new Insets(6, 20, 6, 6); row4.add(btnBook, g4);

        msg = new JLabel(" ");
        msg.setBorder(new EmptyBorder(8, 4, 4, 4));

        root.add(row1, BorderLayout.NORTH);
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(row2);
        center.add(row3);
        center.add(row4);
        root.add(center, BorderLayout.CENTER);
        root.add(msg, BorderLayout.SOUTH);
    }

    private void addLabeled(JPanel panel, GridBagConstraints g, int col, String label, JComponent field, double weight) {
        GridBagConstraints l = (GridBagConstraints) g.clone();
        l.gridx = col; l.gridy = 0; l.weightx = 0;
        panel.add(new JLabel(label), l);

        GridBagConstraints f = (GridBagConstraints) g.clone();
        f.gridx = col + 1; f.gridy = 0; f.weightx = weight; f.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, f);
    }

    private void wireEvents() {
        cbEvent.addActionListener(e -> {
            if (eventsAllowed()) loadDatesForSelectedEvent();
        });
        cbDate.addActionListener(e -> {
            if (eventsAllowed() && cbDate.isEnabled()) loadMatchesForSelectedDate();
        });
        cbMatch.addActionListener(e -> {
            if (eventsAllowed()) updateLeftAndSpinner();
        });
        btnBook.addActionListener(e -> onBook());
    }

    // ===== helpers for safe model updates =====
    private void withNoEvents(Runnable r) {
        suspendEvents++;
        try { r.run(); } finally { suspendEvents--; }
    }
    private boolean eventsAllowed() { return suspendEvents == 0; }

    private void setCombo(JComboBox<String> cb, DefaultComboBoxModel<String> model) {
        withNoEvents(() -> cb.setModel(model));
    }

    private String selected(JComboBox<String> cb) {
        Object o = cb.getSelectedItem();
        return o == null ? "" : o.toString().trim();
    }

    private void showMsg(String text, boolean error) {
        msg.setText(text == null ? " " : text);
        msg.setForeground(error ? new Color(180, 0, 0) : new Color(0, 102, 0));
    }

    private int parseLeft(String s) {
        String digits = s.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    // =============== Flow ===============

    // 1) Event -> Dates
    private void loadDatesForSelectedEvent() {
    if (isLoading) return;
    isLoading = true;
    try {
        String event = selected(cbEvent);

        setCombo(cbDate, new DefaultComboBoxModel<>());
        cbDate.setEnabled(false);
        setCombo(cbMatch, new DefaultComboBoxModel<>());
        cbMatch.setEnabled(false);
        btnBook.setEnabled(false);
        lblLeft.setText("Left: -");
        showMsg("Loading dates…", false);

        // السيرفر يرجّع تواريخ لهذا الحدث فقط
        String raw = conn.sendMultiLine("DATES," + event);

        DefaultComboBoxModel<String> d = new DefaultComboBoxModel<>();
        if (raw != null && !raw.isBlank()) {
            for (String ln : raw.split("\\r?\\n")) {
                String dt = ln.trim();
                if (!dt.isEmpty()) d.addElement(dt);
            }
        }
        setCombo(cbDate, d);
        cbDate.setEnabled(d.getSize() > 0);

        showMsg(cbDate.isEnabled() ? "Select a date." : "No dates available for this event.",
                !cbDate.isEnabled());
    } catch (Exception ex) {
        showMsg("Error loading dates: " + ex.getMessage(), true);
    } finally {
        isLoading = false;
    }
}


    // 2) Date -> Matches (id|time|desc|available)
   private void loadMatchesForSelectedDate() {
    withNoEvents(() -> {
        try {
            String event = selected(cbEvent);
            String date  = selected(cbDate);  // << مو dates

            setCombo(cbMatch, new DefaultComboBoxModel<>());
            cbMatch.setEnabled(false);
            btnBook.setEnabled(false);
            lblLeft.setText("Left: -");
            showMsg("Loading matches…", false);

            String raw = conn.sendMultiLine(Protocol.makeList(event, date)); // << date

            currentRows.clear();
            DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
            if (raw != null && !raw.isBlank()) {
                for (String ln : raw.split("\\r?\\n")) {
                    String[] p = ln.split("\\|");
                    if (p.length >= 4) {
                        String id   = p[0].trim();
                        String time = p[1].trim();
                        String desc = p[2].trim();
                        int avail   = parseLeft(p[3]);
                        if (avail > 0) {
                            currentRows.add(new String[]{id, time, desc, String.valueOf(avail)});
                            m.addElement(desc + " — " + time);
                        }
                    }
                }
            }
            setCombo(cbMatch, m);
            cbMatch.setEnabled(m.getSize() > 0);
            showMsg(cbMatch.isEnabled() ? "Select a match." : "No matches available on this date.",
                    !cbMatch.isEnabled());
        } catch (Exception ex) {
            showMsg("Error loading matches: " + ex.getMessage(), true);
        }
    });
}


    // 3) Match -> Left + spinner bounds
    private void updateLeftAndSpinner() {
        int idx = cbMatch.getSelectedIndex();
        if (idx < 0 || idx >= currentRows.size()) {
            btnBook.setEnabled(false);
            lblLeft.setText("Left: -");
            showMsg("Select a match.", false);
            return;
        }
        int left = Integer.parseInt(currentRows.get(idx)[3]);
        lblLeft.setText("Left: " + left);
        int cur = ((Number)((SpinnerNumberModel) spQty.getModel()).getNumber()).intValue();
        if (cur > left) cur = left;
        if (cur < 1) cur = 1;

        spQty.setModel(new SpinnerNumberModel(cur, 1, Math.max(1, left), 1));

        

        btnBook.setEnabled(left > 0);
        showMsg(left > 0 ? "Select tickets and press Book." : "Sold out.", left == 0);
    }

    // 4) Book
    /*private void onBook() {
        int idx = cbMatch.getSelectedIndex();
        if (idx < 0 || idx >= currentRows.size()) {
            showMsg("Select a match first.", true);
            return;
        }

        int left = Integer.parseInt(currentRows.get(idx)[3]);
        int qty;
        try { spQty.commitEdit(); qty = (Integer) spQty.getValue(); }
        catch (Exception ignored) { qty = 1; }
        if (qty < 1) qty = 1;

        if (qty > left) {
            showMsg("Requested " + qty + " but only " + left + " left.", true);
            return;
        }

        String matchId = currentRows.get(idx)[0];
        try {
            String res = conn.sendOneLine(Protocol.makeReserve(matchId, qty));
            if (res != null && res.toUpperCase().startsWith(Protocol.CONFIRMED)) {
                showMsg("Reservation confirmed ✓", false);
                loadMatchesForSelectedDate(); // refresh availability
            } else if (res != null && res.toUpperCase().contains(Protocol.SOLD_OUT)) {
                showMsg("Not enough tickets available. Refreshing…", true);
                loadMatchesForSelectedDate();
            } else {
                showMsg("Reservation failed: " + res, true);
            }
        } catch (Exception ex) {
            showMsg("Error during reservation: " + ex.getMessage(), true);
        }
    }*/

private void onBook() {
    int idx = cbMatch.getSelectedIndex();
    if (idx < 0 || idx >= currentRows.size()) {
        showMsg("Select a match first.", true);
        return;
    }

    int left = Integer.parseInt(currentRows.get(idx)[3]);
    int qty;
    try { spQty.commitEdit(); qty = (Integer) spQty.getValue(); }
    catch (Exception ignored) { qty = 1; }
    if (qty < 1) qty = 1;

    if (qty > left) {
        showMsg("Requested " + qty + " but only " + left + " left.", true);
        return;
    }

    String matchId = currentRows.get(idx)[0];
    try {
        String res = conn.sendOneLine(Protocol.makeReserve(matchId, qty));
        if (res != null && res.toUpperCase().startsWith(Protocol.CONFIRMED)) {
            
            // ⭐️ التعديل هنا: استبدال showMsg بالشاشة المنبثقة
            JOptionPane.showMessageDialog(this, 
                                        " Reservation confirmed successfully.", 
                                        "Reservation confirmed", 
                                        JOptionPane.INFORMATION_MESSAGE);
            
            // بما أن JOptionPane يحجب التنفيذ، فإن loadMatchesForSelectedDate() سيتم تنفيذه مباشرة بعد إغلاق النافذة.
            loadMatchesForSelectedDate(); // refresh availability
            
        } else if (res != null && res.toUpperCase().contains(Protocol.SOLD_OUT)) {
            showMsg("Not enough tickets available. Refreshing…", true);
            loadMatchesForSelectedDate();
        } else {
            showMsg("Reservation failed: " + res, true);
        }
    } catch (Exception ex) {
        showMsg("Error during reservation: " + ex.getMessage(), true);
    }
}
}
