/**
 * Copyright 2015 Red Hat, Inc., and individual contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.feedhenry.blank;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import cz.msebera.android.httpclient.message.BasicHeader;

import com.feedhenry.sdk.FH;
import com.feedhenry.sdk.FHActCallback;
import com.feedhenry.sdk.FHResponse;
import com.feedhenry.sdk.FHHttpClient;
import com.feedhenry.sdk.api.FHCloudRequest;
import com.feedhenry.sdk.exceptions.FHNotReadyException;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;
import android.widget.TextView;
import android.view.Menu;
import android.view.Window;
import android.app.Dialog;
import android.view.View.OnClickListener;
import android.view.View;

import java.util.ArrayList;

import org.json.fh.JSONException;
import org.json.fh.JSONObject;
import org.json.fh.JSONArray;

import android.widget.ProgressBar;


public class MainActivity extends AppCompatActivity implements MainInterface{

	private static final String TAG = MainActivity.class.getName();
	private ListAdapter itemsAdapter;
	private ArrayList<Item> items=new ArrayList<Item>();
	private ProgressBar spinner;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		// Find the toolbar view inside the activity layout
		Toolbar appToolbar = (Toolbar) findViewById(R.id.toolbar);
		appToolbar.setTitle("RHMAP To Do List");
		// Sets the Toolbar to act as the ActionBar for this Activity window.
		setSupportActionBar(appToolbar);

		// List
		itemsAdapter = new ListAdapter(this,this,items);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(itemsAdapter);

		spinner = (ProgressBar)findViewById(R.id.spinnerview);
		spinner.setVisibility(View.VISIBLE);
		connectToFH();
	}

	private void connectToFH () {
		FH.init(this, new FHActCallback() {
			@Override
			public void success(FHResponse fhResponse) {
				Log.d(TAG, "init - success");
				// populate item array
				getAllListItemsFromCloud();
			}

			@Override
			public void fail(FHResponse fhResponse) {
				Log.d(TAG, "init - fail");
				Log.e(TAG, fhResponse.getErrorMessage(), fhResponse.getError());

			}
		});

	}

	private void getAllListItemsFromCloud(){

		// Clear array of items
		items.clear();
		BasicHeader[] headers = new BasicHeader[1];
		headers[0] = new BasicHeader("contentType", "application/json");
		//The request should have a timeout of 25 seconds, 10 is the default
		FHHttpClient.setTimeout(25000);
		FHCloudRequest request = null;
		try {
			request = FH.buildCloudRequest("/todo", "GET",  headers, null);
			//the request will be executed asynchronously
			request.executeAsync(new FHActCallback() {
				@Override
				public void success(FHResponse res) {
					//the function to execute if the request is successful
					try{
						Log.v(TAG,"Response " + res.getJson().toString());
						populateAdapterArray(res.getJson());
						//process response data
					} catch(Exception e){
						Log.e(TAG, e.getMessage(), e);
					}
				}

				@Override
				public void fail(FHResponse res) {
					//the function to execute if the request is failed
					Log.e(TAG, res.getErrorMessage(), res.getError());
				}
			});
		} catch (FHNotReadyException e) {
			e.printStackTrace();
		}
	}


	private void populateAdapterArray(JSONObject dataList) {
		try {
			if(dataList.getInt("count")>0) {
				JSONArray listObject = dataList.getJSONArray("list");
				// Data Looks like this
				/*{"count":2,"list":[{"fields":{"name":"eggs"},"guid":"57bdf5bbe7eb84b773000148","type":"todo"},{"fields":{"name":"milk"},"guid":"57bef2df060e5fa74f0000a8","type":"todo"}]}*/
				for(int i =0; i<dataList.getInt("count");i++) {
					JSONObject recordData = listObject.getJSONObject(i);
					JSONObject fieldData = recordData.getJSONObject("fields");
					items.add(new Item(fieldData.getString("name"),recordData.getString("guid")));
				}
				spinner.setVisibility(View.GONE);
				itemsAdapter.notifyDataSetChanged();
			} else {
				Log.v(TAG,"Response has no records");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void sendNewItemToCloud(String newItem){
		BasicHeader[] headers = new BasicHeader[1];
		headers[0] = new BasicHeader("contentType", "application/json");
		//The request should have a timeout of 25 seconds, 10 is the default
		FHHttpClient.setTimeout(25000);
		FHCloudRequest request = null;
		try {
			request = FH.buildCloudRequest("/todo", "POST",  headers, new JSONObject().put("name",newItem));
			//the request will be executed asynchronously
			request.executeAsync(new FHActCallback() {
				@Override
				public void success(FHResponse res) {
					//the function to execute if the request is successful
					try{
						Log.v(TAG,"Response for Post" + res.getJson().toString());
						getAllListItemsFromCloud();
					} catch(Exception e){
						Log.e(TAG, e.getMessage(), e);
					}
				}

				@Override
				public void fail(FHResponse res) {
					//the function to execute if the request is failed
					Log.e(TAG, res.getErrorMessage(), res.getError());
				}
			});
		} catch (FHNotReadyException e) {
			e.printStackTrace();
		}

	}
	private void updateItemToCloud(String newItemName, Item item){
		BasicHeader[] headers = new BasicHeader[1];
		headers[0] = new BasicHeader("contentType", "application/json");
		//The request should have a timeout of 25 seconds, 10 is the default
		FHHttpClient.setTimeout(25000);
		FHCloudRequest request = null;
		try {
			request = FH.buildCloudRequest("/todo/" + item.getId(), "PUT",  headers, new JSONObject().put("name",newItemName));
			//the request will be executed asynchronously
			request.executeAsync(new FHActCallback() {
				@Override
				public void success(FHResponse res) {
					//the function to execute if the request is successful
					try{
						Log.v(TAG,"Response for Post" + res.getJson().toString());
						getAllListItemsFromCloud();
					} catch(Exception e){
						Log.e(TAG, e.getMessage(), e);
					}
				}

				@Override
				public void fail(FHResponse res) {
					//the function to execute if the request is failed
					Log.e(TAG, res.getErrorMessage(), res.getError());
				}
			});
		} catch (FHNotReadyException e) {
			e.printStackTrace();
		}

	}

	private void deleteItemFromCloud(String newItemName, Item item){
		BasicHeader[] headers = new BasicHeader[1];
		headers[0] = new BasicHeader("contentType", "application/json");
		//The request should have a timeout of 25 seconds, 10 is the default
		FHHttpClient.setTimeout(25000);
		FHCloudRequest request = null;
		try {
			request = FH.buildCloudRequest("/todo/" + item.getId(), "DELETE",  headers, null);
			//the request will be executed asynchronously
			request.executeAsync(new FHActCallback() {
				@Override
				public void success(FHResponse res) {
					//the function to execute if the request is successful
					try{
						Log.v(TAG,"Response for Post" + res.getJson().toString());
						getAllListItemsFromCloud();
					} catch(Exception e){
						Log.e(TAG, e.getMessage(), e);
					}
				}

				@Override
				public void fail(FHResponse res) {
					//the function to execute if the request is failed
					Log.e(TAG, res.getErrorMessage(), res.getError());
				}
			});
		} catch (FHNotReadyException e) {
			e.printStackTrace();
		}

	}

	private void createDialog (String action, final Item item) {
		final Dialog newDialog = new Dialog(this);
		newDialog.setContentView(R.layout.dialog);
		// Edit text
		final EditText userInput = (EditText) newDialog.findViewById(R.id.newitemtext);
		TextView title = (TextView) newDialog.findViewById(R.id.title);
		///OK Button
		Button okDialogButton = (Button) newDialog.findViewById(R.id.okbutton);
		// Cancel Button
		Button cancelDialogButton = (Button) newDialog.findViewById(R.id.cancelbutton);
		cancelDialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				newDialog.dismiss();
			}
		});

		if(action.matches("newitem")) {
			//newDialog.setTitle("Enter New Name");
			title.setText("Enter New Name");
			okDialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (userInput.getText().length() > 0) {
						sendNewItemToCloud(userInput.getText().toString());
						userInput.setText("");
					}
					newDialog.dismiss();
				}
			});
			newDialog.show();
		} else if(action.matches("updateitem")) {
			title.setText("Update Name");
			userInput.setText(item.getName());
			okDialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (userInput.getText().length() > 0) {
						updateItemToCloud(userInput.getText().toString(),item);
						userInput.setText("");
					}
					newDialog.dismiss();
				}
			});
			newDialog.show();
		}else if(action.matches("deleteitem")) {
			title.setText("Delete Name");
			userInput.setText(item.getName());
			userInput.setFocusable(false);
			okDialogButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (userInput.getText().length() > 0) {
						deleteItemFromCloud(userInput.getText().toString(), item);
						userInput.setText("");
					}
					newDialog.dismiss();
				}
			});
			newDialog.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.toolbarmenu, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.action_addnewitem:
				createDialog("newitem",null);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void editName(Item editItem) {
		createDialog("updateitem",editItem);
	}

	@Override
	public void deleteName(Item deleteItem) {
		createDialog("deleteitem",deleteItem);

	}

}
