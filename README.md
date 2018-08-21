# Brahma Wallet

Brahma Wallet is a decentralized, secure and light wallet for brahma os, and support android.

## Features

- **Wallet account management**.
- Token assets(Now only support Ethereum ERC20 token) management.
- **Transaction management**.
- **FEX(Flash Exchange)**, integrate Kyber, support DEX.
- **Contact management**: you can set an wallet account for your contacts, when you select a contact at the time of transfer, the recipient of transfer is automatically set, this reduces the risk of transfer errors.

## Building

To build everything from source, simply checkout the source and build using gradle on the build system you need.

 - JDK 1.8
 
Then you need to use the Android SDK manager to install the following components:

- *ANDROID_HOME* environment variable pointing to the directory where the SDK is installed
- Android SDK Tools 26.1.1
- SDK Platform Tools 23.0.2
- Android SDK build Tools 27.0.3
- Android 6.0 (API 23) (at least SDK Platform)
- Android Extras:
  - Android Support Repository rev 5
  - Android Support Library rev 25.1.1

The project layout is designed to be used with a recent version of Android Studio (currently 3.1)

**Build commands**

On the console type:

```
git clone https://github.com/BrahmaOS/wallet.git
cd wallet
```

Linux/Mac type:

```
./gradle build
```

Windows type:

```
gradlew.bat build
```


## Bugs and feature requests

Have a bug or a feature request? [Please open a new issue](https://github.com/BrahmaOS/wallet/issues).
