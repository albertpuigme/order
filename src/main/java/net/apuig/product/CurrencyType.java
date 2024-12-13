package net.apuig.product;

public enum CurrencyType
{
    EURO('€'), DOLLAR('$');

    final char symbol;

    CurrencyType(char symbol)
    {
        this.symbol = symbol;
    }

    public char symbol()
    {
        return symbol;
    }
}
