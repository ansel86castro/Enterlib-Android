package com.enterlib.support;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.enterlib.app.UIUtils;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.NotifyPropertyChanged;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.fields.Field;
import com.enterlib.mvvm.SelectionCommand;
import com.enterlib.mvvm.support.FormFragment;
import com.enterlib.threading.LoaderHandler;
import com.enterlib.threading.LoaderHandler.LoadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ActivityPickFile extends AppCompatActivity implements View.OnClickListener {
	
	public static final String EXTRA_FILE = "EXTRA_FILE";
	public static final String MODE = "MODE";

	public static final int MODE_PICK_FILE = 1;
	public static final int MODE_PICK_FOLDER = 2;

	private File sdRoot;
	private int mode;

	public File getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(File selectedItem) {
		this.selectedItem = selectedItem;
	}

	File selectedItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_frame);

		Intent intent = getIntent();
		mode = intent.getIntExtra(MODE, MODE_PICK_FILE);

		sdRoot = Environment.getExternalStorageDirectory(); //new File("/storage/");
		if(!sdRoot.exists()){
			throw new InvalidOperationException();
		}
		//sdRoot = Environment.getExternalStorageDirectory();
        selectedItem= sdRoot;

		if(savedInstanceState == null){					
			 getSupportFragmentManager()
			 .beginTransaction()
			 .add(R.id.container, FragmentDir.newIntance(sdRoot.getAbsolutePath()))
			 .addToBackStack(sdRoot.getAbsolutePath())
			 .commit();
		}
		
		setResult(RESULT_CANCELED);
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if(mode == MODE_PICK_FILE) {
			findViewById(R.id.actionBar).setVisibility(View.GONE);
		}

		findViewById(R.id.btnCancel).setOnClickListener(this);
		findViewById(R.id.btnAccept).setOnClickListener(this);
	}
	
	@Override
	public boolean onNavigateUp() {	
		FragmentDir fr = (FragmentDir) getSupportFragmentManager().findFragmentById(R.id.container);
		if(fr.filePath.equals(sdRoot.getAbsolutePath())){
			finish();
		}else{
			getSupportFragmentManager()
			 .popBackStack();
		}
			
		return true;
	}

	@Override
	public boolean onSupportNavigateUp() {
		return onNavigateUp();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		getMenuInflater().inflate(R.menu.activity_pick_file, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.cerrar){
			finish();
			return true;
		}
	  return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.btnCancel){
			finish();
		} else if (id == R.id.btnAccept){
			File itemFile = selectedItem;
			if(mode == MODE_PICK_FOLDER && !itemFile.isDirectory()) {
				Toast.makeText(this, R.string.select_folder_warning, Toast.LENGTH_SHORT).show();
				return;
			}

			Intent result = new Intent();
			result.putExtra(EXTRA_FILE, itemFile.getAbsolutePath());
			result.setData(Uri.fromFile(itemFile));
			setResult(RESULT_OK, result);
			finish();
		}
	}

	public static class FragmentDir extends FormFragment {
		static final String PATH = "PATH";
		
		String filePath;
		File file;
		FileViewModel[]childs;
		LoaderHandler loader;
		int mode;
		ActivityPickFile activityPickFile;

		@Override
		public void onAttach(Activity activity) {
			mode = activity.getIntent().getIntExtra(MODE, MODE_PICK_FILE);
			activityPickFile = (ActivityPickFile) activity;
			super.onAttach(activity);
		}

		public FileViewModel[] getItems(){
			return childs;
		}
		
		public SelectionCommand Selection = new SelectionCommand(){

			@Override
			public void invoke(Field field, AdapterView<?> adapterView,
					View itemView, int position, long id) {
				
				FileViewModel item = (FileViewModel) adapterView.getItemAtPosition(position);
				activityPickFile.setSelectedItem(item.getFile());
				File itemFile = item.getFile();
				if(itemFile.isDirectory()){
					 getActivity().getSupportFragmentManager()
					 .beginTransaction()
					 .replace(R.id.container, FragmentDir.newIntance(itemFile.getAbsolutePath()))
					 .addToBackStack(itemFile.getAbsolutePath())
					 .commit();
				}else if(mode == MODE_PICK_FILE){
					Intent result = new Intent();
					result.putExtra(EXTRA_FILE, itemFile.getAbsolutePath());
					result.setData(Uri.fromFile(itemFile));
					getActivity().setResult(RESULT_OK, result);
					getActivity().finish();
				}
			}
			
		};

		private int width;

		private int height;
		
		public static FragmentDir newIntance(String filePath){
			Bundle args = new Bundle();
			args.putString(PATH, filePath);
			FragmentDir fr= new FragmentDir();
			fr.setArguments(args);
			return fr;			
		}
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			return inflater.inflate(R.layout.fragment_dir, container, false);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {		
			super.onActivityCreated(savedInstanceState);

			Resources res = getResources();
			width  =(int) res.getDimension(R.dimen.image_file_width);
			height = (int) res.getDimension(R.dimen.image_file_height);
			
			filePath = getArguments().getString(PATH);
			file = new File(filePath);
			
			File[] subFiles = file.listFiles();
			if(subFiles == null) {
				childs = new FileViewModel[0];
			}else {
				childs = new FileViewModel[subFiles.length];
				if (loader == null) {
					loader = new LoaderHandler();
					loader.setAutofinish(false);
				}

				for (int i = 0; i < subFiles.length; i++) {
					final FileViewModel vm = new FileViewModel(subFiles[i]);
					//final File filename = subFiles[i];
					childs[i] = vm;
				}
			}
		}
		
		@Override
		protected BindingResources getBindingResources() {
			return new BindingResources()
			.put("R.layout", R.layout.class);
		}
		
		@Override
		public void onStart() {		
			super.onStart();
			
			updateTargets();
		}
		
		@Override
		public void onDestroy() {
			if(loader!=null){
				loader.stop();
				loader = null;
			}
			
			super.onDestroy();
		}
		
		public Drawable getImage(File file){
			Resources res = getResources();
			
			
			if(file.isDirectory())
				return res.getDrawable(R.drawable.folder);
			
			String name = file.getName();
			int extIndex = name.lastIndexOf('.');
			if(extIndex < 0){
				return res.getDrawable(R.drawable.file);
			}
			
			String ext = name.substring(extIndex);
			if(ext.equalsIgnoreCase(".txt")){
				return res.getDrawable(R.drawable.blog);
			}else if(ext.equalsIgnoreCase(".pdf")){
				return res.getDrawable(R.drawable.acroread);
			}else if(ext.equalsIgnoreCase(".rar")){
				return res.getDrawable(R.drawable.archive);		
			}else if(ext.equalsIgnoreCase(".zip")){
				return res.getDrawable(R.drawable.zip);		
			}else if(ext.equalsIgnoreCase(".mp3") ||
					ext.equalsIgnoreCase(".acc")){
				return res.getDrawable(R.drawable.agt_mp3);
			}else if(ext.equalsIgnoreCase(".db")||
					 ext.equalsIgnoreCase(".sqlite")||
					 ext.equalsIgnoreCase(".db3")){
				return res.getDrawable(R.drawable.database_flat);
			}else if(ext.equalsIgnoreCase(".jpg")|| 
					ext.equalsIgnoreCase(".png") ||
					ext.equalsIgnoreCase(".gif")){				
				//return res.getDrawable(R.drawable.image);	
				FileInputStream is;
				try {
					is = new FileInputStream(file);
					
					final Options options = new Options();
				    options.inJustDecodeBounds = true;
				    BitmapFactory.decodeStream(is, null, options);

				    // Calculate inSampleSize
				    options.inSampleSize = UIUtils.calculateInSampleSize(options.outWidth, options.outHeight, width, height);

				    // Decode bitmap with inSampleSize set
				    options.inJustDecodeBounds = false;
				    
				    is.close();
				    is = new FileInputStream(file);
				    Bitmap bmp  =  BitmapFactory.decodeStream(is, null, options);										
					is.close();
					
					int sheight =Math.min((bmp.getHeight() * width) / bmp.getWidth(), height); 
					bmp = Bitmap.createScaledBitmap(bmp, width, sheight, true);
					return new BitmapDrawable(res, bmp);
					
				} catch (FileNotFoundException e) {
					Log.d(getClass().getSimpleName(), e.getLocalizedMessage(), e);
				}								
				catch (IOException e) {
					Log.d(getClass().getSimpleName(), e.getLocalizedMessage(), e);
				}
				
				//Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
				//int sheight =Math.min((bmp.getHeight() * width) / bmp.getWidth(), height); 
				//bmp = Bitmap.createScaledBitmap(bmp, width, sheight, true);						
				return null;
				
			}else if(ext.equalsIgnoreCase(".mp4")|| 
					ext.equalsIgnoreCase(".avi") ||
					ext.equalsIgnoreCase(".mpg") ||
					ext.equalsIgnoreCase(".webm")||
					ext.equalsIgnoreCase(".flv")||
					ext.equalsIgnoreCase(".rmvb")){
				return res.getDrawable(R.drawable.movie);
			}else if(ext.equalsIgnoreCase(".html")||
					ext.equalsIgnoreCase(".htm")){
				return res.getDrawable(R.drawable.www);
			}else if(ext.equalsIgnoreCase(".apk")){
				return res.getDrawable(R.drawable.application);
			}else if(ext.equalsIgnoreCase(".docx")||
					ext.equalsIgnoreCase(".doc")){
				return res.getDrawable(R.drawable.wordprocessing);
			}else{
				return res.getDrawable(R.drawable.file);
			}
			
		}
		
		public  class FileViewModel extends NotifyPropertyChanged{
			File file;
		    Drawable image;
		    boolean loaded = false;
		    
			public FileViewModel(File file) {
				super();
				this.file = file;
			}

			public String getName(){
				String name = file.getName();
				return name;
			}
			
			public String getAbsolutePath(){
				return file.getAbsolutePath();
			}
			
			public File getFile(){
				return file;
			}
			
			public Drawable getImage(){
				if(image == null && !loaded){
						loader.postTask(new LoadTask() {
						
						@Override
						public Object runAsync(Object args) throws Exception {
							return FragmentDir.this.getImage(file);
						}
						
						@Override
						public void onComplete(Object result, Exception e) {
							if(e != null)
								return;
							
							Drawable dr = (Drawable) result;
							setImage(dr);
						}
					},null);		
				}
				
				return image;
			}
			
			public void setImage(Drawable image){
				this.image= image;
				loaded = true;
				onPropertyChange("Image");
			}
					
		}		
	}	
	
	
	
}
