package net.apuig.order.pay;

public record GenerateTokenResult(String token, String error)
{
    public static GenerateTokenResult ok(String token)
    {
        return new GenerateTokenResult(token, null);
    }

    public static GenerateTokenResult error(String error)
    {
        return new GenerateTokenResult(null, error);
    }

    public boolean isError()
    {
        return error != null;
    }
}
