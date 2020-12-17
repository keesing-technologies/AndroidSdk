package com.keesing.kvsclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.ArrayMap;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jenid.mobile.capture.configuration.DeviceSupport;
import com.jenid.mobile.capture.controller.GenuineIDActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import androidx.appcompat.app.AlertDialog;

public class DemoActivity extends GenuineIDActivity {

	private RequestQueue _requestQueue;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.setPrimaryColor(getResources().getColor(R.color.keesingBlue));
		this.setTextFont("fonts/Ubuntu-Regular.ttf");
		this.setSecondaryTextFont("fonts/Ubuntu-Italic.ttf");

		super.onCreate(savedInstanceState);

		super.setTakePhotoTimeOut(15000);
		super.setEnableFaceDetection(true);
		super.setEnableNFC(false);
		super.setEnableAllowWithoutNFC(true);

		_requestQueue = Volley.newRequestQueue(this); // 'this' is Context
	}

	@Override
	public void doAfterDocumentFound(Bitmap frontImage,
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
									 boolean verification) {
		try {
			//This is an example to POST the completeJsonPayload to Keesing's test server. Replace it to yours
			final String myBackend = "https://dev-demo.keesingtechnologies.com/sdkapi";
			final JSONObject jsonBody = new JSONObject(completeJsonPayload);

			JsonObjectRequest req = new JsonObjectRequest(
				myBackend,
				jsonBody,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							VolleyLog.v("Response:%n %s", response.toString(2));
							showAlert("Data sent to the server", android.R.drawable.ic_dialog_info);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.e("Error: ", error.getMessage());
						showAlert("Unable to send data to the server", android.R.drawable.ic_dialog_alert);
					}
				}) {

				@Override
				public Map<String, String> getHeaders() throws AuthFailureError {
					final String token = "...";

					Map<String, String> headers = new ArrayMap<>();
					headers.put("Authorization", "Bearer " + token);
					return headers;
				}
			};

			_requestQueue.add(req);
		} catch (JSONException e) {
			e.printStackTrace();

			showAlert("Invalid payload", android.R.drawable.ic_dialog_alert);
		}
	}

	public void showAlert(String message, int iconId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(DemoActivity.this);
		builder
			.setMessage(message)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

				}
			})
			.setIcon(iconId)
			.show();
	}

	@Override
	public void doAfterFail(
		Bitmap frontImage,
		String encodedFrontImage,
		Bitmap backImage,
		String encodedBackImage,
		String completePayload) {
		DemoActivity.this.navigateBackHere();
	}

	private void navigateBackHere() {
		Intent i = new Intent(DemoActivity.this, DemoActivity.class);
		startActivity(i);
		finish();
	}

	@Override
	public void renderButton(Button button) {
		super.renderButton(button);
		button.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Italic.ttf"));
		button.setBackgroundColor(getResources().getColor(R.color.keesingBlue));
	}

	@Override
	public void renderSecondaryButton(Button button) {
		super.renderSecondaryButton(button);
		button.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Ubuntu-Italic.ttf"));
		button.setBackgroundColor(Color.GRAY);
	}

	@Override
	public void renderPhotoButton(Button button) {
		super.renderPhotoButton(button);
	}


	@Override
	public void backPressed() {
		System.exit(0);
	}


	@Override
	public void doAfterDeviceIsNotSupported(DeviceSupport deviceSupport) {
		AlertDialog.Builder builder = new AlertDialog.Builder(DemoActivity.this);
		builder
			.setTitle("Device not supported")
			.setMessage("Problem happened running application <Cause: " + deviceSupport.toString() + ">")
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					System.exit(0);
				}
			}).setIcon(android.R.drawable.ic_dialog_alert)
			.show();
	}
}
