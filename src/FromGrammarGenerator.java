import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FromGrammarGenerator {
    private final Map<Character, Set<String>> regulations = new HashMap<>();
    private final char startNonTerminal;
    private int minLength, maxLength;

    public FromGrammarGenerator(String handle, String p, Set<String> rules){
        handle = handle.replace(" ", "");
        handle = handle.substring(2, handle.length() - 1);
        Set<Character> terminal = Arrays.stream(handle.substring(1, handle.indexOf('}')).split(","))
                .map(s -> s.charAt(0))
                .collect(Collectors.toSet());
        if(terminal.stream().anyMatch(Character::isUpperCase))
            throw new IllegalArgumentException("Terminal symbol must be lower case");

        Set<Character> nonTerminal = Arrays
                .stream(handle.substring(handle.lastIndexOf('{') + 1, handle.lastIndexOf('}')).split(","))
                .map(s -> s.charAt(0))
                .collect(Collectors.toSet());
        if(nonTerminal.stream().anyMatch(c -> Character.isLowerCase(c) || Character.isDigit(c)))
            throw new IllegalArgumentException("Non-terminal symbol must be upper case");

        char realP = handle.charAt(handle.lastIndexOf('}') + 2);
        startNonTerminal = handle.charAt(handle.length() - 1);

        String[] temp;
        p = p.trim();
        Consumer<Set<String>> check = (set -> {
            for(String s: set){
                if(!s.equals("'lambda'") &&
                        !s.chars().allMatch(c -> terminal.contains((char) c) || nonTerminal.contains((char) c)))
                    throw new IllegalArgumentException(s + " contains unknown symbol");
                }
        });
        if(p.charAt(0) != realP)
            throw new IllegalArgumentException("Not contain set rules. Expected " + realP + " symbol.");
        if(p.length() > 2){
            temp = p.substring(3).trim().split("->");
            if(Character.isLowerCase(temp[0].charAt(0)))
                throw new IllegalArgumentException("Rules must be non-terminal symbol");
            regulations.put(temp[0].charAt(0),
                    Arrays.stream(temp[1].split("\\|")).map(String::trim).collect(Collectors.toSet()));
            check.accept(regulations.get(temp[0].charAt(0)));
        }
        rules.forEach(r -> {
            String[] t;
            t = r.trim().split("->");
            if(Character.isLowerCase(t[0].charAt(0)))
                throw new IllegalArgumentException("Rules must be non-terminal symbol");
            regulations.put(t[0].charAt(0),
                    Arrays.stream(t[1].split("\\|")).map(String::trim).collect(Collectors.toSet()));
            check.accept(regulations.get(t[0].charAt(0)));
        });
    }

    public List<String[]> generateChains(int minSize, int maxSize){
        minLength = minSize;
        maxLength = maxSize;
        List<String[]> result = new LinkedList<>();
        creator(Character.toString(startNonTerminal), Character.toString(startNonTerminal), result);
        Comparator<String[]> comparator = Comparator.comparing(strings -> strings[0]);
        result.sort(comparator);
        return result;
    }

    private void creator(String now, String log, List<String[]> result){
        now = now.replace("'lambda'", "");
        if(now.length() - now.chars().filter(Character::isUpperCase).count() <= maxLength) {
            if(now.length() <= maxLength
                    && now.length() >= minLength
                    && now.chars().allMatch(c -> Character.isLowerCase(c) || Character.isDigit(c))){
                result.add(new String[]{now, log});
            }
            String replacement;
            for (char c : now.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    for (String newSubstr : regulations.get(c)) {
                        replacement = now.replaceFirst(Character.toString(c), newSubstr);
                        creator(replacement, log.concat("->").concat(replacement), result);
                    }
                    break;
                }
            }
        }
    }
}