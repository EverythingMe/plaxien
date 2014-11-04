package me.everything.plaxien;

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

import me.everything.plaxien.json.JSONExplainBridge;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import com.google.gson.JsonElement;

public class ExplainActivity extends Activity {
	
	private static final String EXTRA_JSON_FILE_PATH = "jsonFilePath";
	private static final String EXTRA_ROOT_TITLE = "rootTitle";
	private static final String EXTRA_DELETE_WHEN_DONE = "deleteWhenDone";
    private static final String EXTRA_INTERNAL_SERIALIZATION = "internalSerialization";
	
	private boolean mDeleteWhenDone = false;
    private File mJsonFile = null;
	private String mRootTitle = "Explanation";
    private boolean mInternalSerialization;

    /**
     * Convenience function to create a new explain intent for launching
     * @param context the app context
     * @param rootTitle the title of the explain activity
     * @param jsonFilePath where the json dump file is stored
     * @param deleteFileWhenDone delete the file when the activity is closed
     * @param internalSerialization set to true if the the json represents an internally serialized explain tree
     * @return
     */
	static private Intent createIntent(Context context, String rootTitle, File jsonFilePath,
                                      boolean deleteFileWhenDone, boolean internalSerialization) {

		Intent intent = new Intent(context, ExplainActivity.class);
		intent.putExtra(EXTRA_JSON_FILE_PATH, jsonFilePath.getAbsolutePath());
		intent.putExtra(EXTRA_ROOT_TITLE, rootTitle);
		intent.putExtra(EXTRA_DELETE_WHEN_DONE, deleteFileWhenDone);
        intent.putExtra(EXTRA_INTERNAL_SERIALIZATION, internalSerialization);
		return intent;
	}

    /**
     * Create a shared dump file for a json explain
     * @param name the name of the file - practically the root title
     * @param context the app context
     */
    private static File createDumpFile(String name, Context context) {
        return new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), name + ".json");
    }

    /**
     * Dump a json string to the out file
     */
    private static void writeDump(File file, String dump) {

        PrintWriter out;
        try {
            out = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            return;
        }

        try {
            out.println(dump);
        } finally {
            out.close();
        }
    }


    /**
     * The default explain function that should be used in most cases, where you need to show an explain
     * tree that has been created on the client
     * @param context the app context
     * @param rootTitle the title of the explain activity
     * @param root the root node of an explain tree
     * @param deleteFileWhenDone delete the file when the activity is closed
     *
     * @see me.everything.plaxien.Explain.Node
     */
    public static void explain(Context context, String rootTitle, Explain.Node root, boolean deleteFileWhenDone) {
        File explainFile = createDumpFile(rootTitle, context);

        writeDump(explainFile, root.toJSON());

        Intent intent = ExplainActivity.createIntent(context, rootTitle, explainFile, deleteFileWhenDone, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }

    /**
     * Create an explain activity from a raw JSON string from a third party, i.e. server generated explain
     * @param context the app context
     * @param rootTitle the title of the explain activity
     * @param jsonData raw JSON obtained elsewhere
     * @param deleteFileWhenDone delete the file when the activity is closed
     */
    public static void explainJson(Context context, String rootTitle, String jsonData, boolean deleteFileWhenDone) {
        File explainFile = createDumpFile(rootTitle, context);

        writeDump(explainFile, jsonData);

        explainFromJsonFile(context, rootTitle, explainFile, deleteFileWhenDone);
    }

    /**
     * Create an activity from a raw json string. Prefer {explainJson} instead
     * @param context the app context
     * @param rootTitle the title of the explain activity
     * @param explainDir the base dir where the file resides
     * @param explainFileName the name of the file
     * @param jsonData raw JSON obtained elsewhere
     * @param deleteFileWhenDone delete the file when the activity is closed
     */
    @Deprecated
	public static void explainFromJsonString(Context context, String rootTitle, File explainDir,
                                             String explainFileName, String jsonData, boolean deleteFileWhenDone) {
		File explainFile = new File(explainDir, explainFileName);
		writeDump(explainFile, jsonData);

		explainFromJsonFile(context, rootTitle, explainFile, deleteFileWhenDone);
	}


    /**
     * Create an explain activity from a JSON file that already exists on the device
     * @param context the app context
     * @param rootTitle the title of the explain activity
     * @param jsonDataFile the path to where the file is stored
     * @param deleteWhenDone delete the file when the activity exits
     */
	public static void explainFromJsonFile(Context context, String rootTitle, File jsonDataFile,
                                           boolean deleteWhenDone) {

		Intent intent = ExplainActivity.createIntent(context, rootTitle, jsonDataFile, deleteWhenDone, false);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    /**
     * Read all the contents of a stream into a string
     * @param is input stream
     * @return the stream content
     * @throws IOException
     */
	private static String readAll(InputStream is) throws IOException {
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
			    String ret = readAll(fin);
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
            String jsonFilePath = intent.getExtras().getString(EXTRA_JSON_FILE_PATH);
			mJsonFile = new File(jsonFilePath);
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
        if (intent.hasExtra(EXTRA_INTERNAL_SERIALIZATION)) {
            mInternalSerialization = intent.getExtras().getBoolean(EXTRA_INTERNAL_SERIALIZATION);
        }

		ScrollView contentLayout = (ScrollView) findViewById(R.id.plaxien_content_layout);
		
		String jsonData = "{}";
		if (mJsonFile != null) {
			jsonData = getStringFromFile(mJsonFile);
		}

        ExplainViewFactory viewFactory = new ExplainViewFactory(this);
        Explain.Node node;
        if (!mInternalSerialization) {
        	
        	
            JSONExplainBridge bridge = new JSONExplainBridge(); 
            try {
            	node = bridge.parseJSON(jsonData, mRootTitle, true);
            } catch (Exception e) {
            	node = new Explain.Node("Could not parse explain JSON", true);
            	node.addValue("Error", e.getMessage());
            }
        } else {
            node = Explain.Node.fromJSON(jsonData);
        }

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
