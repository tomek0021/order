package order;

enum Side {
    Bid, Offer;

    char toChar() {
        return this == Bid ? 'B' : 'O';
    }

    public static Side from(char character) {
        if (character != 'B' && character != 'O') throw new IllegalArgumentException("Side can be only 'B' or 'O', but got: " + character);
        return character == 'B' ? Side.Bid : Side.Offer;
    }
}
