package com.welabeldata.archipelo.task1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class MainWindow {
    public static String APP_NAME = "WeLabelData label tool";
    public static String SAVED = "SAVED";
    public static String NOT_SAVED = "NOT SAVED";

    private JFrame frame;
    private JPanel mainPannel;
    private JPanel row1;
    private JPanel row2;
    private JPanel row3;
    private JLabel savedOrNotSaved = new JLabel(NOT_SAVED);
    private JTextArea currentTaskField = new JTextArea("X");

    private final List<String> classifications = Arrays.asList("NON-DEV", "BEST_PRACTICE", "BUG_FIX",
            "INTERVIEW_QUESTION", "CODE_EXAMPLE",
            "DOCUMENTATION_LOOKUP", "TECH_COMPARISON",
            "PERFORMANCE_IMPROVEMENT", "DEFINITION",
            "STACK_CHOICE", "STACK_FIT",
            "SOLUTION_DISCOVERY", "BUSINESS_VALUE", "UNKNOWN", "KNOWLEDGE_DISCOVERY","Non-English");
    private JComboBox classificationBox = new JComboBox(classifications.toArray());

    private JButton nextButton = new JButton("Next");
    private JButton prevButton = new JButton("Prev");
    private JButton loadCurrentStateButton = new JButton("Load latest state");
    private JButton saveButton = new JButton("Save task");
    private JTextArea taskRowTextField;
    private JTextField jobTextField = new JTextField("", 10);
    private JTextField userTextField = new JTextField("", 10);
    private JCheckBox isForDev = new JCheckBox("Is for developers?: ", false);
    private JTextField keyWordsJTextField = new JTextField("", 100);

    //STATE
    private DbAdapter adapter;
    private DbAdapter.TaskWithResult currentResult;
    private UUID currentUserId;
    private DbAdapter.Job currentJob;
    private List<DbAdapter.TaskWithResult> allResults = new ArrayList<>();

    public MainWindow() throws SQLException {
        componentsInitialization();
        frame = new JFrame(APP_NAME);
        frame.setContentPane(mainPannel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension DimMax = Toolkit.getDefaultToolkit().getScreenSize();
//        frame.setMaximumSize(DimMax);
//        frame.setExtendedState(JFrame.MAXIMIZED_HORIZ);
//        frame.pack();
        frame.setVisible(true);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);


//        KeyboardFocusManager.getCurrentKeyboardFocusManager()
//                .addKeyEventDispatcher(e -> {
//                    if (userTextField.isFocusOwner() || jobTextField.isFocusOwner()) {
//                        return false;
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.VK_ENTER == e.getKeyCode()) {
//                        onTaskNumberChanged();
//                        return true;
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("A")) {
//                        if (prevButton.isEnabled()) {
//                            onPrevClick();
//                        }
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("D")) {
//                        if (nextButton.isEnabled()) {
//                            onNextClick();
//                        }
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("S")) {
//                        if (saveButton.isEnabled()) {
//                            onSaveClick();
//                        }
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("W")) {
//                        if (nextButton.isEnabled()) {
//                            currentResult.isDev = !currentResult.isDev;
//                            redraw();
//                        }
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("E")) {
//                        String word = Optional.ofNullable(taskRowTextField.getSelectedText()).orElse("")
//                                .replace(",", "");
//                        if (!word.isEmpty()) {
//                            currentResult.keyWord.add(word);
//                        }
//                        redraw();
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("Q")) {
//                        String word = Optional.ofNullable(keyWordsJTextField.getSelectedText()).orElse("")
//                                .replace(",", "");
//                        if (!word.isEmpty()) {
//                            currentResult.keyWord.remove(word);
//                        }
//                        redraw();
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("R")) {
//                        if (currentResult != null) {
//                            return openGoogleSearch(currentResult.getTaskRow());
//                        }
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("T")) {
//                        if (currentResult != null) {
//                            return openGoogleTranslate(currentResult.getTaskRow());
//                        }
//                    }
//                    return false;
//                });

    }

    private void componentsInitialization() throws SQLException {
        mainPannel = new JPanel();

        mainPannel.setLayout(new BorderLayout());
        mainPannel.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));

        JPanel pageStart = new JPanel();
        BoxLayout boxLayout = new BoxLayout(pageStart, BoxLayout.Y_AXIS);
        pageStart.setLayout(boxLayout);

        Font font1 = new Font("SansSerif", Font.BOLD, 20);
        row1 = new JPanel();
        row1.add(new JLabel("Job name: "));
        row1.add(jobTextField);
        row1.add(new JLabel("User: "));
        row1.add(userTextField);
        row1.add(loadCurrentStateButton);
        row1.add(prevButton);
        row1.add(new JLabel("Task: "));
        row1.add(currentTaskField);
        row1.add(nextButton);
        pageStart.add(row1);

        row2 = new JPanel();
//        classificationBox.setBounds(50, 50, 90, 20);
//        classificationBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (currentResult != null) {
//                    currentResult.classification = (String) classificationBox.getSelectedItem();
//                }
//            }
//        });
//        for (KeyListener listener : classificationBox.getKeyListeners()) {
//            classificationBox.removeKeyListener(listener);
//        }
//        row2.add(classificationBox);

        taskRowTextField = new JTextArea();
        taskRowTextField.setEditable(false);
//        row2.add(taskRowTextField);
        taskRowTextField.setFont(font1);
        row2.add(savedOrNotSaved);
        row2.add(saveButton);
        pageStart.add(row2);
        mainPannel.add(pageStart, BorderLayout.PAGE_START);


        taskRowTextField.setText("asdasdsad\n " +
                "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n" +
                        "asd\n"
                );

        JScrollPane scrollPane = new JScrollPane(taskRowTextField);
        keyWordsJTextField.setEditable(false);
        mainPannel.add(scrollPane, BorderLayout.CENTER);

        nextButton.addActionListener(e -> onNextClick());
        prevButton.addActionListener(e -> onPrevClick());
        saveButton.addActionListener(e -> onSaveClick());
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);
        saveButton.setEnabled(false);
        loadCurrentStateButton.addActionListener(e -> onLoadStateClick());
//        isForDev.addItemListener(e -> {
//            if (currentResult != null) {
//                if (e.getStateChange() == ItemEvent.SELECTED) {
//                    currentResult.isDev = true;
////                    classificationBox.setSelectedIndex(13);
//                } else {
//                    currentResult.isDev = false;
////                    classificationBox.setSelectedIndex(0);
//                }
//            }
//        });

        adapter = new DbAdapter();
    }

    public void onLoadStateClick() {
        mainPannel.requestFocus();
        if (userTextField.getText() == null || userTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please specify the user name");
            return;
        }
        if (jobTextField.getText() == null || jobTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please specify the task name");
            return;
        }
        if (currentUserId == null) {
            try {
                currentUserId = adapter.getUserIdByName(userTextField.getText());
                if (currentUserId == null) {
                    JOptionPane.showMessageDialog(frame, "Can not find such user ");
                    return;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                JOptionPane.showMessageDialog(frame, "ERROR: " + throwables.getMessage());
                return;
            }
        }
        if (currentJob == null) {
            try {
                currentJob = adapter.getJobIdByName(jobTextField.getText());
                if (currentJob == null) {
                    JOptionPane.showMessageDialog(frame, "Can not find such job ");
                    return;
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                JOptionPane.showMessageDialog(frame, "ERROR: " + throwables.getMessage());
                return;
            }
        }
        try {
            allResults = adapter.getAllCurrentTasksForUser(currentUserId, currentJob.getId());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            JOptionPane.showMessageDialog(frame, "ERROR: " + throwables.getMessage());
            return;
        }
        if (!allResults.isEmpty()) {
            currentResult = allResults.get(allResults.size() - 1);
        } else {
            currentResult = null;
        }
        redraw();
    }

    private void redraw() {
        if (currentResult != null) {
            currentTaskField.setText(allResults.indexOf(currentResult) + "");
//            taskRowTextField.setText(currentResult.getTaskRow());
//            keyWordsJTextField.setText(String.join(",", currentResult.getKeyWord()));
//            isForDev.setSelected(currentResult.isDev);
//            classificationBox.setSelectedIndex(classifications.indexOf(currentResult.getClassification()));
            if (currentResult.getSavedDate() == null) {
                savedOrNotSaved.setText(NOT_SAVED);
            } else {
                savedOrNotSaved.setText(SAVED);
            }
        } else {
            classificationBox.setSelectedIndex(0);
            currentTaskField.setText("X");
            taskRowTextField.setText("");
            keyWordsJTextField.setText("");
            savedOrNotSaved.setText(NOT_SAVED);
            isForDev.setSelected(false);
        }
        if (currentResult != null && currentUserId != null && currentJob != null) {
            saveButton.setEnabled(true);
        }
        if (currentUserId != null && currentJob != null) {
            nextButton.setEnabled(true);
            prevButton.setEnabled(true);
        }
        isForDev.setEnabled(currentResult != null);
        frame.pack();
    }

    private void onPrevClick() {
        if (currentResult != null && currentResult.savedDate == null) {
            JOptionPane.showMessageDialog(frame, "Please save the results before going to another task!");
            return;
        }
        if (!allResults.isEmpty() && allResults.indexOf(currentResult) != 0) {
            currentResult = allResults.get(allResults.indexOf(currentResult) - 1);
            redraw();
        }
    }

    private void onSaveClick() {
        if (currentResult != null && currentUserId != null && currentJob != null) {
//            if (currentResult.isDev && classificationBox.getSelectedIndex() == 0) {
//                JOptionPane.showMessageDialog(frame, "If you marked the sentence as DEV," +
//                        " classification should not be 'NON-DEV'");
//                return;
//            }
//            if (!currentResult.isDev && classificationBox.getSelectedIndex() != 0) {
//                JOptionPane.showMessageDialog(frame, "If you marked the sentence as NON DEV," +
//                        " classification should be 'NON-DEV'");
//                return;
//            }
            try {
                adapter.saveTaskWithResult(currentResult);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
                JOptionPane.showMessageDialog(frame, "ERROR: " + throwables.getMessage());
            }
        }
        redraw();
    }

    private void onNextClick() {
        if (currentResult != null && currentResult.savedDate == null) {
            JOptionPane.showMessageDialog(frame, "Please save the results before going to another task!");
            return;
        }
        int currentPos = allResults.indexOf(currentResult);
        if (allResults.isEmpty() || currentPos == allResults.size() - 1) {
            try {
                DbAdapter.TaskWithResult newTask;
                if (userTextField.getText().toLowerCase().startsWith("expert")) {
                    newTask = adapter.getNextTaskForExpert(currentUserId, currentJob.getId(), currentJob.getPersonPerTask());
                } else {
                    newTask = adapter.getNextTaskForUser(currentUserId, currentJob.getId(), currentJob.getPersonPerTask());
                }
                if (newTask == null) {
                    JOptionPane.showMessageDialog(frame, "There is no any more tasks for you in this job");
                    return;
                }
                currentResult = newTask;
                allResults.add(currentResult);
            } catch (SQLException throwables) {
                JOptionPane.showMessageDialog(frame, "ERROR: " + throwables.getMessage());
                return;
            }
        } else {
            currentResult = allResults.get(currentPos + 1);
        }
        redraw();
    }


    private void onTaskNumberChanged() {
        if (allResults == null || allResults.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "There is no anything to load");
            return;
        }
        if (currentResult != null && currentResult.savedDate == null) {
            JOptionPane.showMessageDialog(frame, "Please save the results before going to another task!");
            return;
        }
        int goalTask;
        try {
            goalTask = Integer.parseInt(currentTaskField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Please specify proper number of the task");
            if (currentResult != null) {
                currentTaskField.setText(allResults.indexOf(currentResult) + "");
            }
            return;
        }
        if (allResults != null && !allResults.isEmpty() && goalTask >= allResults.size()) {
            JOptionPane.showMessageDialog(frame, "You don't have such task number in saved list of tasks");
            return;
        }
        currentResult = allResults.get(goalTask);
        redraw();
    }

    public static boolean openGoogleSearch(String message) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                URL url = new URL("http://www.google.com/search?q=" + URLEncoder.encode(message));
                desktop.browse(url.toURI());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean openGoogleTranslate(String message) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                URL url = new URL("https://translate.google.com.ua/?hl=en&tab=wT&sl=en&tl=ru&op=translate&text=" + URLEncoder.encode(message));
                desktop.browse(url.toURI());
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void main(String[] args) throws SQLException {
        new MainWindow();
    }


}
