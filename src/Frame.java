import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Frame extends JFrame {
    private boolean editExpression = true;
    private boolean editGrammar = true;
    private FromExpressionGenerator fromExpressionGenerator;
    private FromGrammarGenerator fromGrammarGenerator;
    private List<String[]> stringsFromExpression;
    private List<String[]> stringsFromGrammar;
    boolean normalState = true;

    private Frame() {
        super("Grammar generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds(200, 100, 850, 500);
        setLayout(new BorderLayout());

        JPanel inputPane = new JPanel();
        inputPane.setLayout(new BoxLayout(inputPane, BoxLayout.Y_AXIS));
        add(inputPane, BorderLayout.WEST);
        JPanel centerPane = new JPanel();
        centerPane.setLayout(new BorderLayout());
        JPanel indexPanel = new JPanel();
        indexPanel.setLayout(new BoxLayout(indexPanel, BoxLayout.X_AXIS));
        centerPane.add(indexPanel, BorderLayout.NORTH);
        JPanel rightCenter = new JPanel();
        rightCenter.setLayout(new BoxLayout(rightCenter, BoxLayout.Y_AXIS));
        centerPane.add(rightCenter, BorderLayout.EAST);
        JPanel leftCenter = new JPanel();
        leftCenter.setLayout(new BoxLayout(leftCenter, BoxLayout.Y_AXIS));
        centerPane.add(leftCenter, BorderLayout.WEST);
        JPanel chainsPane = new JPanel();
        chainsPane.setLayout(new BoxLayout(chainsPane, BoxLayout.X_AXIS));
        centerPane.add(chainsPane, BorderLayout.CENTER);
        add(centerPane, BorderLayout.CENTER);
        JPanel expressionPane = new JPanel();
        expressionPane.setLayout(new BoxLayout(expressionPane, BoxLayout.Y_AXIS));
        add(expressionPane, BorderLayout.EAST);
        JPanel sizePanel = new JPanel();
        sizePanel.setLayout(new BoxLayout(sizePanel, BoxLayout.X_AXIS));
        add(sizePanel, BorderLayout.NORTH);
        JPanel menuPanel = new JPanel();
        add(menuPanel, BorderLayout.SOUTH);

        indexPanel.add(new JLabel("Index: "));
        JTextField index = new JTextField();
        indexPanel.add(index);

        JButton rightDelete = new JButton("Delete");
        rightCenter.add(rightDelete);
        JButton rightUpdate = new JButton("Change");
        rightCenter.add(rightUpdate);
        JButton rightAdd = new JButton("Add");
        rightCenter.add(rightAdd);

        JButton leftDelete = new JButton("Delete");
        leftCenter.add(leftDelete);
        JButton leftUpdate = new JButton("Change");
        leftCenter.add(leftUpdate);
        JButton leftAdd = new JButton("Add");
        leftCenter.add(leftAdd);

        JButton validate = new JButton("Validate");
        centerPane.add(validate, BorderLayout.SOUTH);

        JPanel leftChainsPanel = new JPanel();
        leftChainsPanel.setLayout(new BoxLayout(leftChainsPanel, BoxLayout.Y_AXIS));
        chainsPane.add(new JScrollPane(leftChainsPanel));
        JPanel rightChainsPanel = new JPanel();
        rightChainsPanel.setLayout(new BoxLayout(rightChainsPanel, BoxLayout.Y_AXIS));
        JScrollPane rightScroll = new JScrollPane(rightChainsPanel);
        chainsPane.add(rightScroll);

        rightDelete.addActionListener(l -> {
            int ind = Integer.parseInt(index.getText());
            if (ind > 0 && ind < stringsFromGrammar.size()) {
                stringsFromGrammar.remove(ind);
                rightChainsPanel.remove(ind);
            }
        });
        leftDelete.addActionListener(l -> {
            int ind = Integer.parseInt(index.getText());
            if (ind > 0 && ind < stringsFromExpression.size()) {
                stringsFromExpression.remove(ind);
                leftChainsPanel.remove(ind);
            }
        });
        rightUpdate.addActionListener(l -> {
            int ind = Integer.parseInt(index.getText());
            String newValue = JOptionPane.showInputDialog("New value of chain");
            stringsFromGrammar.set(ind, newValue.split(":"));
            rightChainsPanel.remove(ind);
            rightChainsPanel.add(new JLabel(newValue), ind);
        });
        leftUpdate.addActionListener(l -> {
            int ind = Integer.parseInt(index.getText());
            String newValue = JOptionPane.showInputDialog("New value of chain");
            stringsFromExpression.set(ind, newValue.split(":"));
            leftChainsPanel.remove(ind);
            leftChainsPanel.add(new JLabel(newValue), ind);
        });
        leftAdd.addActionListener(l -> {
            if(index.getText().isBlank()){
                int ind = Integer.parseInt(index.getText());
                String newValue = JOptionPane.showInputDialog("Value of new chain");
                stringsFromExpression.add(ind, newValue.split(":"));
                leftChainsPanel.add(new JLabel(newValue), ind);
            } else {
                String newValue = JOptionPane.showInputDialog("Value of new chain");
                stringsFromExpression.add(newValue.split(":"));
                leftChainsPanel.add(new JLabel(newValue));
            }
        });
        rightAdd.addActionListener(l -> {
            if(index.getText().isBlank()){
                int ind = Integer.parseInt(index.getText());
                String newValue = JOptionPane.showInputDialog("Value of new chain");
                stringsFromGrammar.add(ind, newValue.split(":"));
                rightChainsPanel.add(new JLabel(newValue), ind);
            } else {
                String newValue = JOptionPane.showInputDialog("Value of new chain");
                stringsFromGrammar.add(newValue.split(":"));
                rightChainsPanel.add(new JLabel(newValue));
            }
        });

        JButton about = new JButton("About author");
        menuPanel.add(about);
        JButton theme = new JButton("Theme");
        menuPanel.add(theme);
        JButton open = new JButton("Open initial data");
        menuPanel.add(open);
        JButton saveExpression = new JButton("Save grammar to file");
        menuPanel.add(saveExpression);
        JButton saveLeftChains = new JButton("Save left chains to file");
        menuPanel.add(saveLeftChains);
        JButton saveRightChains = new JButton("Save right chains to file");
        menuPanel.add(saveRightChains);

        sizePanel.add(new JLabel("Size of chains: "));
        sizePanel.add(new JLabel("from "));
        JTextField fromSize = new JTextField();
        sizePanel.add(fromSize);
        sizePanel.add(new JLabel(" to "));
        JTextField toSize = new JTextField();
        sizePanel.add(toSize);

        inputPane.add(new JLabel("Regular expression:"));
        JTextArea expression = new JTextArea();
        expression.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                editExpression = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                editExpression = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                editExpression = true;
            }
        });
        inputPane.add(expression);
        JButton fromExpressionToChains = new JButton("Generate chains");
        inputPane.add(fromExpressionToChains);
        JButton fromExpressionToGrammar = new JButton("Generate grammar");
        inputPane.add(fromExpressionToGrammar);

        JTextArea grammar = new JTextArea();
        expressionPane.add(new JLabel("Grammar:"));
        grammar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                editGrammar = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                editGrammar = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                editGrammar = true;
            }
        });
        expressionPane.add(grammar);
        JButton fromGrammar = new JButton("Generate chains");
        expressionPane.add(fromGrammar);

        fromExpressionToChains.addActionListener(l -> {
            leftChainsPanel.removeAll();
            try {
                if (editExpression) {
                    fromExpressionGenerator = new FromExpressionGenerator(expression.getText());
                    editExpression = false;
                }
                stringsFromExpression = fromExpressionGenerator.generateChains(Integer.parseInt(fromSize.getText()),
                        Integer.parseInt(toSize.getText()));
                stringsFromExpression.forEach(s -> leftChainsPanel.add(new JLabel(s[0] + ": " + s[1])));
            } catch (Exception e) {
                JLabel exceptionLabel = new JLabel(e.getMessage());
                exceptionLabel.setForeground(Color.RED);
                leftChainsPanel.add(exceptionLabel);
                e.printStackTrace();
            }
            leftChainsPanel.updateUI();
        });
        fromExpressionToGrammar.addActionListener(l -> {
            grammar.setForeground(Color.BLACK);
            grammar.setText("");
            try {
                if (editExpression) {
                    fromExpressionGenerator = new FromExpressionGenerator(expression.getText());
                    editExpression = false;
                }
                fromExpressionGenerator.generateGrammar().forEach(s -> grammar.append(s + '\n'));
            } catch (Exception e) {
                grammar.setText(e.getMessage());
                grammar.setForeground(Color.RED);
                e.printStackTrace();
            }
        });
        fromGrammar.addActionListener(l -> {
            rightChainsPanel.removeAll();
            try {
                if (editGrammar) {
                    Scanner grammarScanner = new Scanner(grammar.getText());
                    String handle = grammarScanner.nextLine();
                    String p = grammarScanner.nextLine();
                    Set<String> strings = new HashSet<>();
                    while (grammarScanner.hasNextLine())
                        strings.add(grammarScanner.nextLine());
                    fromGrammarGenerator = new FromGrammarGenerator(handle, p, strings);
                    editGrammar = false;
                }
                stringsFromGrammar = fromGrammarGenerator
                        .generateChains(Integer.parseInt(fromSize.getText()), Integer.parseInt(toSize.getText()));
                stringsFromGrammar.forEach(s -> rightChainsPanel.add(new JLabel(s[0] + ": " + s[1])));
            } catch (Exception e) {
                JLabel exceptionLabel = new JLabel(e.getMessage());
                exceptionLabel.setForeground(Color.RED);
                rightChainsPanel.add(exceptionLabel);
                e.printStackTrace();
            }
            rightChainsPanel.updateUI();
        });

        validate.addActionListener(l -> {
            if(normalState){
                rightScroll.setVisible(false);
                centerPane.updateUI();

                rightAdd.setEnabled(false);
            } else{
                rightScroll.setVisible(true);
                centerPane.updateUI();

                rightAdd.setEnabled(true);
            }
            normalState = !normalState;
        });

        about.addActionListener(l ->
                JOptionPane.showMessageDialog(null, "Рыбников Данил Владимирович, ИП-816"));
        theme.addActionListener(l ->
                JOptionPane.showMessageDialog(null,
                        """
                                Написать программу, которая по заданному регулярному выражению построит эквивалентную грамматику (по желанию разработчика –
                                грамматика может быть контекстно-свободной или регулярной).
                                Программа должна сгенерировать по построенной грамматике и регулярному выражению множества всех цепочек в указанном диапазоне длин,
                                проверить их на совпадение. Процесс построения цепочек отображать
                                на экране. Для подтверждения корректности выполняемых действий
                                предусмотреть возможность корректировки любого из построенных множеств пользователем (изменение цепочки, добавление, удаление).
                                При обнаружении несовпадения в элементах множеств должна выдаваться диагностика различий – где именно несовпадения и в чём они состоят.
                                """));
        saveExpression.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(null,
                    "Введите имя файла для сохранения",
                    "Сохранение грамматики", JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                try {
                    FileWriter writer = new FileWriter(name + ".txt");
                    writer.write(grammar.getText());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveLeftChains.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(null,
                    "Введите имя файла для сохранения",
                    "Сохранение цепочек", JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                try {
                    PrintWriter writer = new PrintWriter(new FileWriter(name + ".txt"));
                    for (String[] component : stringsFromExpression) {
                        writer.println(component[0] + ": " + component[1]);
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        saveRightChains.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(null,
                    "Введите имя файла для сохранения",
                    "Сохранение цепочек", JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                try {
                    PrintWriter writer = new PrintWriter(new FileWriter(name + ".txt"));
                    for (String[] component : stringsFromGrammar) {
                        writer.println(component[0] + ": " + component[1]);
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        open.addActionListener(l -> {
            String name = JOptionPane.showInputDialog(null,
                    "Введите имя файла",
                    "Чтение регулярного выражения из файла", JOptionPane.QUESTION_MESSAGE);
            if (name != null) {
                try {
                    Scanner scanner = new Scanner(new File(name + ".txt"));
                    expression.setText(scanner.nextLine());
                    scanner.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new Frame();
    }
}
