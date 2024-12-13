package net.apuig;

import jakarta.annotation.Nullable;

public class LikeUtil
{
    /** if the user use a wildcard respect it, otherwise assume it want a 'contains' */
    @Nullable
    public static String like(@Nullable String like)
    {
        if (like != null)
        {
            if (like.contains("*"))
            {
                return like.replaceAll("\\*", "%").toUpperCase();
            }
            return '%' + like.toUpperCase() + '%';
        }
        return like;
    }
}
