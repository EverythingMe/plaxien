package me.everything.plaxien.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.everything.plaxien.Explain;
import me.everything.plaxien.ExplainViewFactory;
import me.everything.plaxien.R;
import me.everything.plaxien.json.JSONExplainBridge;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

public class PlaxienActivity extends Activity {
	
	private static final String EXTRA_JSON_FILE_PATH = "jsonFilePath";
	private static final String EXTRA_ROOT_TITLE = "rootTitle";
	private static final String EXTRA_DELETE_WHEN_DONE = "deleteWhenDone";
	
	private boolean mDeleteWhenDone = false;
	private String mJsonFilePath = null;
	private File mJsonFile = null;
	private String mRootTitle = "Explanation";
	
	public static Intent createIntent(Context context, String rootTitle, File jsonFilePath, boolean deleteFileWhenDone) {
		Intent intent = new Intent(context, PlaxienActivity.class);
		intent.putExtra(EXTRA_JSON_FILE_PATH, jsonFilePath.getAbsolutePath());
		intent.putExtra(EXTRA_ROOT_TITLE, rootTitle);
		intent.putExtra(EXTRA_DELETE_WHEN_DONE, deleteFileWhenDone);
		return intent;
	}
	
	public static void explainFromJsonString(Context context, String rootTitle, File explainDir, String explainFileName, String jsonData, boolean deleteFileWhenDone) {
		File explainFile = new File(explainDir, explainFileName);
		PrintWriter out;
		try {
			out = new PrintWriter(explainFile);
		} catch (FileNotFoundException e) {
			return;
		}
		
		try {
			out.println(jsonData);
		} finally {
			out.close();
		}
		explainFromJsonFile(context, rootTitle, explainFile, deleteFileWhenDone);
	}
	
	public static void explainFromJsonFile(Context context, String rootTitle, File jsonDataFile, boolean deleteWhenDone) {
		Intent intent = PlaxienActivity.createIntent(context, rootTitle, jsonDataFile, deleteWhenDone);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
		if (intent != null) {
			context.startActivity(intent);
		}		
	}
	
	private static String convertStreamToString(InputStream is) throws IOException {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    return sb.toString();
	}

	// from [http://stackoverflow.com/questions/12910503/read-file-as-string]
	private static String getStringFromFile(File file) {
		try {
		    FileInputStream fin = new FileInputStream(file);
			try {
			    String ret = convertStreamToString(fin);
			    return ret;
			} finally {
				fin.close();
			}
		} catch (IOException ex) {
			return null;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plaxien_activity);
		
		Intent intent = getIntent();
		
		if (intent.hasExtra(EXTRA_JSON_FILE_PATH)) {
			mJsonFilePath = intent.getExtras().getString(EXTRA_JSON_FILE_PATH);
			mJsonFile = new File(mJsonFilePath);
			getActionBar().setTitle("Explain: " + mJsonFile.getName());			
		} else {
			getActionBar().setTitle("Explain");
		}
		if (intent.hasExtra(EXTRA_ROOT_TITLE)) {
			mRootTitle = intent.getExtras().getString(EXTRA_ROOT_TITLE);
		}
		if (intent.hasExtra(EXTRA_DELETE_WHEN_DONE)) {
			mDeleteWhenDone = intent.getExtras().getBoolean(EXTRA_DELETE_WHEN_DONE);
		}

		ScrollView contentLayout = (ScrollView) findViewById(R.id.plaxien_content_layout);
		
		String jsonData = "{}";
		if (mJsonFile != null) {
			jsonData = getStringFromFile(mJsonFile);
		}

		ExplainViewFactory viewFactory = new ExplainViewFactory(this);
		JSONExplainBridge bridge = new JSONExplainBridge();
		Explain.Node node = bridge.parseJSON(jsonData, mRootTitle, true);
		View view = viewFactory.getNodeView(node);
		
		contentLayout.addView(view);		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mDeleteWhenDone) {
			mJsonFile.delete();
		}
	}	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu_plaxien_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_share) {
            handleShare();
            return true;			
		}		
        return super.onOptionsItemSelected(item);
	}
	
	private void handleShare() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		String android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String time = sdf.format(new Date());

		String subject = "Explain Dump [" + mRootTitle + " @ " + time + "]";
		String text = "See file attachment.";
		if (android_id != null)
			text += "\nOrigin Device ID [" + android_id + "].";    	    	

		intent.putExtra(Intent.EXTRA_SUBJECT, subject);
		intent.putExtra(Intent.EXTRA_TEXT, text);

		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mJsonFile));
		intent.setType("application/file");

		startActivity(Intent.createChooser(intent, "Share explain dump to..."));
	}
	
}
