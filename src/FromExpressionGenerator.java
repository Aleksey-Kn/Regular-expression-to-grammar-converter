import java.util.*;
import java.util.stream.Collectors;

public class FromExpressionGenerator {
    private final String regular;
    private int maxSize;

    public FromExpressionGenerator(String regular) {
        this.regular = regular.replace("\n", "").replace(" ", "");
        if (regular.chars().filter(c -> c == '(').count() != regular.chars().filter(c -> c == ')').count())
            throw new IllegalArgumentException("Count '(' not equal count ')'");
        if(regular.chars().anyMatch(Character::isUpperCase))
            throw new IllegalArgumentException("Symbol must be lower case");
    }

    public List<String[]> generateChains(int minLength, int maxLength) {
        maxSize = maxLength;
        ArrayList<String[]> results = new ArrayList<>();
        results.add(new String[]{"", "'lambda'"});
        if (regular.charAt(0) == '('
                && regular.charAt(regular.length() - 1) == '*'
                && indexCloseFor(regular) == regular.length() - 2)
            results = manyStep(results, split(regular.substring(1, regular.length() - 2), '+'));
        else {
            if (regular.charAt(0) == '(' && indexCloseFor(regular) == regular.length() - 1)
                results = oneStep(results, split(regular.substring(1, regular.length() - 1), '+'));
            else
                results = oneStep(results, split(regular, '+'));
        }
        return results.stream()
                .filter(s -> s[0].length() <= maxLength)
                .filter(s -> s[0].length() >= minLength)
                .sorted(Comparator.comparing(c -> c[0]))
                .collect(Collectors.toList());
    }

    public List<String> generateGrammar() {
        Set<Character> terminals = regular.chars()
                .filter(c -> Character.isDigit(c) || Character.isLowerCase(c))
                .mapToObj(i -> (char) i)
                .collect(Collectors.toSet());
        Map<String, Character> parentalRegulations = new HashMap<>();
        int startInd = 0;
        String substr;
        while (true) {
            substr = searchNextBlock(regular, startInd);
            if(substr == null)
                break;
            if (!parentalRegulations.containsKey(substr)) {
                parentalRegulations.put(substr, (char) ('A' + parentalRegulations.size()));
            }
            startInd++;
        }

        Set<String> realRegulations = new HashSet<>(parentalRegulations.size() + 1);
        for (Map.Entry<String, Character> entry : parentalRegulations.entrySet()) { // преобразуем кадждое правило
            realRegulations.add(entry.getValue() + "->" + replacePart(entry.getKey(), parentalRegulations));
        }
        Set<Character> nonTerminal = new TreeSet<>(parentalRegulations.values());
        char startNonTerminal = 'A';
        if(regular.charAt(0) != '('
                || !(indexCloseFor(regular) == regular.length() - 1 || indexCloseFor(regular) == regular.length() - 2)){
            startNonTerminal = 'S';
            nonTerminal.add('S');
            realRegulations.add("S->" + replaceAllBlocks(regular, parentalRegulations));
        }
        StringBuilder heading = new StringBuilder("G({");
        terminals.forEach(t -> heading.append(t).append(", "));
        heading.insert(heading.length() - 2, '}').append('{');
        nonTerminal.forEach(n -> heading.append(n).append(", "));
        heading.insert(heading.length() - 2, '}').append("P, ").append(startNonTerminal).append(')');
        LinkedList<String> result = new LinkedList<>();
        result.add(heading.toString());
        result.add("P:");
        result.addAll(realRegulations);
        return result;
    }

    private String replacePart(String key, Map<String, Character> regulation){
        boolean repeating;
        char rulesName = regulation.get(key);
        if (key.charAt(key.length() - 1) == '*') {
            repeating = true;
            key = key.substring(1, key.length() - 2);
        } else {
            repeating = false;
            key = key.substring(1, key.length() - 1);
        }
        key = replaceAllBlocks(key, regulation);
        if(key.contains("(")){
            key = Arrays.stream(key.split("\\|"))
                    .map(k -> replacePart(k, regulation) + '|')
                    .collect(Collectors.joining());
            key = key.substring(0, key.length() - 1);
        }
        if(repeating) {
            key = Arrays.stream(split(key, '|'))
                    .map(a -> a + rulesName + '|')
                    .collect(Collectors.joining())
                    .concat("'lambda'");
        }
        return key;
    }

    private String replaceAllBlocks(String parseString, Map<String, Character> regulations){
        int startInd = 0;
        String nowBlock;
        while (true) { // ищем все скобки, чтобы заменить
            nowBlock = searchNextBlock(parseString, startInd);
            if(nowBlock == null)
                break;
            parseString = parseString.replace(nowBlock, Character.toString(regulations.get(nowBlock)));
        }
        return parseString.replace('+', '|');
    }

    private String searchNextBlock(String s, int startInd){
        int endInd;
        String result;
        startInd = s.indexOf('(', startInd);
        if (startInd == -1)
            return null;
        endInd = indexCloseFor(s, startInd);
        result = s.substring(startInd, endInd + 1);
        if (s.length() > endInd + 1 && s.charAt(endInd + 1) == '*')
            result += '*';
        return result;
    }

    private ArrayList<String[]> oneStep(ArrayList<String[]> previousResult, String[] options) {
        ArrayList<String[]> tempResult;
        ArrayList<String[]> newResult = new ArrayList<>();
        StringBuilder state;
        int ind;
        for (String nowOptions : options) {
            tempResult = new ArrayList<>(previousResult);
            state = new StringBuilder(nowOptions);
            while (!state.isEmpty()) {
                if (state.charAt(0) == '(') {
                    ind = indexCloseFor(state.toString());
                    String now = state.substring(1, ind);
                    if (ind != state.length() - 1 && state.charAt(ind + 1) == '*') {
                        tempResult = manyStep(tempResult, split(now, '+'));
                        state.delete(0, ind + 2);
                    } else {
                        tempResult = oneStep(tempResult, split(now, '+'));
                        state.delete(0, ind + 1);
                    }
                } else {
                    ind = state.indexOf("(");
                    if (ind == -1)
                        ind = state.length();
                    String now = state.substring(0, ind);
                    state.delete(0, ind);
                    for (int i = 0; i < tempResult.size(); i++)
                        tempResult.set(i, new String[]{tempResult.get(i)[0] + now,
                                tempResult.get(i)[1] + "->" + tempResult.get(i)[0] + now});
                }
            }
            newResult.addAll(tempResult);
        }
        return newResult;
    }

    private ArrayList<String[]> manyStep(ArrayList<String[]> previousResult, String[] options) {
        ArrayList<String[]> newResult = new ArrayList<>(previousResult);
        ArrayList<String[]> tempResult = new ArrayList<>(previousResult);
        while (true) {
            tempResult = oneStep(tempResult, options);
            if (tempResult.stream().allMatch(e -> e[0].length() > maxSize)) {
                return newResult.stream()
                        .filter(e -> e[0].length() <= maxSize)
                        .collect(Collectors.toCollection(ArrayList::new));
            }
            newResult.addAll(tempResult);
        }
    }

    private int indexCloseFor(String nowRegular) {
        if (nowRegular.charAt(0) != '(')
            throw new IllegalArgumentException("String must start on '('");
        int count = 1;
        char[] reg = nowRegular.toCharArray();
        for (int i = 1; i < nowRegular.length(); i++) {
            if (reg[i] == '(')
                count++;
            else if (reg[i] == ')') {
                count--;
                if (count == 0)
                    return i;
            }
        }
        return -1;
    }

    private int indexCloseFor(String nowRegular, int startIndex) {
        if (nowRegular.charAt(startIndex) != '(')
            throw new IllegalArgumentException("String must have '(' on current index");
        int count = 1;
        char[] reg = nowRegular.toCharArray();
        for (int i = 1 + startIndex; i < nowRegular.length(); i++) {
            if (reg[i] == '(')
                count++;
            else if (reg[i] == ')') {
                count--;
                if (count == 0)
                    return i;
            }
        }
        return -1;
    }

    private String[] split(String s, char splitChar) {
        LinkedList<String> spliting = new LinkedList<>();
        int count = 0;
        int prev = 0;
        char[] reg = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            if (reg[i] == splitChar) {
                if (count == 0) {
                    spliting.add(s.substring(prev, i));
                    prev = i + 1;
                }
            } else if (reg[i] == '(')
                count++;
            else if (reg[i] == ')') {
                count--;
            }
        }
        spliting.add(s.substring(prev));
        return spliting.toArray(String[]::new);
    }
}
