import { Plugins } from '@capacitor/core';
import { AppleGooglePayPlugin } from './definitions';
const { AppleGooglePayPlugin } = Plugins;
export class AppleGooglePay implements AppleGooglePayPlugin {
    presentPaymentPopup(): Promise<any> {
        return AppleGooglePayPlugin.presentPaymentPopup();
    }
}
