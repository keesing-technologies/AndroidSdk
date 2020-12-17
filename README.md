# Android Capture SDK
The capture SDK provides functionality to integrate ID document and facial capture functionality
into your App to be later on verified by the Keesing's API.
The SDK is easy and simple to understand to limit the effort for integration and encapsulates all the complicated
image processing functions for you. You simply need to integrate and configure only one *Activity*: GenuineIDActivity.

## System Requirements 
The SDK is available and does support ARM-v7 and ARM64-v8a platforms. The required minimum API level is 23.
A minimum of 2GB RAM is required and a quad core with at least 1.5 GHz is preferable. 

## Access Rights
The SDK needs demands access to cameras and memory. The user has to grant this rights otherwise your App will not work as expected. 

# GenuineIDActivity

This *Activity* leads the end-user through the process of detecting id-documents, detecting
ISO conform faces and proceeding a liveness check. As a result, there will be cropped images of
the id-documents pages, as well as valid *JSON* data to be send to Keesing's API
for further processing, extracting of data and verification.

The steps to be taken by the end-user are:

1. selection of the document type, from *TD3* (Passports) and *TD1* (national id-cards & driver licenses)
2. capturing one or two pages of a document
3. reading NFC chip (in case of passports)
4. detecting an  ISO conform image of the end user
5. performing a liveness check

Note: the selection of the document type is critical for correctly detecting the right document
proportions in an image. It does make a difference if we search a *TD1* or *TD3* document.

The capturing process consists of two sub steps. First, the user takes an image whether
automatically or manually (after the automatic process took too long). Second, the user reviews
the document found.

Also note, that the last three steps are optional. It is possible to configure the *Activity* to
not perform NFC reading or the face detection. A liveness check will only be performed if an 
ISO conform image was detected.

## API

### Integration

In order to use the functionality of the capture SDK you will have to copy four
.aar-files to your app project. These are:

1. GenuineID_Mobile_Core-[...].aar
2. GenuineID_Mobile_MRZ-[...].aar
3. GenuineID_Mobile_Capture-[...].aar
4. tess-two-release.aar

Simply place the files to the location [YourProject]/app/libs and go to your *build.gradle* to 
configure your project to find the SDK. In the dependencies section add the following:

    dependencies {
    	implementation fileTree(dir: 'libs', include: ['*.aar'])


Please note, that you have to change the [...] to the actual version of the SDK.

Additionally, you have to include the following in the dependencies section:

	dependencies {
				
		...
				
		implementation 'com.madgag.spongycastle:prov:1.58.0.0'
    	implementation 'net.sf.scuba:scuba-sc-android:0.0.9'
   		implementation 'org.bouncycastle:bcpkix-jdk15on:1.51'
		implementation 'pl.droidsonroids.gif:android-gif-drawable:1.2.0'
		implementation 'com.gemalto.jp2:jp2-android:1.0'
    

### Using the class

The *GenuineIDActivity* is an abstract class, that extends an *Activity*.
When implemented by an App, the user is guided through the whole process by a couple of
specialized *Fragments*. These *Fragments* use partly either the front (face detection) or back
(document detection) camera. They deal with all the camera related topics: displaying the camera
stream onto a *SurfaceView*, triggering auto focus when possible or let the user set focus
manually by touching the desired area. Additionally, the user interface guides the user
throughout the entire process when needed and displays important information on the screen.

There are two possible callbacks to be used by the integrating developer.

1. *doAfterDocumentFound*
2. *doAfterFail*

The first callback will be called, when a document could be successfully detected and - if
configured to do so - an ISO conform face image could be made and verified by a liveness check.

The second callback will be called when the process of detecting an ISO conform face and/or the
performing of the liveness check failed. This will happen for example when the end user does not
keep his face in the screen, or it is not possible to get an ISO conform image of his face. The
end user can try three times. After that, the *doAfterFail* callback will be called.

Since both callbacks are abstract methods, they have to be implemented by the developer and serve as entry point for further actions, like
presenting the document found and the face image to the end user, starting new Activities and so on.

In either way the instance will provide you the images in different formats 
and the compiled JSON formated payload to be send to the server (e.g. our API).

The call backs will have the following parameters:
            - Bitmap frontImage  -- the image of first captured side    
            - String encodedFrontImage -- base64 encoded format of the image   
            - Bitmap backImage -- the image of the second captured side of a double 
			                      sided document; empty in case of a passport document,
            - String encodedBackImage  -- base64 encoded format of the image
            - Bitmap faceImage   -- facial image in case face capture is enabled 
			                        or empty if face capture is disabled(see configuration) 
            - String encodedFaceImage  -- base64 encoded format of the image
            - String completeJsonPayload  -- JSON formated payload as input for sending to the server
            - String mrz	-- Machine Readable Zone (MRZ) from passports chip
            - String documentNumber	-- Document Number from passport
            - String dateOfBirth	-- date of birth from passport
            - String dateOfExpiry	-- date of expiry from passport
            - Bitmap rfidFaceImage	-- face image from passport's chip
            - boolean verification	-- a boolean whether the datagroups of the chip of the passport are verified or not

			
The resulting images of the pages of the document come in both, a *Bitmap*
and a Base64 *String* representation. The former could be used for your individual processing
or showing to the end user. The Base64 String is optimized for uploading to the *Keesing's API* in order to process the document.

**Note:** The images are already compressed please DO NOT further compress the images 
since this will have negative impact on the performance of the verification engine.

The payload must be used as is. **Do not change** the payload as it might result in errors 
and will be rejected by the server.  


**Important**:

The process of finding documents in a camera frame uses sophisticated techniques consisting of
machine learning algorithms. The nature of them is, that they need proper initialization.
Unfortunately this is a somehow time consuming process, which can take up to 1 - 3 seconds
depending of the hardware being used. This initialization process has to be called only once,
nevertheless it can influence the end users App experience. There is a solution for this: do the
initialization right after for example the splash screen (see "Initializing the class"), and 
not while loading a controller using this framework.


### Configuration
There are only four possible configuration parameters for an instance of the *GenuineIDActivity*.  

*setTakePhotoTimeOut* can be used to override the default time setting before the Activity starts
the manual capture in case the automatic capture routine will not find a document. 
The default setting is 15000ms.  

*setEnableFaceDetection* can be used to enable or disable the face capture. 
If set to *false* the Activity will only capture the document images. The default setting is *true*.

*setEnableNFC* can be used to enable or disable reading a NFC chip from a passport. If set to *false*,
the Activity won't read the chip from a passport.

*setEnableAllowWithoutNFC* can be used to allow or not the user with proceeding without NFC reading.
 

### Initializing the class
Initialize the framework as follows:

    DocFinder docFinder = new DocFinder(SplashActivity.this);
    docFinder.init();

### Code Example 
The following code snippet demonstrate  how to use the *GenuineIDActivity* in your App:

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
            String completeJsonPayload,
            String mrz,
            String documentNumber,
            String dateOfBirth,
            String dateOfExpiry,
            Bitmap rfidFaceImage,
            boolean verification)
        {
            // you can here perform the upload of the payload to your server and later forward it to Keesing's API...
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
 
        ...
 
        @Override
        public void renderButton(Button button)
        {
            // for example to set a custom font:
            Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/YourFont.ttf");
            button.setTypeface(typeFace);
        }
  
        ...
    }

**Note:** Please note, that if the end user captures a *TD3* document (passport), only one page will be
captured. Therefore, *backImage* and *encodedBackImage* will be null. If the face detection
functionality is disabled, *faceImage* and *encodedFaceImage* will be null.

Then configure the *GenuineIDActivity* by doing like follows to for example enable face
detection:

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
 
        ...
 
        super.onCreate(savedInstanceState);
  
        // if automatic detection of documents fails, there is a fallback to capture manually.
        // set a sufficient timeout here:
        super.setTakePhotoTimeOut( 15000 );
 
        super.setEnableFaceDetection( true );
        
        super.setEnableNFC( true );
 
        ...
    }
  
**Note:** Sometimes it is difficult for the end users to use the capturing process like intended.
Therefore, there is a functionality added, which uses a button to take a photo after a certain
timeout. The photo taken, is processed like the camera stream before and leads to a successfully
detected document, if the photo being taken allows that.

To apply your custom button style to the correction buttons, override the method "renderButton"
and customize background color and so on:

    @Override
    public void renderButton(Button button)
    {
        button.setBackgroundColor(Color.MAGENTA);
        ...
    }
 
When implementing back button functionality, it is crucial not to override androids
*onBackPressed*. Instead please override the method *backPressed*:

    @Override
    public void backPressed()
    {
        Intent i = new Intent(CaptureActivity.this, YourMainActivity.class);
        startActivity(i);
        finish();
    }
 
### UI Customization

If you are fine with the default UI, leave everything as it is.

If you want to apply your own graphic style to the SDK's interface, you have the following options.

#### Color Options: 
You can color specific features of the UI of the SDK:

1. Background color ( setBackgroundColor( Color.BLACK ) ) 
2. Primary color (setPrimaryColor( Color.GRAY )) 
3. Font color 1 (setFontColor1( Color.LTGRAY)) 
4. Font color 2 (setFontColor2( Color.WHITE )) 
5. Correction color (setCorrectionColor( Color.LTGRAY )) 
6. Face color 1 (setFaceColor1( Color.LTGRAY )) 
7. Face color 2 (setFaceColor2( Color.WHITE )) 
8. Face track color (setFaceTrackColor( Color.LTGRAY )) 
9. Face feature color (setFaceFeatureColor( Color.WHITE )) 
10. Warn color (setWarnColor( Color.LTGRAY ))

#### Font Options
You can add you .ttf font to the texts of the SDK. Simply add your preferred font into the assets folder of your app and add the relative path with:

1. setTextFont( "..." )
2. setSecondaryTextFont( "..." )

For example, if your font is located in a folder "fonts" in the assets folder, use it like:

     this.setTextFont( "fonts/OCRAStd.ttf" )

#### Button Options

You can customize the buttons used in the SDK by overriding the three methods:

1. renderButton( Button button)
2. renderSecondaryButton( Button button ) 
3. renderPhotoButton( Button button )

For example, you can change the text color or add a background resource to your button.

#### Image Options

There are five different images interchangeable in the Activities' layout. You can now easily change them to fit your own user experience. (We recommend .png images, for they come with the ability to have a transparent background.)

The five different images are:

1. Image of a passport
2. Image of an id card
3. Front image of a passport
4. Front image of an id card
5. Back image of an id card

The corresponding methods are shown in the following code snippet. Note, that the parameter for each method is a resource id to the App's drawables:

	this.setPassportImage( R.drawable.passport );
    this.setIDCardImage( R.drawable.idcard );
    this.setPassportFrontImage( R.drawable.command_front_passport );
    this.setIDCardFrontImage( R.drawable.command_front_idcard );
    this.setIDCardBackImage( R.drawable.command_back_idcard );

#### UI customization sample:

This is a working example.

     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         // add before "super.onCreate(savedInstaceState)" !
         
         this.setPassportImage( R.drawable.passport );
      	 this.setIDCardImage( R.drawable.idcard );

         this.setPassportFrontImage( R.drawable.command_front_passport );
         this.setIDCardFrontImage( R.drawable.command_front_idcard );
         this.setIDCardBackImage( R.drawable.command_back_idcard );

         this.setBackgroundColor( Color.BLACK );
         this.setPrimaryColor( Color.GRAY );
         this.setFontColor1( Color.LTGRAY);
         this.setFontColor2( Color.WHITE );

         this.setTextFont( "fonts/OCRAStd.ttf" );

         this.setCorrectionColor( Color.LTGRAY );
         this.setFaceColor1( Color.LTGRAY );
         this.setFaceColor2( Color.WHITE );
         this.setFaceTrackColor( Color.LTGRAY );
         this.setFaceFeatureColor( Color.WHITE );
         this.setWarnColor( Color.LTGRAY );


         super.onCreate(savedInstanceState);

         ...
     }

     ...

     @Override
     public void renderButton(Button button)
     {
         super.renderButton(button);

         button.setTextColor(Color.BLACK); button.setBackgroundResource(R.drawable.roundcustombutton);
     }

     @Override
     public void renderSecondaryButton(Button button)
     {
         super.renderSecondaryButton(button);

         button.setTextColor(Color.DKGRAY);
         button.setBackgroundResource(R.drawable.roundcustombutton);
     }

     @Override
     public void renderPhotoButton(Button button)
     {
         super.renderPhotoButton(button);

         button.setBackgroundResource(R.drawable.circlebuttonnew);
     }

Please note, that for this example you have to add the font "OCRAStd.ttf" to the assets folder. Also, the drawables "roundcustombutton" and "circlebuttonnew"
should be located in your drawables resource folder in your project.

The background resource roundcustombutton.xml could be as follows:

     <?xml version="1.0" encoding="utf-8"?>
     <shape xmlns:android="http://schemas.android.com/apk/res/android"
         android:shape="rectangle" android:padding="10dp">
         <solid android:color="#dddddd"/>
         <corners android:radius="5dp"/>
     </shape>

The background resource of the photo button could be as follows:

     <?xml version="1.0" encoding="utf-8"?>
     <shape xmlns:android="http://schemas.android.com/apk/res/android"
         android:shape="rectangle">
         <solid android:color="#888888" />
         <corners android:bottomRightRadius="50dp"
             android:bottomLeftRadius="50dp"
             android:topRightRadius="50dp"
             android:topLeftRadius="50dp"/>
         <stroke android:color="#ffffff"
             android:width="10dp"/>
     </shape>

### Extending Language of the SDK

The SDK comes in four languages: English, German, French and Spanish. If your App needs to work in 
another, not already supported language, you can easily extend the multi-language functionality of
the SDK.

Simply put the following XML elements in your strings.xml in the locale folder of your choice, in the 
"resources" tag and your App will work with another language.

The elements you want to change are:

    <string name="idcard">National ID Card</string>
    <string name="driver_license">Driver License</string>
    <string name="passport">Passport</string>
    <string name="select_document_type">Which document would you like to check?</string>

    <string name="takephoto_timeout_title">MANUAL capture</string>
    <string name="takephoto_timeout_text">Document could not be captured automatically. Please <b>capture</b> the document <b>manually</b>. Press the button below while placing the document into the frame.</string>

    <string name="takephoto_timeout_title_REDUCED">MANUAL Capture</string>
    <string name="takephoto_timeout_text_REDUCED1">Press the button below to take a picture</string>
    <string name="takephoto_timeout_text_REDUCED2">while placing the document into the frame.</string>

    <string name="ImageErrors_Title">Picture quality is not sufficient</string>
    <string name="ImageErrors_Text">Make sure there is no blur or glare and the document is correctly cropped.</string>

    <string name="ImageGlares_Title">Glare is detected</string>
    <string name="ImageGlares_Text">Try moving away from direct light.</string>

    <string name="Sharpness_Title">Image is out of focus</string>
    <string name="Sharpness_Text">Tap into the frame to adjust focus.</string>

    <string name="LessLight_Title">Bad light is detected</string>
    <string name="LessLight_Text">Try moving to a window to get better light.</string>

    <string name="NotCropped_Title"><b>Document not cropped properly</b></string>
    <string name="NotCropped_Text1"><b>Align the edges</b> of the cropping frame <b>exactly to</b> the edges of <b>your document</b>.</string>
    <string name="NotCropped_Text2">To move the edge of the cropping frame <b>tap on the circle</b> </string>
    <string name="NotCropped_Text3"><b>and move it</b> to the edge of the document as shown in the animation.</string>
    <string name="NotCropped_Text4"></string>
    <string name="NotCropped_Text5"></string>

    <string name="CaptureFront_Title">FRONT of the document</string>
    <string name="CaptureFront_Text1">Place the <b>front</b> of your</string>
    <string name="CaptureFront_Text2">document into the <b>frame</b>.</string>
    <string name="CaptureFront_Text3">The image will be captured <b>automatically.</b></string>

    <string name="CaptureBack_Title">BACK of the document</string>
    <string name="CaptureBack_Text1">Place the <b>back</b> of your</string>
    <string name="CaptureBack_Text2">document into the <b>frame</b>.</string>
    <string name="CaptureBack_Text3">The image will be captured <b>automatically</b>.</string>

    <string name="CaptureDataPage_Title">PASSPORT DATAPAGE</string>
    <string name="CaptureDataPage1">Place the <b>page</b> with your</string>
    <string name="CaptureDataPage2">photo into the <b>frame</b>.</string>
    <string name="CaptureDataPage3">The image will be captured <b>automatically</b>.</string>

    <string name="Verify_Title"></string>
    <string name="Verify_Text1"></string>
    <string name="Verify_Text2">Make sure your document details are</string>
    <string name="Verify_Text3"><b>clear to read</b>, with <b>no blur or glare</b> and the</string>
    <string name="Verify_Text4">document is <b>cropped correctly</b>.</string>
    <string name="Verify_Text5"></string>

    <string name="ImageToSmall_Title">Image too small</string>
    <string name="ImageToSmall_Text">The image you are trying to crop will be too small. Please adjust the frame right.</string>

    <string name="DocTypeFail_Title">Wrong cropping</string>
    <string name="DocTypeFail_Text">The document you are trying to crop has the wrong proportions. Please readjust.</string>

    <string name="tutorialOKBtn">OK, got it!</string>

    <string name="Accept_Image">MY DOCUMENT IS READABLE</string>
    <string name="GotItbtn">Got it!</string>
    <string name="Abortbtn">CANCEL</string>
    <string name="Retrybtn">TAKE A NEW PICTURE</string>

    <string name="document_frame">Please ensure that the entire document can be seen within the frame shown.</string>
    <string name="document_frame_2">Once the image is properly framed, it will be automatically captured.</string>

    <string name="sharp_1">Image out of focus.</string>
    <string name="sharp_2">Please tap on the screen to adjust focus.</string>

    <string name="faceoverlay_beforeStart1">The next step is to <b>capture your face</b>. <b>3 images</b> will be taken.</string>
    <string name="faceoverlay_beforeStart2">Please <b>keep your face in the circle</b> throughout the entire process.</string>
    <string name="faceoverlay_beforeStart3">Please follow further instructions.</string>
    <string name="faceoverlay_init1">Initialize</string>
    <string name="faceoverlay_init2">facial capturing…</string>
    <string name="faceoverlay_IsoScreen">Frame your face</string>
    <string name="faceoverlay_blinkScreen1"><b>Close your eyes</b></string>
    <string name="faceoverlay_blinkScreen2">until you notice a vibration.</string>
    <string name="faceoverlay_smileScreen1"><b>Smile</b></string>
    <string name="faceoverlay_smileScreen2">until you notice a vibration.</string>
    <string name="faceoverlay_smileScreen3">(Show your teeth!)</string>
    <string name="faceoverlay_helpScreen">The capturing process <b>failed</b>. Please repeat the process and consider the following hints:</string>
    <string name="faceoverlay_helpScreen_bullet1">\u2022 Make sure that your face is <b>lit evenly</b> throughout the shot.</string>
    <string name="faceoverlay_helpScreen_bullet2">\u2022 The face should be <b>clear of shadows</b> and the eyes, the nose and your mouth <b>clearly visible</b>.</string>
    <string name="faceoverlay_helpScreen_bullet3">\u2022 Look <b>straight</b> into the camera.</string>
    <string name="faceoverlay_button_checkedOK">OK, got it!</string>
    <string name="faceoverlay_errorTrackingScreen">Facial capture failed because face has been lost. <b>Please keep your face in the circle throughout the entire process.</b></string>
    <string name="faceoverlay_successScreen">Pictures were captured successfully. Please review the pictures and send them for review or repeat the capturing process.</string>
    <string name="faceoverlay_button_startCheck">Start verification</string>
    <string name="faceoverlay_button_repeat">Repeat capturing</string>
    <string name="faceoverlay_errorSecondRun">The facial image could not be captured successfully. Please try again later. </string>

    <string name="ok">OK</string>
    <string name="no">No</string>
    <string name="yes">Yes</string>

    <string name="abortbtn">Cancel</string>
    <string name="retrybtn">Retry</string>
    <string name="helpbtn">"?"</string>

    <string name="document_start_verification">Start verification</string>
    <string name="command_front">Now, capture the image of the <b>front</b> of the document.</string>
    <string name="command_back">Now, capture the image of the <b>back</b> of the document.</string>
    <string name="command_datapage">Now, capture the image of the <b>Datapage</b> of the passport.</string>

    <string name="command_hold1">Capturing in progress.</string>
    <string name="command_hold2">Please hold the document calm</string>
    <string name="command_hold3">and do not move. </string>

    <string name="command_hold4">Please hold still!</string>
    
    <string name="nfc_caption1"></string>
    <string name="nfc_caption2">Hold your phone onto your passport / id card</string>
    <string name="nfc_caption3"></string>
    <string name="nfc_donecaption1">Thank you!</string>
    <string name="nfc_donecaption2">Now, continue by clicking "Continue".</string>
    <string name="nfc_donecaption3"></string>
    <string name="nfc_documentnumbercaption">Document Number:</string>
    <string name="nfc_dateofbirthcaption">Date of Birth:</string>
    <string name="nfc_dateofexpirycaption">Date of Expiry:</string>
    <string name="nfc_btn">Continue</string>
    <string name="nfc_alert_caption">Note</string>
    <string name="nfc_skip">No NFC chip could be found. Continue without information of the chip.</string>
    <string name="nfc_fail">The connection to your chip couldn\'t be succesfully established for too many times.</string>
    <string name="nfc_mrz">Your document could not be read in full.</string>
    <string name="nfc_holdstill">Please hold still!</string>
    <string name="nfc_accesscontrol">Access Control</string>
    <string name="nfc_personalinformation">Personal Information</string>
    <string name="nfc_faceimage">Face Image</string>
    <string name="nfc_verification">Verification</string>

For example, if you want to add the Italian language, add a android resource folder named "values-it" 
to the "res" folder, add the strings of your app and finally add the XML elements of the SDK above you 
want to change with italian texts. 

### Unsupported devices

The processing of a document needs some crucial features of modern devices. For example, a flash light
on the back of a smartphone or tablet is necessary. The SDK can only produce good verification results, if
it uses the flash light for capturing a document. Since there are devices, that come without a flash light,
the SDK will detect this and does not allow to use the SDK on those devices. 

For you, as an integrator of the SDK you will get two methods to address this problem. First, you can
test by using a static method, if the device the SDK runs on, is supported like follows:

    if (GenuineIDActivity.isDeviceSupported( this ) != DeviceSupport.OK)
    {
        // ...
    }

If you do like stated, you have the chance to give the user a message and prevent from navigating to the 
SDK's *Activity*.

Second, there is a callback in *GenuineIDBaseController*, which can serve as an entry point for you to drop
the user a message and prevent him from using the SDK functionality:

    @Override
    public void doAfterDeviceIsNotSupported( DeviceSupport ds )
    {
        // Toast
        Toast.makeText( YourActivity.this, "Your device is not supported. Try with another one.", Toast.LENGTH_LONG).show();

        finish();
    }
    
### Document Type

Capturing a document depends on the knowledge of the expected format or aspect ratio of the document. 
If you do not specify a document type in your *Activity*, the user has to choose between two 
types: 1) TD3 (Passports) and 2) TD1 (driver licenses and id cards).

If you know the document type your use wants to capture before, you
have the possibility to set the document type, and the document type selection view will be skipped:

	...

    super.setDocumentType( DocumentType.TD1 ); // or DocumentType.TD3

    super.onCreate(savedInstanceState);

    ...

