package com.example.audioprototype;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	// This will be used to record the call
	MediaRecorder myRecorder = new MediaRecorder();
	
	// This will be used to play audio files that have been recorded
	MediaPlayer myPlayer = new MediaPlayer();
	
	// CheckBox in layout which tells whether or not to record the phone call
	CheckBox recordConversation;
	
	Button playAudioFile;
	
	@Override
	// The onCreate method is called when the app is started
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Get the checkBox view from the XML layout
		recordConversation = (CheckBox) findViewById(R.id.recordBox);
		
		playAudioFile = (Button) findViewById(R.id.playFile);
		playAudioFile.setText("Play Audio File");
				
		// Listen to the phone state in the background (see phonelistener for more details)
		PhoneCallListener phoneListener = new PhoneCallListener();
		TelephonyManager telephonyManager = (TelephonyManager) this
			.getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
				
		return true;
	}

	// Called when the call has ended
	public void endCall() throws IllegalArgumentException, SecurityException, 
								 IllegalStateException, IOException {		
		
		// If the phone call was recorded
		if (recordConversation.isChecked()) {
		
			// Close the recorder
			myRecorder.stop();	// Stop the recorder
			myRecorder.reset();   // You can reuse the object by going back to setAudioSource() step
			myRecorder.release(); // Now the object cannot be reused
			
			Log.i("Main Activity", "Recording Ended");
			
		}
	}
	
	// Called to start recording a phone call
	@SuppressLint("SimpleDateFormat")
	public void startRecording(int value) throws IllegalStateException, IOException {
		
		// Make sure the audioRecords directory exists
		File audioDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audioRecords");
		
		if (!audioDir.exists()) {
			audioDir.mkdir();
		}
		
		// Get the path of the file that the audio will be written to
		String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		// Get the date in simple format
		SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
		String date = s.format(new Date());
			
		// Add the date and file name to the path of the directory
		// The filename will look like: "audioRecord_ddMMyyyyhhnmmss.3gp"
		String appendToOutputFile = "/audioRecords/audioRecord_" + date + ".3gp";
		
		outputFile += appendToOutputFile;
				
		Log.i("Main Activity", outputFile);
		
		// Make a new MediaRecorder
		myRecorder = new MediaRecorder();
		
		// Set the attributes
		// Record using the Mic
		myRecorder.setAudioSource(value);
		// Create a .3gp file
		myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		// Encode using what is best for voice recognition
		myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		myRecorder.setAudioChannels(2);
		
		// Set the file that the audio will be written to
		myRecorder.setOutputFile(outputFile);
		
		// Start recording
		myRecorder.prepare();
		
		try {
			myRecorder.start();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), 
					"Direct Voice Recording is not Permitted on this Device", Toast.LENGTH_SHORT).show();
			
			myRecorder.reset();
			
			startRecording(MediaRecorder.AudioSource.MIC);
		}
	}
	
	// Play the audio from a specified file
	public void playAudioFromFile(File file) throws IOException {
		
		// Instantiate a new MediaPlayer
		myPlayer = new MediaPlayer();
		
		Log.d("RUN", Environment.getExternalStorageDirectory().getAbsolutePath() + 
				"/" + file.getName());
		
		if (file.exists()) {
			
			myPlayer.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer arg0) {
					Log.v("DONE", "Done Playing");
					myPlayer.stop();
					myPlayer.release();
					
					playAudioFile.setText("Play Audio File");
				}
			});
			
			FileInputStream inputStream = new FileInputStream(file);
			FileDescriptor fd = inputStream.getFD();
			myPlayer.setDataSource(fd);
			inputStream.close();
						
			myPlayer.prepare();
			// Begin playing
			myPlayer.start();
		}
	}
	
	public void createNotification() {		
		NotificationCompat.Builder builder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, MainActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(MainActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		builder.setContentIntent(resultPendingIntent);
		NotificationManager notificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		notificationManager.notify(1, builder.build());
	}
	
	// Called when the play audio file button is clicked
	public void playFileButtonClicked(View v) throws IllegalArgumentException, SecurityException, 
													 IllegalStateException, IOException { 
		
		
		if (playAudioFile.getText().toString() == "Play Audio File") {
		
			playAudioFile.setText("Stop Playing File");
						
			// Get the folder that all the audio files are stored in
			File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audioRecords");
			File[] listOfFiles = folder.listFiles();
			ArrayList<File> playableFiles = new ArrayList<File>();
			
			// Iterate through the folder and check which files start with "audioRecord"
			for (int i = 0; i < listOfFiles.length; i++) {
				Log.i("Main Activity", listOfFiles[i].getName());
				if (listOfFiles[i].getName().split("_")[0].trim().equals("audioRecord")) {
					Log.d("FOUND", listOfFiles[i].getName());
					// Add the file to the playable files list
					playableFiles.add(listOfFiles[i]);
				}
			}
			
			// Play the first file found
			playAudioFromFile(playableFiles.get(playableFiles.size() - 1));
		} else {
			playAudioFile.setText("Play Audio File");
			
			Log.v("DONE", "Done Playing");
			myPlayer.stop();
			myPlayer.release();
		}
		
		
	}
	
	public void recordBoxClicked(View v) {
		Log.i("RecordBox", "RecordBoxClicked");
	}
	
	// The PhoneCallListener will listen for a change in state of the phone app in the background
	// It will detect when a phone call begins and ends while running in the background
	private class PhoneCallListener extends PhoneStateListener {
		 
		private boolean isPhoneCalling = false;
 
		String LOG_TAG = "LOGGING 123";
 
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
 
			if (TelephonyManager.CALL_STATE_RINGING == state) {
				// Phone is ringing
				Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
			}
 
			if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
				// Active phone call
				Log.i(LOG_TAG, "OFFHOOK");
				
				createNotification();
 
				CheckBox recordConversation = (CheckBox) findViewById(R.id.recordBox);
				
				// If the conversation should be recorded
				if (recordConversation.isChecked()) {
				
					try {
						// Start Recording
						startRecording(MediaRecorder.AudioSource.VOICE_CALL);
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				isPhoneCalling = true;
			}
 
			if (TelephonyManager.CALL_STATE_IDLE == state) {
				// Run when class initial and phone call ended, 
				// Need detect flag from CALL_STATE_OFFHOOK
				Log.i(LOG_TAG, "IDLE");
 
				// If the phone has gone from in call to Idle
				if (isPhoneCalling) {
  
					try {
						// End the call (close the MediaRecorder)
						endCall();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
 
					isPhoneCalling = false;
				}
			}
		}
	}
}
