**Android SDK Integration with KVS**

List of Contents

- [Android SDK](#android-sdk)
  - [Incorporating the SDK](#incorporating-the-sdk)
  - [Using JenID Activity](#using-jenid-activity)
  - [Using own Activity by extending JenID's component](#using-own-activity-by-extending-jenids-component)
    - [How it works](#how-it-works)
  - [Receiving data from SDK](#receiving-data-from-sdk)
    - [Approach One (Using GenuineIDActivity)](#approach-one-using-genuineidactivity)
    - [Approach Two (Customizing and Extending SDK)](#approach-two-customizing-and-extending-sdk)
- [Android Capture SDK (Integration Details)](#android-capture-sdk-integration-details)
  - [System Requirements](#system-requirements)
  - [Access Rights](#access-rights)
  - [Example Project](#example-project)
  - [GenuineIDActivity](#genuineidactivity)
  - [Integration](#integration)
  - [Using the class](#using-the-class)
    - [Configuration](#configuration)
    - [Initializing the class](#initializing-the-class)
    - [Code Example](#code-example)
- [UI Customization](#ui-customization)

---

# Android SDK

JenID has provided a SDK to facilitate taking document picture and performing facial recognition in Android, it also contains some useful utilities as well.

## Incorporating the SDK

In order to incorporate the SDK App developer needs to import 2 main libraries (like below example)

Put these lines in `build.gradle` (App) (next sections contain more detailed information)

```c
api(name: 'GenuineID_Mobile_Core-3_0_1_19128', ext: 'aar')
api(name: 'Genuine-ID_Mobile_Capture_3_0_1_19128', ext: 'aar')
```

## Using JenID Activity

By using this approach App developer easy just extends `GenuineIDActivity` and only receives necessary data after performing a document scan (with/without facial recognition).

> For detailed information about this approach check out JenId documentation (see [Appendixes](#genuine-id-android-capture-sdk)).

## Using own Activity by extending JenID's component

This approach needs more coding and carefully handling different scenarios of scanning a document and carrying out facial recognition.

### How it works

Basically, the JenID Android SDK designed to do its job by using *Android Fragment*s, In fact by extending few underlaying _Fragments_ App developer can setup a full wizard steps for document scanning.

**Example**
To understanding how SDK works checkout the `CaptureFragment` implementation.
This class (_Fragment_) extends `GenuineIDFragment` which extends `GenuineIDBaseCamera2Fragment` and implements `GenuineIDCallback` and `GenuineIDSharpnessCallback`

_Check out following code from JenID SDK's implementation._

```java
public class CaptureFragment extends GenuineIDFragment {
  //...
}
```

```java
public abstract class GenuineIDFragment extends GenuineIDBaseCamera2Fragment implements GenuineIDCallback, GenuineIDSharpnessCallback {
  //...
  takePhoto();

}
```

App developer can ignore using `CaptureFragment` and develop his own _Fragment_ like `GenuineIDFragment` and implement all required actions (starting camera, taking picture, etc..) and then use `DocFinderRunnable` or `DocFinder` classes in order to possibly find present document in the image.

> It is worthwhile to checkout implementation of mentioned _Fragments_ available from the JenID SDK.

## Receiving data from SDK

No matter what approach would be chosen to use JenID SDK, Ultimately SDK provides App developer with a `JSON` object which called payload and few other data in different types (String, Bitmap) that corespond to the images of scanned document and facial recognition steps.

### Approach One (Using GenuineIDActivity)

Please refer to JenID SDK documentation for complete and detailed information.

### Approach Two (Customizing and Extending SDK)

Its highly recommended to review `GenuineIDActivity` implementation code to understand how the SDK works and payload gets constructed, but briefly, After implementing the _Fragments_ and wiring them up correctly they will return pieces of data objects corresponding to images of scanned document and liveness validation steps which can be converted to **Base64String** by some utility methods provided by SDK.

# Android Capture SDK (Integration Details)

The Genuine-ID Capture SDK provides functionality to integrate ID document and facial capture functionality
into your App to be verified by Keesing Technologies.
The API is easy and simple to understand to limit the effort for integration and encapsulates all the complicated
image processing functions for you. As mentioned before you simply need to integrate and configure only one _Activity_: GenuineIDActivity.

## System Requirements

The SDK is available and does support ARM-v7 and ARM64-v8a platforms. The required minimum API level is 23.
A minimum of 2GB RAM is required and a quad core with at least 1.5 GHz is preferable.

## Access Rights

The SDK needs access to cameras and memory. The user has to grant these rights otherwise your App will not work as expected.

## Example Project

The SDK does include a working example project which you can use to start with your integration.
To start the project please open the project under the "GenuineID_Mobile_ExampleProject" folder in Android Studio.
You can build and execute the project on your device.

## GenuineIDActivity

This _Activity_ leads the end-user through the process of detecting id-documents, detecting
ISO conform faces and proceeding a liveness check. As a result there will be cropped images of
the id-documents pages, as well as valid _JSON_ data to be send to the _JenID Solutions GmbH server_
for further processing, extracting of data and verification.

The steps to be taken by the end-user are:

1. selection of the document type, from _TD3_ (Passports) and _TD1_ (national id-cards & driver licenses)
2. capturing one or two pages of a document
3. detecting an ISO conform image of the end user
4. performing a liveness check

Note: the selection of the document type is critical for correctly detecting the right document
proportions in an image. It does make a difference if we search a _TD1_ or _TD3_ document.

The capturing process consists of two sub steps. First the user takes an image, whether
automatically or manually (after the automatic process took too long). Second the user reviews
the found document and can correct it if needed. The better the cropping result, the
better the verification of the document will perform.

Also note, that the last two steps are optional. It is possible to configure the _Activity_ to
not perform the face detection. A liveness check will only be performed if an ISO conform image
was detected.

## Integration

In order to use the functionality of the Genuine-ID Capture SDK you will have to copy two
.aar-files to your app project. These are:

1. GenuineID_Mobile_Core-[...].aar
2. GenuineID_Mobile_Capture-[...].aar

Simply place the files to the location [YourProject]/app/libs and go to your _build.gradle_ to
configure your project to find the SDK. In the dependencies section add the following:

```c
    dependencies {
        ...
        compile(name:'GenuineID_Mobile_Core-[...]', ext:'aar')
        compile(name:'GenuineID_Mobile_Capture-[...]', ext:'aar')
        ...
    }
```

Please note, that you have to change the [...] to the actual version of the SDK.

## Using the class

The _GenuineIDActivity_ is an abstract class, that extends an _Activity_.
When implemented by an App, the user is guided through the whole process by a couple of
specialized _Fragments_. These _Fragments_ use partly either the front (face detection) or back
(document detection) camera. They deal with all the camera related topics: displaying the camera
stream onto a _SurfaceView_, triggering auto focus when possible or let the user set focus
manually by touching the desired area. Additionally, the user interface guides the user
throughout the entire process when needed and displays important information on the screen.

There are two possible callbacks to be used by the integrating developer.

1. _doAfterDocumentFound_
2. _doAfterFail_

The first callback will be called, when a document could be successfully detected and - if
configured to do so - an ISO conform face image could be made and verified by a liveness check.

The second callback will be called when the process of detecting an ISO conform face and/or the
performing of the liveness check failed. This will happen for example when the end user does not
keep his face in the screen, or it is not possible to get an ISO conform image of his face. The
end user can try three times. After that, the _doAfterFail_ callback will be called.

Since both callbacks are abstract methods, they have to be implemented by the integrating
developer of this GenuineID Mobile SDK and serve as entry points for further actions, like
presenting the found document and the face image to the end user and starting new Activities
and so on.

In either way the instance will provide you the images in different formats
and the compiled JSON formatted payload to be send to the verification server.

The call backs will have the following parameters:

- Bitmap frontImage -- the image of first captured side
- String encodedFrontImage -- base64 encoded format of the image
- Bitmap backImage -- the image of the second captured side of a double-sided document; empty in case of a passport document,
- String encodedBackImage -- base64 encoded format of the image
- Bitmap faceImage -- facial image in case face capture is enabled or empty if face capture is disabled(see configuration)
- String encodedFaceImage -- base64 encoded format of the image
- String completeJsonPayload -- JSON formatted payload as input for to send to the verification server.

The resulting images of the pages of the document come in both, a _Bitmap_
and a Base64 _String_ representation. The former could be used for your individual processing
or showing to the end user. The Base64 String is optimized for uploading to the _JenID Solutions
CLOUD_ in order to process the document.

**Note:** The images are already compressed please DO NOT further compress the images
since this will have negative impact on the performance of the Genuine-ID verification engine.

The payload must be used as is. **Do not change** the payload as it might result in errors
and will be rejected by the server.

**Important**:
The process of finding documents in a camera frame uses sophisticated techniques consisting of
machine learning algorithms. The nature of them is, that they need proper initialization.
This process can about 1 to 3 seconds
depending on the hardware being used. This initialization process must be called only once,
To avoid the delay, do the initialization right after for example the splash screen, and not while loading a controller
using this framework.

### Configuration

There are only two possible configuration parameters for an instance of the _GenuineIDActivity_.  
_setTakePhotoTimeOut_ can be used to override the default time setting before the Activity starts
the manual capture in case the automatic capture routine will not find a document.
The default setting is 15000ms.

_setEnableFaceDetection_ can be used to enable or disable the face capture.
If set to _false_ the Activity will only capture the document images. The default setting is _true_.

### Initializing the class

Initialize the framework as follows:

    DocFinder docFinder = new DocFinder(SplashActivity.this);
    docFinder.init();

### Code Example

The following code snippet demonstrate how to use the _GenuineIDActivity_ in your App:

```java
    public class CaptureActivity extends GenuineIDActivity
    {
        ...

        @Override
        public void doAfterDocumentFound(
            Bitmap frontImage,
            String encodedFrontImage,
            Bitmap backImage,
            String encodedBackImage,
            Bitmap faceImage,
            String encodedFaceImage,
            String completeJsonPayload)
        {
            // you can here perform the upload of the payload to the JenID Solutions GmbH server in another activity...
        }

        @Override
        public void doAfterFail(
            Bitmap frontImage,
            String encodedFrontImage,
            Bitmap backImage,
            String encodedBackImage,
            String completeJsonPayload)
        {
            // you can do some handling here e.g. in case you allow an upload of the transaction without the facial image do it here
			      // or do display some user guidance how to capture a face successfully.....
        }
```

```java
        @Override
        public void renderButton(Button button)
        {
            // for example to set a custom font:
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/YourFont.ttf");
            button.setTypeface(typeFace);
        }

        ...
    }
```

**Note:** Please note, that if the end user captures a _TD3_ document (passport), only one page will be
captured. Therefore, _backImage_ and _encodedBackImage_ will be null. If the face detection
functionality is disabled, _faceImage_ and _encodedFaceImage_ will be null.

Then configure the _GenuineIDActivity_ by doing like follows to for example enable face
detection:

```java
    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        ...

        super.onCreate(savedInstanceState);

        // if automatic detection of documents fails, there is a fallback to capture manually.
        // set a sufficient timeout here:
        super.setTakePhotoTimeOut( 15000 );

        super.setEnableFaceDetection( true );

        ...
    }
```

**Note:** Sometimes it is difficult for the end users to use the capturing process like intended.
Therefore, there is a functionality added, which uses a button to take a photo after a certain
timeout. The photo taken, is processed like the camera stream before and leads to a successfully
detected document, if the photo being taken allows that.

To apply your custom button style to the correction buttons, override the method "renderButton"
and customize background color and so on:

```java
    @Override
    public void renderButton(Button button)
    {
        button.setBackgroundColor(Color.MAGENTA);
        ...
    }
```

When implementing back button functionality, it is crucial not to override androids
_onBackPressed_. Instead please override the method _backPressed_:

```java
    @Override
    public void backPressed()
    {
        Intent i = new Intent(CaptureActivity.this, YourMainActivity.class);
        startActivity(i);
        finish();
    }
```

---

# UI Customization

UI customization is possible via various methods available is the main activity, following list is those methods.

- **setBackgroundColor**: sets the background color of the capturing UI pages.
- **setPrimaryColor**: sets the primary color which is suitable to set the enterprice color code.
- **setFontColor1**: sets one of the two font color of the capturing UI pages.
- **setFontColor2**: same as `setFontColor1`.
- **setTextFont**: sets the primary font of all texts available in the capturing process pages.
- **setSecondaryTextFont**: sets the fonts of pages after main page.
- **setCorrectionColor**: sets the color of the rectangle shown during the manual capturing (when automatic capturing fails).
- **setFaceColor1**: sets the color of an indicator over the face during liveness check.
- **setFaceColor2**: sets the color of an indicator over the face when face detection succeeds.
- **setFaceTrackColor**: sets the color of square around the face during the liveness check.
- **setFaceFeatureColor**: sets the color of the squares around face features (eyes/mouth) during liveness check.
- **setWarnColor**: sets the warning color while process failed during capturing or liveness check process.

Also by overriding three methods in main activity we can apply UI changes in buttons available in the capturing process, please see below code example.

```java
 @Override
    public void renderButton(Button button) {
        super.renderButton(button);
        button.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-Italic.ttf"));
        button.setBackgroundColor(getResources().getColor(R.color.keesingBlue));
    }

    @Override
    public void renderSecondaryButton(Button button){
        super.renderSecondaryButton(button);
        button.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/Ubuntu-Italic.ttf"));
        button.setBackgroundColor(Color.GRAY);
    }

    @Override
    public void renderPhotoButton(Button button){
        super.renderPhotoButton(button);
    }

```
