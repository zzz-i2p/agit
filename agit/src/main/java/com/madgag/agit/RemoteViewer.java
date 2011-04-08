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

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.jgit.transport.RemoteConfig;

import android.content.Intent;
import android.os.Bundle;

public class RemoteViewer extends RepositoryActivity {
    
    public static Intent remoteViewerIntentFor(File gitdir, RemoteConfig remote) {
		return new GitIntentBuilder("git.view.REMOTE").gitdir(gitdir).remote(remote).toIntent();
	}

	private static final String TAG = "RemoteViewer";
	
	private RemoteConfig remote;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.remote_view);
		
		try {
			remote = new RemoteConfig(repo().getConfig(), getIntent().getStringExtra("remote"));
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
}
