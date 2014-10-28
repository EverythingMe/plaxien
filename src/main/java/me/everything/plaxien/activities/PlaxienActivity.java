package me.everything.plaxien.activities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import me.everything.plaxien.Explain;
import me.everything.plaxien.ExplainViewFactory;
import me.everything.plaxien.R;
import me.everything.plaxien.json.JSONExplainBridge;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

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
		getActionBar().setTitle("Plaxien");
		
		Intent intent = getIntent();
		
		if (intent.hasExtra(EXTRA_JSON_FILE_PATH)) {
			mJsonFilePath = intent.getExtras().getString(EXTRA_JSON_FILE_PATH);
			mJsonFile = new File(mJsonFilePath);
		}
		if (intent.hasExtra(EXTRA_ROOT_TITLE)) {
			mRootTitle = intent.getExtras().getString(EXTRA_ROOT_TITLE);
		}
		if (intent.hasExtra(EXTRA_DELETE_WHEN_DONE)) {
			mDeleteWhenDone = intent.getExtras().getBoolean(EXTRA_DELETE_WHEN_DONE);
		}

		LinearLayout contentLayout = (LinearLayout) findViewById(R.id.plaxien_content_layout);
		
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
}
