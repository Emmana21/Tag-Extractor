import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.util.List; // Explicit import for List
import java.util.ArrayList; // Explicit import for ArrayList
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class TagExtractorGUI extends JFrame {
    private JTextArea tagTextArea;
    private JButton chooseFileButton, chooseStopWordsButton, extractButton, saveButton;
    private JFileChooser fileChooser;
    private Path filePath, stopWordsFilePath;
    private Map<String, Integer> tagFrequencyMap;

    public TagExtractorGUI() {
        setTitle("Tag Extractor");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        chooseFileButton = new JButton("Choose Text File");
        chooseFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFile();
            }
        });
        topPanel.add(chooseFileButton);

        chooseStopWordsButton = new JButton("Choose Stop Words File");
        chooseStopWordsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseStopWordsFile();
            }
        });
        topPanel.add(chooseStopWordsButton);

        add(topPanel, BorderLayout.NORTH);

        tagTextArea = new JTextArea();
        tagTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(tagTextArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        extractButton = new JButton("Extract Tags");
        extractButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                extractTags();
            }
        });
        bottomPanel.add(extractButton);

        saveButton = new JButton("Save Tags");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveTags();
            }
        });
        saveButton.setEnabled(false);
        bottomPanel.add(saveButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void chooseFile() {
        fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            filePath = fileChooser.getSelectedFile().toPath();
            tagTextArea.setText("Processing file: " + filePath.getFileName() + "\n");
        }
    }

    private void chooseStopWordsFile() {
        fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            stopWordsFilePath = fileChooser.getSelectedFile().toPath();
        }
    }

    private void extractTags() {
        if (filePath == null || stopWordsFilePath == null) {
            JOptionPane.showMessageDialog(this, "Please select both text file and stop words file.");
            return;
        }

        try {
            tagFrequencyMap = new HashMap<>();
            Set<String> stopWords = loadStopWords(stopWordsFilePath);

            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                String[] words = line.split("\\s+");
                for (String word : words) {
                    word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    if (!stopWords.contains(word)) {
                        tagFrequencyMap.put(word, tagFrequencyMap.getOrDefault(word, 0) + 1);
                    }
                }
            }

            displayTags();
            saveButton.setEnabled(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading files: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Set<String> loadStopWords(Path stopWordsFilePath) throws IOException {
        return new TreeSet<>(Files.readAllLines(stopWordsFilePath));
    }

    private void displayTags() {
        StringBuilder tags = new StringBuilder();
        for (Map.Entry<String, Integer> entry : tagFrequencyMap.entrySet()) {
            tags.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        tagTextArea.append(tags.toString());
    }

    private void saveTags() {
        fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            Path saveFilePath = fileChooser.getSelectedFile().toPath();
            try (BufferedWriter writer = Files.newBufferedWriter(saveFilePath)) {
                for (Map.Entry<String, Integer> entry : tagFrequencyMap.entrySet()) {
                    writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
                }
                JOptionPane.showMessageDialog(this, "Tags saved successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving tags: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TagExtractorGUI().setVisible(true);
            }
        });
    }
}
