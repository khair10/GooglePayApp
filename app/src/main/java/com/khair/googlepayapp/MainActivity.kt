package com.khair.googlepayapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import android.widget.ImageView
import android.widget.TextView
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), Presenter.View {

    private val LOAD_PAYMENT_DATA_REQUEST_CODE = 991
    private val mBikeItem = ItemInfo("Simple Bike", 30, R.drawable.ic_motorcycle_black_216dp)

    private lateinit var paymentsClient: PaymentsClient
    private lateinit var presenter: Presenter
    private lateinit var mGooglePayStatusText: TextView
    private lateinit var mGooglePayButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        presenter = Presenter(this)
        initItemUI()
        mGooglePayButton = findViewById(R.id.googlepay_button)
        mGooglePayStatusText = findViewById(R.id.googlepay_status)

        paymentsClient = GooglePayHelper.createPaymentsClient(this)
        GooglePayHelper.isReadyToPay(paymentsClient, this,
            OnCompleteListener { task ->
                try {
                    if (task.isSuccessful) {
                        presenter.setGooglePayAvailable(task.result ?: false)
                    } else {
                        Log.w("isReadyToPay failed", task.exception)
                    }
                } catch (e: ApiException) {
                    handleError(e.message.toString())
                }
            })

        mGooglePayButton.setOnClickListener {
            presenter.startPaymentProcess()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            LOAD_PAYMENT_DATA_REQUEST_CODE -> {
                presenter.endPaymentProcess()
                when (resultCode) {
                    Activity.RESULT_OK ->
                        data?.let {
                            val paymentData = PaymentData.getFromIntent(it)
                            handlePaymentSuccess(paymentData!!)
                        }
                    Activity.RESULT_CANCELED -> presenter
                    AutoResolveHelper.RESULT_ERROR -> {
                        data?.let {
                            val status = AutoResolveHelper.getStatusFromIntent(data)
                            handleError("" + status!!.statusCode)
                        }
                    }
                    else -> {
                    }
                }
                mGooglePayButton.isClickable = true
            }
        }
    }

    override fun showGooglePayButton() {
        mGooglePayStatusText.visibility = View.GONE;
        mGooglePayButton.visibility = View.VISIBLE;
    }

    override fun hideGooglePayButton() {
        mGooglePayStatusText.text = "Google Play Unavailable"
    }

    override fun showToken(tokenId: String) {
        Log.d("TOKEN", tokenId)
    }

    override fun requestPayment() {
        mGooglePayButton.isClickable = false
        val price = GooglePayHelper.rublesToString(mBikeItem.priceRubles)
        val requestJson = GooglePayHelper.getPaymentDataRequest(price)
        if (requestJson.length() == 0) {
            return
        }
        val request = PaymentDataRequest.fromJson(requestJson.toString())
        if (request != null) {
            AutoResolveHelper.resolveTask(
                paymentsClient.loadPaymentData(request), this, LOAD_PAYMENT_DATA_REQUEST_CODE
            )
        }
    }

    private fun handleError(statusCode: String) {
        Log.w("loadPaymentData failed", String.format("Error code ", statusCode))
    }

    override fun showLoading() {
        progressBar1.visibility = View.VISIBLE
    }

    override fun hideLoading() {
        progressBar1.visibility = View.GONE
    }

    private fun handlePaymentSuccess(paymentData: PaymentData) {
        val paymentInformation = paymentData.toJson() ?: return
        val paymentMethodData: JSONObject
        try {
            paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
            presenter.onGooglePayTokenParsed(paymentMethodData.getJSONObject("tokenizationData").getString("token"))
        } catch (e: JSONException) {
            Log.e("handlePaymentSuccess", "Error: $e")
            return
        }
    }

    private fun initItemUI() {
        val itemName = findViewById<TextView>(R.id.text_item_name)
        val itemImage = findViewById<ImageView>(R.id.image_item_image)
        val itemPrice = findViewById<TextView>(R.id.text_item_price)
        itemName.text = mBikeItem.name
        itemImage.setImageResource(mBikeItem.imageResourceId)
        itemPrice.text = GooglePayHelper.rublesToString(mBikeItem.priceRubles)
    }
}
