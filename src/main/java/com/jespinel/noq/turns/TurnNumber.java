package com.jespinel.noq.turns;

public record TurnNumber(char letter, int number) {

    public static TurnNumber next(TurnNumber turnNumber) {
        int nextTurn = turnNumber.number() + 1;
        return new TurnNumber(turnNumber.letter(), nextTurn);
    }

    public static TurnNumber from(String turnNumber) {
        String intString = turnNumber.substring(1);
        int number = Integer.parseInt(intString);
        return new TurnNumber(turnNumber.charAt(0), number);
    }

    public static boolean isValid(String initialTurn) {
        boolean lengthIsAtLeastTwo = initialTurn.length() >= 2;
        boolean firstIsLetter = Character.isLetter(initialTurn.charAt(0));
        boolean secondIsDigit = Character.isDigit(initialTurn.charAt(1));

        return lengthIsAtLeastTwo && firstIsLetter && secondIsDigit;
    }

    @Override
    public String toString() {
        return "" + letter + number;
    }
}
