package ru.meefik.wshell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private static final String SHELL_IN_A_BOX = "shellinaboxd";
	private static String FILES_DIR;
	private static String PORT;
	private static Boolean LOCALHOST;
	private static Boolean ROOT;
	private static Boolean AUTOSTART;
	private static String SHELL;
	private static String USERNAME;
	private static String PASSWORD;
	private static Boolean ACTIVE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FILES_DIR = getApplicationInfo().dataDir;
		extractData();
		
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setOnClickListener(new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	        	runURL();
	        }
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_start:
			start(false);
			printStatus();
			break;
		case R.id.menu_stop:
			stop();
			printStatus();
			break;
		case R.id.menu_settings:
			Intent intent_settings = new Intent(this, SettingsActivity.class);
			startActivity(intent_settings);
			break;
		case R.id.menu_about:
			Intent intent_about = new Intent(this, AboutActivity.class);
			startActivity(intent_about);
			break;
		case R.id.menu_exit:
			finish();
			break;
		}
		return false;
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		loadPrefs();
		if (AUTOSTART && !ACTIVE) {
			start(false);
		}
		printStatus();
	}
	
	private void loadPrefs() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		PORT = sp.getString("port", getString(R.string.port));
		LOCALHOST = sp.getBoolean("localhost", getString(R.string.localhost).equals("true"));
		ROOT = sp.getBoolean("root", getString(R.string.root).equals("true"));
		AUTOSTART = sp.getBoolean("autostart", getString(R.string.autostart).equals("true"));
		SHELL = sp.getString("shell", getString(R.string.shell));
		USERNAME = sp.getString("username", getString(R.string.username));
		PASSWORD = sp.getString("password", getString(R.string.password));
		ACTIVE = isAlive();
	}

	private boolean copyFile(String sourceFile, File targetFile) {
		boolean result = true;
		AssetManager assetManager = getBaseContext().getAssets();
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(sourceFile);
			out = new FileOutputStream(targetFile);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	private String getLocalIpAddress() {
		String ip = "127.0.0.1";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& !inetAddress.isLinkLocalAddress()) {
						ip = inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return ip;
	}
	
	private void extractData() {
		File file = new File(FILES_DIR + File.separator + SHELL_IN_A_BOX);
		if (!file.exists()) {
			copyFile(SHELL_IN_A_BOX, file);
			file.setExecutable(true);
		}
	}
	
	private void start(boolean restart) {
		List<String> list = new ArrayList<String>();
		String cmd = FILES_DIR + File.separator + SHELL_IN_A_BOX + " -t -p "+PORT+" --shell="+SHELL+":"+USERNAME+":"+PASSWORD;
		if (LOCALHOST) {
			cmd += " --localhost-only";
		}
		if (ROOT) {
			list.add("su");
			cmd += " -u 0 -g 0";
		} else {
			list.add("sh");
		}
		if (restart) {
			list.add("pkill -9 " + SHELL_IN_A_BOX);
			list.add("sleep 1");
		}
		list.add(cmd);
		new Thread(new ExecCmd(list)).start();
		ACTIVE = true;
	}
	
	private void stop() {
		List<String> list = new ArrayList<String>();
		if (ROOT) {
			list.add("su");
		} else {
			list.add("sh");
		};
		String cmd = "pkill -9 " + SHELL_IN_A_BOX;
		list.add(cmd);
		new Thread(new ExecCmd(list)).start();
		ACTIVE = false;
	}
	
	private boolean isAlive() {
		boolean active = false;
		List<String> list = new ArrayList<String>();
		String cmd = "ps | grep " + SHELL_IN_A_BOX;	
		list.add(cmd);
		ExecCmd r = new ExecCmd(list);
		r.run();
		if (r.getOutput().size() > 1) {
			active = true;
		}
		return active;
	}
	
	private void runURL() {
		if (ACTIVE) {
			TextView tv = (TextView) findViewById(R.id.textView1);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) tv.getText()));
			startActivity(browserIntent);
		}
	}
	
	private void printStatus() {
		String text = "Inactive";
		if (ACTIVE) {
			String ipaddress = "127.0.0.1";
			if (!LOCALHOST) ipaddress = getLocalIpAddress();
			text = "http://"+ipaddress+":"+PORT;
		}
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setText(text);
	}

}
