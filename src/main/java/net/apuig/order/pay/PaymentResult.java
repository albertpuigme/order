package net.apuig.order.pay;

public record PaymentResult(String transactionId, String error)
{
    public static PaymentResult ok(String transactionId)
    {
        return new PaymentResult(transactionId, null);
    }

    public static PaymentResult error(String error)
    {
        return new PaymentResult(null, error);
    }

    public boolean isError()
    {
        return error != null;
    }
}
