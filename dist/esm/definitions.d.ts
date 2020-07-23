declare module '@capacitor/core' {
    interface PluginRegistry {
        AppleGooglePay?: AppleGooglePayPlugin;
    }
}
export interface AppleGooglePayPlugin {
    presentPaymentPopup(): Promise<any>;
}
