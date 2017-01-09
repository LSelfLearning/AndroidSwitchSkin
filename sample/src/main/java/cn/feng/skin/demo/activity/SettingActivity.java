package cn.feng.skin.demo.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.feng.skin.demo.R;
import cn.lewish.changeskin.base.BaseSkinActivity;
import cn.lewish.changeskin.listener.ILoaderListener;
import cn.lewish.changeskin.loader.SkinManager;
import cn.lewish.changeskin.utils.L;

public class SettingActivity extends BaseSkinActivity {

	/**
	 * Put this skin file on the root of sdcard
	 * eg:
	 * /mnt/sdcard/BlackFantacy.skin
	 */
	private String skinPath = "";
	private TextView titleText;
	private Button setOfficalSkinBtn;
	private Button setNightSkinBtn;
	
	private boolean isOfficalSelected = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		initView();
		copyToAndroid();
	}
	private static void copyFileUsingFileStreams(File source, File dest)
			throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally {
			input.close();
			output.close();
		}
	}
	private static void copyFileUsingFileStreams(InputStream source, File dest)
			throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = source;
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally {
			input.close();
			output.close();
		}
	}
	private void copyToAndroid() {
		File filesDir = getFilesDir();
		skinPath = filesDir.toString() + "/blackNight.skin";
		File file = new File(skinPath);
		try {
			InputStream inputStream = getAssets().open("blackNight.skin");
			copyFileUsingFileStreams(inputStream,file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initView() {
		titleText = (TextView) findViewById(R.id.title_text);
		titleText.setText("设置皮肤");
		setOfficalSkinBtn = (Button) findViewById(R.id.set_default_skin);
		setNightSkinBtn = (Button) findViewById(R.id.set_night_skin);
		
		
		isOfficalSelected = !SkinManager.getInstance().isExternalSkin();
		
		if(isOfficalSelected){
			setOfficalSkinBtn.setText("官方默认(当前)");
			setNightSkinBtn.setText("黑色幻想");
		}else{
			setNightSkinBtn.setText("黑色幻想(当前)");
			setOfficalSkinBtn.setText("官方默认");			
		}
		
		setNightSkinBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSkinSetClick();
			}
		});
		
		setOfficalSkinBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onSkinResetClick();
			}
		});
	}

	protected void onSkinResetClick() {
		if(!isOfficalSelected){
			SkinManager.getInstance().restoreDefaultTheme();
			Toast.makeText(getApplicationContext(), "切换成功", Toast.LENGTH_SHORT).show();			
			setOfficalSkinBtn.setText("官方默认(当前)");
			setNightSkinBtn.setText("黑色幻想");
			isOfficalSelected = true;
		}
	}

	private void onSkinSetClick() {
		if(!isOfficalSelected) return;
		File skin = new File(skinPath);
		if(skin == null || !skin.exists()){
			Toast.makeText(getApplicationContext(), "请检查" + skinPath + "是否存在", Toast.LENGTH_SHORT).show();
			return;
		}

		SkinManager.getInstance().load(skin.getAbsolutePath(),
				new ILoaderListener() {
					@Override
					public void onStart() {
						L.e("startloadSkin");
					}

					@Override
					public void onSuccess() {
						L.e("loadSkinSuccess");
						Toast.makeText(getApplicationContext(), "切换成功", Toast.LENGTH_SHORT).show();
						setNightSkinBtn.setText("黑色幻想(当前)");
						setOfficalSkinBtn.setText("官方默认");		
						isOfficalSelected = false;
					}

					@Override
					public void onFailed() {
						L.e("loadSkinFail");
						Toast.makeText(getApplicationContext(), "切换失败", Toast.LENGTH_SHORT).show();
					}
				});
	}
}
