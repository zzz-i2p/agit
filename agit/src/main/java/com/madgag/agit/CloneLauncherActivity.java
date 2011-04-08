/*
 * Copyright (c) 2011 Roberto Tyley
 *
 * This file is part of 'Agit' - an Android Git client.
 *
 * Agit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Agit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.madgag.agit;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static com.madgag.agit.GitOperationsService.cloneOperationIntentFor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.URIish;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CloneLauncherActivity extends Activity {
	private final static String TAG="CloneLauncherActivity";

	public static String EXTRA_TARGET_DIR="target-dir",EXTRA_SOURCE_URI="source-uri";
	
	private Button button;
	private CheckBox useDefaultGitDirLocationButton;
	private TextView warningTextView;
	private EditText gitDirEditText, cloneUrlEditText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clone_launcher);
        button = (Button) findViewById(R.id.GoCloneButton);
        gitDirEditText = (EditText) findViewById(R.id.GitDirEditText);
        warningTextView = (TextView) findViewById(R.id.GitDirWarning);
        cloneUrlEditText = (EditText) findViewById(R.id.CloneUrlEditText);
        useDefaultGitDirLocationButton = (CheckBox) findViewById(R.id.UseDefaultGitDirLocation);
		button.setOnClickListener(goCloneButtonListener);
		useDefaultGitDirLocationButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) { updateUIWithValidation(); }
		});
        TextWatcher watcher = new TextWatcher() {
			public void onTextChanged(CharSequence text, int arg1, int arg2, int arg3) {}
			
			public void beforeTextChanged(CharSequence text, int arg1, int arg2, int arg3) {}
			
			public void afterTextChanged(Editable gitDirEditText) { updateUIWithValidation(); }
		};
		cloneUrlEditText.addTextChangedListener(watcher);
		gitDirEditText.addTextChangedListener(watcher);
    }
    
    protected void updateUIWithValidation() {
    	boolean enableClone=true;
    	
    	URIish cloneUri=null;
    	try {
    		cloneUri=getCloneUri();
    	} catch (URISyntaxException e) {
    		enableClone=false;
    	}
    	
    	gitDirEditText.setEnabled(!useDefaultGitDirLocationButton.isChecked());
    	if (useDefaultGitDirLocationButton.isChecked() && cloneUri!=null) {
    		String currentGitDirText=gitDirEditText.getText().toString();
    		String requiredText = defaultRepoDirFor(cloneUri).getAbsolutePath();
    		if (!currentGitDirText.equals(requiredText))
    			gitDirEditText.setText(requiredText);
    	}
    	
		File f=new File(gitDirEditText.getText().toString());
		boolean goodGitDir=!f.exists();
		Log.d("Clone", f.getAbsolutePath()+"is goodGitDir="+goodGitDir);
		warningTextView.setVisibility(goodGitDir?INVISIBLE:VISIBLE);
		if (!goodGitDir) {
			enableClone=false;
		}
		
		button.setEnabled(enableClone);
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    	Intent intent = getIntent();
    	Log.d(TAG, "Starting with da "+intent);
    	if (intent!=null) {
    		setSourceUriFrom(intent);
    		setGitDirFrom(intent);
    	}
    }

	private void setSourceUriFrom(Intent intent) {
		String sourceUri= intent.getStringExtra(EXTRA_SOURCE_URI);
		if (sourceUri == null && intent.getData()!=null) { 
			//https://github.com/rtyley/jgit
			//git@github.com:rtyley/jgit.git RW
			//git://github.com/spearce/jgit.git R
			sourceUri = "git://github.com"+intent.getData().getPath()+".git";
		}
		if (sourceUri != null) {
			cloneUrlEditText.setText(sourceUri);
			Log.d(TAG, "Set cloneUrlEditText to "+sourceUri);
		}
	}

	private void setGitDirFrom(Intent intent) {
		String gitdir= intent.getStringExtra(EXTRA_TARGET_DIR);
		useDefaultGitDirLocationButton.setChecked(gitdir==null);
		if (gitdir!=null) { 
			gitDirEditText.setText(gitdir);
			Log.d(TAG, "Set gitdir to "+gitdir);
		}
	};
    
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.d(TAG, "onResume called");
    	updateUIWithValidation();
    }
    
    public URIish getCloneUri() throws URISyntaxException {
    	return new URIish(cloneUrlEditText.getText().toString());
    }
    
    public File getCheckoutLocation() {
    	return new File(gitDirEditText.getText().toString());
    }
    
    OnClickListener goCloneButtonListener = new OnClickListener() {
        public void onClick(View v) {
    		URIish uri;
    		try {
    			uri=getCloneUri();
    		} catch (URISyntaxException e) {
				Toast.makeText(v.getContext(), "bad dog", 10).show();
				return;
			}
    		File checkoutLocation=getCheckoutLocation();
    		try {
				wham(uri,checkoutLocation);
			} catch (Exception e) {
				Toast.makeText(v.getContext(), "ARRG: "+e, 10).show();
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

		private void wham(URIish uri, File repoDir) throws IOException, URISyntaxException {
			if (!repoDir.mkdirs()) {
				String message = "Couldn't create "+repoDir;
				Toast.makeText(CloneLauncherActivity.this, message, LENGTH_LONG).show();
				throw new IOException(message);
			}
    		
    		startService(cloneOperationIntentFor(uri, repoDir));
		}


    };
    
	private File defaultRepoDirFor(URIish uri) {
		File reposDir=new File(Environment.getExternalStorageDirectory(),"git-repos");
		try {
			return new File(reposDir, uri.getHumanishName());
		} catch (IllegalArgumentException e) {
			return new File(reposDir, "repo");
		}
	}

}