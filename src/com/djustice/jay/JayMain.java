package com.djustice.jay;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alicebot.ab.AIMLProcessor;
import org.alicebot.ab.Bot;
import org.alicebot.ab.Chat;
import org.alicebot.ab.Graphmaster;
import org.alicebot.ab.MagicBooleans;
import org.alicebot.ab.MagicStrings;
import org.alicebot.ab.PCAIMLProcessorExtension;
import org.alicebot.ab.Timer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class JayMain extends Activity {

	public Bot bot;
	public static Chat chat;
	public TextView tvOutput;
	public EditText etInput;
	public Button btnParse;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_jay_main);
		
		tvOutput = (TextView) findViewById(R.id.tvOutput);
		etInput = (EditText) findViewById(R.id.etInput);
		btnParse = (Button) findViewById(R.id.btnParse);
		btnParse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				tvOutput.setText(tvOutput.getText().toString() + "\nA: " + etInput.getText().toString());
				String response = chat.multisentenceRespond(etInput.getText().toString());
				if (response.contains("<oob>")) {
					response = response.replace("<oob>", "");
					response = response.replace("</oob>", "");
					
					if (response.contains("<dial>")) {
						response = response.replace("<dial>", "");
						response = response.replace("</dial>", ",");

						try {
							Intent callIntent = new Intent(Intent.ACTION_CALL);
							callIntent.setData(Uri.parse("tel:" + response.split(",")[0]));
							startActivity(callIntent);
						} catch (ActivityNotFoundException activityException) {
							Log.e("Calling a Phone Number", "Call failed", activityException);
						}
						
						response = response.split(",")[1];
						System.out.println("XXX:" + response);
					}
				}
				tvOutput.setText(tvOutput.getText().toString() + "\nB: " + response + "\n");
				etInput.setText("");
			}
		});

		boolean a = isSDCARDAvailable();
		AssetManager assets = getResources().getAssets();
		File jayDir = new File(Environment.getExternalStorageDirectory().toString() + "/jay/bots/justice");
		boolean b = jayDir.mkdirs();
		if (jayDir.exists()) {
			try {
				for (String dir : assets.list("justice")) {
					File subdir = new File(jayDir.getPath() + "/" + dir);
					boolean subdir_check = subdir.mkdirs();
					for (String file : assets.list("justice/" + dir)) {
						File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
						if (f.exists()) {
							continue;
						}
						InputStream in = null;
						OutputStream out = null;
						in = assets.open("justice/" + dir + "/" + file);
						out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		MagicStrings.root_path = Environment.getExternalStorageDirectory().toString() + "/jay";
		System.out.println("Working Directory = " + MagicStrings.root_path);
		AIMLProcessor.extension =  new PCAIMLProcessorExtension();
		bot = new Bot("justice", MagicStrings.root_path, "chat");
		chat = new Chat(bot);
		String[] args = null;
		mainFunction(args);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.jay_main, menu);
		return true;
	}

	public static boolean isSDCARDAvailable(){
		return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true :false;
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	public static void mainFunction (String[] args) {
		MagicBooleans.trace_mode = false;
		System.out.println("trace mode = " + MagicBooleans.trace_mode);
		Graphmaster.enableShortCuts = true;
		Timer timer = new Timer();
		String request = "Hello.";
		String response = chat.multisentenceRespond(request);

		System.out.println("Human: "+request);
		System.out.println("Robot: "+response);
	}
}
