package com.khair.googlepayapp

import android.app.Activity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.wallet.*
import com.stripe.android.GooglePayConfig
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object GooglePayHelper {

    private const val MERCHANT_NAME = "example"
    private const val CURRENCY_CODE_RUS = "RUB"

    private val baseRequest: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject()
            .put("apiVersion", 2)
            .put("apiVersionMinor", 0)

    private val merchantInfo: JSONObject
        @Throws(JSONException::class)
        get() = JSONObject()
            .put("merchantName", MERCHANT_NAME)

    private val environment = WalletConstants.ENVIRONMENT_TEST

    private val createTokenizationParameters: JSONObject =
        GooglePayConfig(StripeInteractor.stripePublishableKeyKey).tokenizationSpecification

    private val allowedCardNetworks = JSONArray()
        .put("AMEX")
        .put("DISCOVER")
        .put("INTERAC")
        .put("JCB")
        .put("MASTERCARD")
        .put("VISA")

    private val allowedCardAuthMethods: JSONArray = JSONArray()
        .put("PAN_ONLY")
        .put("CRYPTOGRAM_3DS")

    @Throws(JSONException::class)
    private fun getBaseCardPaymentMethod(): JSONObject{
        val parameters = JSONObject()
            .put("allowedAuthMethods", allowedCardAuthMethods)
            .put("allowedCardNetworks", allowedCardNetworks)
        val baseCardPaymentMethod = JSONObject()
            .put("type", "CARD")
            .put("parameters", parameters)
        return baseCardPaymentMethod
    }

    @Throws(JSONException::class)
    private fun getCardPaymentMethod(): JSONObject {
        val cardPaymentMethod = getBaseCardPaymentMethod()
            .put("tokenizationSpecification", createTokenizationParameters)
        return cardPaymentMethod
    }

    fun createPaymentsClient(activity: Activity): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(environment)
            .build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    @Throws(JSONException::class)
    fun isReadyToPay(paymentsClient: PaymentsClient, activity: Activity, listener: OnCompleteListener<Boolean>) {
        val request = try {
            baseRequest
                .put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
        } catch (e: JSONException){
            JSONObject()
        }
        if (request.length() == 0)
            return
        val requestString = IsReadyToPayRequest.fromJson(request.toString()) ?: return
        val task = paymentsClient.isReadyToPay(requestString)
        task.addOnCompleteListener(activity, listener)
    }

    @Throws(JSONException::class)
    private fun getTransactionInfo(price: String) = JSONObject()
        .put("totalPrice", price)
        .put("totalPriceStatus", "FINAL")
        .put("currencyCode", CURRENCY_CODE_RUS)

    @Throws(JSONException::class)
    fun getPaymentDataRequest(price: String): JSONObject {
        try {
            val paymentDataRequest = baseRequest
                .put("allowedPaymentMethods", JSONArray().put(getCardPaymentMethod()))
                .put("transactionInfo", getTransactionInfo(price))
                .put("merchantInfo", merchantInfo)
            return paymentDataRequest
        } catch (e: JSONException) {
            return JSONObject()
        }
    }

    fun rublesToString(priceRubles: Int): String {
        return priceRubles.toString()
    }
}