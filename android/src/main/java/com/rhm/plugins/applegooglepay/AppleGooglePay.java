package com.rhm.plugins.applegooglepay;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.stripe.android.GooglePayConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

@NativePlugin
public class AppleGooglePay extends Plugin {

    private PaymentsClient paymentsClient;

    @Override
    public void load() {
        super.load();

        paymentsClient = Wallet.getPaymentsClient(this.bridge.getActivity(),
                new Wallet.WalletOptions.Builder().setEnvironment(WalletConstants.ENVIRONMENT_TEST
                ).build());
    }

    private void isReadyToPay() throws JSONException {
            final IsReadyToPayRequest request = createIsReadyToPayRequest();
            paymentsClient.isReadyToPay(request)
                    .addOnCompleteListener(
                            new OnCompleteListener<Boolean>() {
                                public void onComplete(Task<Boolean> task) {
                                    if (task.isSuccessful()) {
                                        // show Google Pay as payment option
                                        System.out.println("can use");
                                    } else {
                                        // hide Google Pay as payment option
                                        System.out.println("CANNOT use");
                                    }

                                }
                            }
                    );
        }

    @NonNull
        private IsReadyToPayRequest createIsReadyToPayRequest() throws JSONException {
            final JSONArray allowedAuthMethods = new JSONArray();
            allowedAuthMethods.put("PAN_ONLY");
            allowedAuthMethods.put("CRYPTOGRAM_3DS");

            final JSONArray allowedCardNetworks = new JSONArray();
            allowedCardNetworks.put("AMEX");
            allowedCardNetworks.put("MASTERCARD");
            allowedCardNetworks.put("VISA");

            final JSONObject isReadyToPayRequestJson = new JSONObject();
            isReadyToPayRequestJson.put("allowedAuthMethods", allowedAuthMethods);
            isReadyToPayRequestJson.put("allowedCardNetworks", allowedCardNetworks);

            return IsReadyToPayRequest.fromJson(isReadyToPayRequestJson.toString());
        }

        @NonNull
            private PaymentDataRequest createPaymentDataRequest(String publicKey, String amount, String currency, String plan) throws JSONException {
                final JSONObject tokenizationSpec =
                        new GooglePayConfig(publicKey).getTokenizationSpecification();
                final JSONObject cardPaymentMethod = new JSONObject()
                        .put("type", "CARD")
                        .put(
                                "parameters",
                                new JSONObject()
                                        .put("allowedAuthMethods", new JSONArray()
                                                .put("PAN_ONLY")
                                                .put("CRYPTOGRAM_3DS"))
                                        .put("allowedCardNetworks",
                                                new JSONArray()
                                                        .put("AMEX")
                                                        .put("MASTERCARD")
                                                        .put("VISA"))

                                        // require billing address
                                        .put("billingAddressRequired", true)
                                        .put(
                                                "billingAddressParameters",
                                                new JSONObject()
                                                        // require full billing address
                                                        .put("format", "MIN")

                                                        // require phone number
                                                        .put("phoneNumberRequired", true)
                                        )
                        )
                        .put("tokenizationSpecification", tokenizationSpec);

                final JSONObject transactionInfoData = new JSONObject()
                        .put("totalPrice", amount)
                        .put("totalPriceStatus", "FINAL")
                        .put("currencyCode", currency);

                // create PaymentDataRequest
                final JSONObject paymentDataRequest = new JSONObject()
                        .put("apiVersion", 2)
                        .put("apiVersionMinor", 0)
                        .put("allowedPaymentMethods",
                                new JSONArray().put(cardPaymentMethod))
                        .put("transactionInfo", transactionInfoData)
                        .put("merchantInfo", new JSONObject()
                                .put("merchantName", "TV5MONDE APAC " + plan))
                        // require email address
                        .put("emailRequired", true);

                return PaymentDataRequest.fromJson(paymentDataRequest.toString());
            }

            @PluginMethod()
                public void presentPaymentPopup(PluginCall call) throws JSONException {
                    final int LOAD_PAYMENT_DATA_REQUEST_CODE = 53;
                    String amount = call.getString("amount");
                    String currency = call.getString("currency");
                    String planType = call.getString("plan");
                    String publickey = call.getString("publicKey");

                    AutoResolveHelper.resolveTask(
                            paymentsClient.loadPaymentData(createPaymentDataRequest(publickey, amount, currency, planType)),
                            this.getActivity(),
                            LOAD_PAYMENT_DATA_REQUEST_CODE
                    );


                }
}
