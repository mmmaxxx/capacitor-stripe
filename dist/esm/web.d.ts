import { WebPlugin } from '@capacitor/core';
import { AppleGooglePayPlugin } from './definitions';
export declare class AppleGooglePayWeb extends WebPlugin implements AppleGooglePayPlugin {
    constructor();
    presentPaymentPopup(): Promise<any>;
}
declare const AppleGooglePayWebImplementation: AppleGooglePayWeb;
export { AppleGooglePayWebImplementation };
