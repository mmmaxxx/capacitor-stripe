import { WebPlugin } from '@capacitor/core';
import { AppleGooglePayPlugin } from './definitions';

export class AppleGooglePayWeb extends WebPlugin implements AppleGooglePayPlugin {
  constructor() {
    super({
      name: 'AppleGooglePay',
      platforms: ['web'],
    });
  }

  async presentPaymentPopup(): Promise<any> {
    return true;
  }
}

const AppleGooglePayWebImplementation = new AppleGooglePayWeb();

export { AppleGooglePayWebImplementation };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(AppleGooglePayWebImplementation);
