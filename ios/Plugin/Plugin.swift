import Foundation
import Capacitor
import PassKit
import StoreKit
import Stripe

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(AppleGooglePay)
public class AppleGooglePay: CAPPlugin, STPApplePayContextDelegate {

    public var amount = ""
    public var currency = "USD"
    public var planType = "monthly"
    public var publicKey = ""
    public var merchantID = ""
    public var backendDomain = ""
    public var paymentMethodID = ""

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.success([
            "value": value
        ])
    }

    public func applePayContext(_ context: STPApplePayContext, didCreatePaymentMethod paymentMethod: STPPaymentMethod, paymentInformation: PKPayment, completion: @escaping STPIntentClientSecretCompletionBlock) {
        
        print(paymentInformation)
        print(context)

        var floatAmount = (self.amount as NSString).doubleValue
        var stripeAmount = Int(floatAmount * 100)
        var stripeAmountString = String(stripeAmount)
        
        // Monthly: only create a setup intent
        var params = ["amount": stripeAmountString, "currency": self.currency] as Dictionary<String, String>
        var request = URLRequest(url: URL(string: self.backendDomain + "/stripe/paymentintent")!)


        // Yearly: create a payment intent
        if(self.planType == "yearly") {
            request = URLRequest(url: URL(string: self.backendDomain + "/stripe/chargepaymentintent")!)
            params = ["amount": stripeAmountString, "currency": self.currency] as Dictionary<String, String>
        }

        request.httpMethod = "POST"
        request.httpBody = try? JSONSerialization.data(withJSONObject: params, options: [])
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")

        let session = URLSession.shared
        let task = session.dataTask(with: request, completionHandler: { data, response, error -> Void in
            do {
                let json = try JSONSerialization.jsonObject(with: data!)
                let response = json as AnyObject?
                let paymentIntent = response?.object(forKey: "paymentIntent") as AnyObject?
                print("PAYMENT INTENT")
                print(paymentIntent)
                let clientSecretData = paymentIntent?.object(forKey: "client_secret") as? String ?? ""
                let paymentMethodData = paymentIntent?.object(forKey: "payment_method") as? String ?? ""
                
                    let clientSecret: String = clientSecretData
                self.paymentMethodID = paymentMethodData
                    completion(clientSecret, error)
                
                



            } catch {
                print(error)
            }
        })

        task.resume()
    }

    public func applePayContext(_ context: STPApplePayContext, didCompleteWith status: STPPaymentStatus, error: Error?) {
        switch status {
        case .success:
            print("payment_success")
            self.bridge.triggerWindowJSEvent(eventName: "rhm_capacitor_stripe", data: "{'status': 'payment_success', 'paymentMethodID': '" + self.paymentMethodID + "'}")
            break
        case .error:
            print("payment_failed")
            self.bridge.triggerWindowJSEvent(eventName: "rhm_capacitor_stripe", data: "{'status': 'payment_failed'}")
            break
        case .userCancellation:
            print("user_cancelled")
            self.bridge.triggerWindowJSEvent(eventName: "rhm_capacitor_stripe", data: "{'status': 'user_cancelled'}")
            break
        @unknown default:
            fatalError()
        }
    }

    func canMakePayment() -> Bool {

        // check if this device can make a payment
        return PKPaymentAuthorizationController.canMakePayments()

    }

    @objc func presentPaymentPopup(_ call: CAPPluginCall) {

        self.amount = (call.getString("amount") ?? "") as String
        self.currency = (call.getString("currency") ?? "USD") as String
        self.planType = (call.getString("plan") ?? "monthly") as String
        self.publicKey = (call.getString("publicKey") ?? "") as String
        self.merchantID = (call.getString("merchantID") ?? "") as String
        self.backendDomain = (call.getString("backendDomain") ?? "") as String


        Stripe.setDefaultPublishableKey(self.publicKey)
        let merchantIdentifier = self.merchantID
        let request = Stripe.paymentRequest(withMerchantIdentifier: merchantIdentifier, country: "US", currency: currency)
        request.supportedNetworks = [PKPaymentNetwork.visa, PKPaymentNetwork.masterCard, PKPaymentNetwork.amex]
        request.merchantCapabilities = PKMerchantCapability.capability3DS
        request.paymentSummaryItems = [
            PKPaymentSummaryItem(label: planType, amount: NSDecimalNumber(string: self.amount))
               ]

        if let applePayContext = STPApplePayContext(paymentRequest: request, delegate: self) {
            // Present Apple Pay payment sheet
            DispatchQueue.main.async {
                applePayContext.presentApplePay(on: self.bridge.viewController)
            }
        } else {
            // There is a problem with your Apple Pay configuration
        }
    }
}
