package net.apuig.order;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** see README.md for transitions diagram */
public enum OrderStatusType
{
    /** accept products */
    OPEN(true, true, true, true, false, false),  //
    /** discard the order */
    CANCELLED(false, false, true, false, false, false),  //
    /** the user don't want more products */
    PENDING_PAYMENT(true, false, true, false, true, false),  //
    /** the payment is being processed in the gateway */
    PAYMENT_IN_PROGRESS(true, false, false, false, false, false),  //
    /** the user paid the order */
    PAID(false, false, false, false, false, true),  //
    /** the payment cannot be processed */
    PAYMENT_FAILED(false, false, false, false, true, false),  //
    /** the order was delivered */
    DELIVERED(false, false, false, false, false, false)  //
    ;

    public final boolean allowModifySeat;

    public final boolean allowModifyProducts;

    public final boolean allowCancel;

    public final boolean allowFinish;

    public final boolean allowPay;

    public final boolean allowDelivery;

    private OrderStatusType(boolean allowModifySeat, boolean allowModifyProducts,
        boolean allowCancel, boolean allowFinish, boolean allowPay, boolean allowDelivery)
    {
        this.allowModifySeat = allowModifySeat;
        this.allowModifyProducts = allowModifyProducts;
        this.allowCancel = allowCancel;
        this.allowFinish = allowFinish;
        this.allowPay = allowPay;
        this.allowDelivery = allowDelivery;
    }

    public static final Set<OrderStatusType> ALL =
        new HashSet<>(Arrays.asList(OrderStatusType.values()));
}
