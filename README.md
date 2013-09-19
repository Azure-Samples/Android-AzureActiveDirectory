# Android - Azure Active Directory Demo
This is a sample which demonstrates how you an authenticate with Windows Azure Active Directory (WAAD) from an Android application.  You will then use the access token retrieved from WAAD to gain access to a web service that has been secured with WAAD.  This application was built with a minimum SDK version of 15 and a target version of 18 but newer or older versions of Android should work without issue.

Below you will find requirements and deployment instructions.

## Requirements
* Eclipse - This sample was built with Eclipse Kepler.
* Android SDK - You can download this from the [Android Developer portal](http://developer.android.com/sdk/index.html).
* Windows Azure Account - Needed to create the Windows Azure Active Directory Account as well as to deploy the web service to Web Sites (if you choose to do so).  [Sign up for a free trial](https://www.windowsazure.com/en-us/pricing/free-trial/).

## Source Code Folders
* /source/ - This contains code for the iOS Client application.

## Additional Resources
I've released a [blog post](http://chrisrisner.com/Accessing-Resources-Secured-By-Azure-Active-Directory-with-iOS-and-Android) which walks through this sample as well as how to setup the web service and WAAD account.  It's highly recommended to read this post before / while looking at the source code.

#Setting up your Windows Azure Active Directory account and Web Service
To set up your WAAD account and create a web service you can access with the clients, please read this post on [Securing a Windows Store Application and REST Web Service using Windows Azure AD](http://msdn.microsoft.com/en-us/library/windowsazure/dn169448.aspx).  

To make things easier, you can also download the source code from the above post [here](http://code.msdn.microsoft.com/AAL-Windows-Store-app-to-2430e331).  Be sure you change your **domainName** and **audience** variables in the **global.asax** file for the web service.

#Client Application Changes
The client application is currently set to run against an existing WAAD account and site I have set up.  At the time of writing, these services should be working and you should be able to authenticate using the following credentials:

1.  Username: testuser@christesttwo.onmicrosoft.com
1.  Password: ThisIs@Test

You can also follow instructions in [this post](http://chrisrisner.com/Accessing-Resources-Secured-By-Azure-Active-Directory-with-iOS-and-Android) regarding setting up your own service and AD account.

## Contact

For additional questions or feedback, please contact the [team](mailto:chrisner@microsoft.com).
