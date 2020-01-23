package com.khair.googlepayapp

class Presenter(val view: View) {

    fun setGooglePayAvailable(result: Boolean) {
        if(result)
            view.showGooglePayButton()
        else
            view.hideGooglePayButton()
    }

    fun onGooglePayTokenParsed(id: String){
        view.showToken(id)
    }

    fun startPaymentProcess() {
        view.showLoading()
        view.requestPayment()
    }

    fun endPaymentProcess() {
        view.hideLoading()
    }

    interface View{
        fun showGooglePayButton()
        fun hideGooglePayButton()
        fun showToken(tokenId: String)
        fun showLoading()
        fun requestPayment()
        fun hideLoading()
    }
}