import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DFA {
    private String[] states;
    private String[] inputAlphabet;
    private String initialState;
    private String[] favorableStates;
    private Map<String, Map<String, String>> deltaFunctionTransitions;

    public static class InvalidDFADefinition extends Exception {
        public InvalidDFADefinition(String message) {
            super(message);
        }
    }

    public DFA(String sourceUrl) throws InvalidDFADefinition {
        String[] data = simpleReadData(sourceUrl);
        if (data.length != 5 ) {
            throw new DFA.InvalidDFADefinition(String.format("Expected 5 lines containing dfa definition; found %d", data.length));
        }

        HashSet<String> statesSet = new HashSet<>();
        for (String state : data[0].split("\\s+")) {
            if (state.length() != 1 || !Character.isLetter(state.charAt(0))) {
                throw new DFA.InvalidDFADefinition(String.format("Expected single alpha characters separated by single spaces; found '%s'", state));
            }
            statesSet.add(state);
        }
        if (statesSet.size() == 0) {
            throw new DFA.InvalidDFADefinition("Expected a DFA to contain at least one state; found 0");
        }
        states = statesSet.toArray(String[]::new);

        inputAlphabet = data[1].split("\\s+");
        for (String input : inputAlphabet) {
            if (input.length() != 1 || !(Character.isDigit(input.charAt(0))) || Character.isLetter(input.charAt(0))) {
                throw new DFA.InvalidDFADefinition(String.format("Expected single alpha-numeric characters separated by single spaces; found '%s'", input));
            };
        }
        if (inputAlphabet.length > statesSet.size()) {
            throw new DFA.InvalidDFADefinition(String.format("Expected a DFA alphabet to less than or equal to the number of states; found %d states and %d alphabet characters", statesSet.size(), inputAlphabet.length));
        }

        initialState = data[2].strip();
        if (initialState.length() != 1 || !statesSet.contains(initialState)) {
            throw new DFA.InvalidDFADefinition(String.format("Expected initial state to be one of the allowed states %s; found '%s'", Arrays.toString(states), initialState));
        }

        favorableStates = data[3].split("\\s+");
        for (String favorableState : favorableStates) {
            if (!statesSet.contains(favorableState)) {
                throw new DFA.InvalidDFADefinition(String.format("Expected favorable state to be one of the allowed states %s; found '%s'", Arrays.toString(states), favorableState));
            }
        }

        String[] potentialDeltaFunctions = data[4].split("\\s+");
        if (potentialDeltaFunctions.length != (statesSet.size()*inputAlphabet.length)) {
            throw new DFA.InvalidDFADefinition(String.format("Expected %d items in the delta function transition table; found %d", statesSet.size() * inputAlphabet.length, potentialDeltaFunctions.length));
        }

        deltaFunctionTransitions = new HashMap<>();
        for (int i = 0; i < statesSet.size(); i++) {
            String state = states[i];
            for (int j = 0; j < inputAlphabet.length; j++) {
                String input = inputAlphabet[j];
                String outputState = potentialDeltaFunctions[(i * inputAlphabet.length) + j];
                if (!statesSet.contains(outputState)) {
                    throw new InvalidDFADefinition(String.format("Expected delta transition function state to be one of the allowed states %s; found '%s'", Arrays.toString(states), outputState));
                }

                deltaFunctionTransitions.computeIfAbsent(state, s -> new HashMap<>());
                deltaFunctionTransitions.get(state).put(input, outputState);
            }
        }
    }

    public boolean isAccepted(String inputSequence) {
        String currentState = initialState;
        for (char inputChar : inputSequence.toCharArray()) {
            if (!deltaFunctionTransitions.containsKey(currentState)) {
                return false;
            }
            Map<String, String> inputMap = deltaFunctionTransitions.get(currentState);
            String input = String.valueOf(inputChar);
            if (!inputMap.containsKey(input)) {
                return false;
            }
            currentState = inputMap.get(input);
        }
        return Arrays.asList(favorableStates).contains(currentState);
    }

    public String[] getStates() {
        return states;
    }

    public String[] getInputAlphabet() {
        return inputAlphabet;
    }

    public String getInitialState() {
        return initialState;
    }

    public String[] getFavorableStates() {
        return favorableStates;
    }

    public Map<String, Map<String, String>> getDeltaFunctionTransitions() {
        return deltaFunctionTransitions;
    }

    public static String[] simpleReadData(String urlName) {
        String[] info = null;

        try {
            URL url = new URL(urlName);
            In fileInput = new In(url);

            if (fileInput.exists()) {
                System.out.println("fileInput.exists()   ");
                String inputData = fileInput.readAll();
                info = inputData.split("\n");
            } else {
                System.out.println("   NOT   fileInput.exists()   ");
                System.out.println("could not find url:  " + urlName + "\n\n");
            }
        } catch (MalformedURLException ex) {
            System.out.println("error trying to read URL:\n  " + urlName);
        }
        return info;
    }
}
