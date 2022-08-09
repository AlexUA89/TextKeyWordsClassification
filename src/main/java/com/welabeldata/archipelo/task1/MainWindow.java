package com.welabeldata.archipelo.task1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MainWindow {
    public static String APP_NAME = "WeLabelData label tool";
    public static String SAVED = "SAVED";
    public static String NOT_SAVED = "NOT SAVED";

    private JFrame frame;
    private JPanel mainPannel;
    private JPanel row1;
    private JPanel row2;
    private JLabel savedOrNotSaved = new JLabel(NOT_SAVED);
    private JTextArea currentTaskField = new JTextArea("X");

    private JButton nextButton = new JButton("Next");
    private JButton prevButton = new JButton("Prev");
    private JButton loadCurrentStateButton = new JButton("Load latest state");
    private JButton saveButton = new JButton("Save task");
    private JTextArea textArea;
    private JTextField jobTextField = new JTextField("", 10);
    private JTextField userTextField = new JTextField("", 10);

    Highlighter.HighlightPainter questionPainter =
            new DefaultHighlighter.DefaultHighlightPainter(Color.MAGENTA);
    Highlighter.HighlightPainter devPainter =
            new DefaultHighlighter.DefaultHighlightPainter(Color.GREEN);
    Highlighter.HighlightPainter nonDevPainter =
            new DefaultHighlighter.DefaultHighlightPainter(Color.RED);

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
        frame.setVisible(true);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);


        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {
                    if (userTextField.isFocusOwner() || jobTextField.isFocusOwner()) {
                        return false;
                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.VK_ENTER == e.getKeyCode()) {
                        onTaskNumberChanged();
                        return true;
                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.VK_UP == e.getKeyCode()) {
//
//                        return true;
//                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.VK_DOWN == e.getKeyCode()) {
//                        int pos = textArea.getSelectionEnd();
//                        textArea.setSelectionStart(pos + 100);
//                        textArea.setSelectionEnd(pos + 100+5);
//                        return true;
//                    }

                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("A")) {
                        if (prevButton.isEnabled()) {
                            onPrevClick();
                        }
                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("D")) {
                        if (nextButton.isEnabled()) {
                            onNextClick();
                        }
                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("S")) {
                        if (saveButton.isEnabled()) {
                            onSaveClick();
                        }
                    }
//                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("W")) {
//                        if (nextButton.isEnabled()) {
//                            currentResult.isDev = !currentResult.isDev;
//                            redraw();
//                        }
//                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("E")) {
                        String word = Optional.ofNullable(textArea.getSelectedText()).orElse("");
                        int startSelected = textArea.getSelectionStart();
                        int endSelected = textArea.getSelectionEnd();
                        if (!word.isEmpty()) {
                            for (DbAdapter.KeyWord keyWord : currentResult.getKeyWords()) {
                                if (keyWord.getStartPos() >= startSelected && keyWord.getEndPos() <= endSelected) {
                                    DbAdapter.ClassifiedKeyWord classifiedKeyWord = currentResult.getClassifiedKeyWords()
                                            .stream().filter(r -> r.getStartPos() == keyWord.getStartPos()
                                                    && r.getEndPos() == keyWord.getEndPos()).findAny().orElse(null);
                                    if (classifiedKeyWord == null) {
                                        currentResult.getClassifiedKeyWords().add(new DbAdapter.ClassifiedKeyWord(keyWord, DbAdapter.DEV));
                                    } else {
                                        classifiedKeyWord.setClassification(DbAdapter.DEV);
                                    }
                                    redraw();
                                    return false;
                                }
                            }
                        }
                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("W")) {

                        String word = Optional.ofNullable(textArea.getSelectedText()).orElse("");
                        int startSelected = textArea.getSelectionStart();
                        int endSelected = textArea.getSelectionEnd();
                        if (!word.isEmpty()) {
                            for (DbAdapter.KeyWord keyWord : currentResult.getKeyWords()) {
                                if (keyWord.getStartPos() >= startSelected && keyWord.getEndPos() <= endSelected) {
                                    DbAdapter.ClassifiedKeyWord classifiedKeyWord = currentResult.getClassifiedKeyWords()
                                            .stream().filter(r -> r.getStartPos() == keyWord.getStartPos()
                                                    && r.getEndPos() == keyWord.getEndPos()).findAny().orElse(null);
                                    if (classifiedKeyWord != null) {
                                        currentResult.getClassifiedKeyWords().remove(classifiedKeyWord);
                                        redraw();
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("Q")) {
                        String word = Optional.ofNullable(textArea.getSelectedText()).orElse("");
                        int startSelected = textArea.getSelectionStart();
                        int endSelected = textArea.getSelectionEnd();
                        if (!word.isEmpty()) {
                            for (DbAdapter.KeyWord keyWord : currentResult.getKeyWords()) {
                                if (keyWord.getStartPos() >= startSelected && keyWord.getEndPos() <= endSelected) {
                                    DbAdapter.ClassifiedKeyWord classifiedKeyWord = currentResult.getClassifiedKeyWords()
                                            .stream().filter(r -> r.getStartPos() == keyWord.getStartPos()
                                                    && r.getEndPos() == keyWord.getEndPos()).findAny().orElse(null);
                                    if (classifiedKeyWord == null) {
                                        currentResult.getClassifiedKeyWords().add(new DbAdapter.ClassifiedKeyWord(keyWord, DbAdapter.NON_DEV));
                                    } else {
                                        classifiedKeyWord.setClassification(DbAdapter.NON_DEV);
                                    }
                                    redraw();
                                    return false;
                                }
                            }
                        }
                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("G")) {
                        String word = Optional.ofNullable(textArea.getSelectedText()).orElse("");
                        if (!word.isEmpty()) {
                            return openGoogleSearch(word);
                        }
                    }
                    if (KeyEvent.KEY_RELEASED == e.getID() && KeyEvent.getKeyText(e.getKeyCode()).equals("T")) {
                        String word = Optional.ofNullable(textArea.getSelectedText()).orElse("");
                        if (!word.isEmpty()) {
                            return openGoogleTranslate(word);
                        }
                    }
                    return false;
                });

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
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(font1);
        textArea.setLineWrap(true);
        row2.add(savedOrNotSaved);
        row2.add(saveButton);
        pageStart.add(row2);
        mainPannel.add(pageStart, BorderLayout.PAGE_START);
        textArea.setFont(textArea.getFont().deriveFont(14f));
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPannel.add(scrollPane, BorderLayout.CENTER);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        InputMap im = vertical.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        im.put(KeyStroke.getKeyStroke("F"), "positiveUnitIncrement");
        im.put(KeyStroke.getKeyStroke("R"), "negativeUnitIncrement");

        nextButton.addActionListener(e -> onNextClick());
        prevButton.addActionListener(e -> onPrevClick());
        saveButton.addActionListener(e -> onSaveClick());
        nextButton.setEnabled(false);
        prevButton.setEnabled(false);
        saveButton.setEnabled(false);
        loadCurrentStateButton.addActionListener(e -> onLoadStateClick());

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

    private String hashFunction(DbAdapter.KeyWord word) {
        return word.getStartPos() + "," + word.getEndPos();
    }

    private void redraw() {
        if (currentResult != null) {
            currentTaskField.setText(allResults.indexOf(currentResult) + "");
            if (!textArea.getText().equals(currentResult.getContent())) {
                textArea.setText(currentResult.getContent());
            }
            Highlighter highlighter = textArea.getHighlighter();
            highlighter.removeAllHighlights();
            Map<String, DbAdapter.ClassifiedKeyWord> classWordsMap = currentResult.getClassifiedKeyWords()
                    .stream().collect(Collectors.toMap(this::hashFunction, Function.identity()));
            currentResult.getKeyWords().forEach(w -> {
                if (!classWordsMap.containsKey(hashFunction(w))) {
                    try {
                        highlighter.addHighlight(w.getStartPos(), w.getEndPos(), questionPainter);
                    } catch (BadLocationException e) {
                        e.printStackTrace();
                    }
                }
            });

            currentResult.getClassifiedKeyWords().forEach(classifiedKeyWord -> {
                try {
                    if (classifiedKeyWord.getClassification().equals(DbAdapter.NON_DEV)) {
                        highlighter.addHighlight(classifiedKeyWord.getStartPos(),
                                classifiedKeyWord.getEndPos(), nonDevPainter);
                    } else {
                        highlighter.addHighlight(classifiedKeyWord.getStartPos(),
                                classifiedKeyWord.getEndPos(), devPainter);
                    }
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            });

            if (currentResult.getSavedDate() == null) {
                savedOrNotSaved.setText(NOT_SAVED);
            } else {
                savedOrNotSaved.setText(SAVED);
            }
        } else {
            currentTaskField.setText("X");
            textArea.setText("");
            savedOrNotSaved.setText(NOT_SAVED);
        }
        if (currentResult != null && currentUserId != null && currentJob != null) {
            saveButton.setEnabled(true);
        }
        if (currentUserId != null && currentJob != null) {
            nextButton.setEnabled(true);
            prevButton.setEnabled(true);
        }
        textArea.setEnabled(currentResult != null);
//        frame.pack();
    }

    private void onPrevClick() {
        if (currentResult != null && currentResult.getSavedDate() == null) {
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
            Set<String> highPos = currentResult.getKeyWords()
                    .stream().map(this::hashFunction).collect(Collectors.toSet());
            currentResult.getClassifiedKeyWords().forEach(r -> highPos.remove(hashFunction(r)));
            if (!highPos.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "You have not processed all keywords inside this text. " +
                        "Please finish it before the save");
                return;
            }
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
        if (currentResult != null && currentResult.getSavedDate() == null) {
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
        if (currentResult != null && currentResult.getSavedDate() == null) {
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
