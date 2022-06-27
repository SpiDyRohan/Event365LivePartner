package com.ebabu.event365live.host.stripe;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.stripe.android.PaymentAuthConfig;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;

public class StripeConnect {
    public static Stripe paymentAuth(Context context) {
        final PaymentAuthConfig.Stripe3ds2UiCustomization uiCustomization =
                new PaymentAuthConfig.Stripe3ds2UiCustomization.Builder()
                        .build();
        PaymentAuthConfig.init(new PaymentAuthConfig.Builder()
                .set3ds2Config(new PaymentAuthConfig.Stripe3ds2Config.Builder()
                        .setTimeout(5)
                        .setUiCustomization(uiCustomization)
                        .build())

                .build());
        return new Stripe(context, PaymentConfiguration.getInstance(context).getPublishableKey());
    }

    public static void confirmPayment(Context context,Stripe mStripe,@NonNull ConfirmPaymentIntentParams params) {

        mStripe.confirmPayment((Activity) context, params);
    }
}
