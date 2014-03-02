package com.example.pear;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class MainFragment extends Fragment {

	private static final int IO_BUFFER_SIZE = 4 * 1024;
	private static final String TAG = "MainFragment";
	private TextView userInfoTextView;
	private GraphUser user1;

	private UiLifecycleHelper uiHelper;

	private Identifiers ids = new Identifiers();
	private ArrayList<String> IDs;
	private ArrayList<String> males;
	private ArrayList<String> females;
	private RequestBatch requestBatch;
	private boolean yes = true;

	// Test for now; change for info we want
	private String buildUserInfoDisplay(GraphUser user) {
		StringBuilder userInfo = new StringBuilder("");

		// Example: typed access (name)
		// - no special permissions required
		// userInfo.append(String.format("Name: %s\n\n", user.getName()));
		userInfo.append(String.format("JSON: %s\n\n", user.getInnerJSONObject()
				.toString()));
		/*
		 * // Example: typed access (birthday) // - requires user_birthday
		 * permission userInfo.append(String.format("Birthday: %s\n\n",
		 * user.getBirthday()));
		 */

		return userInfo.toString();
	}

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	// Hmm... Final!
	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (state.isOpened()) {
			Log.i(TAG, "Logged in...");
			userInfoTextView.setVisibility(View.VISIBLE);

			// Request user data and show the results
			Request r1 = Request.newMeRequest(session,
					new Request.GraphUserCallback() {

						@Override
						public void onCompleted(GraphUser user,
								Response response) {
							if (user != null) {
								// Display the parsed user info
								userInfoTextView
										.setText(buildUserInfoDisplay(user));
								user1 = user;
								step2(session);
							}
						}
					});

			r1.executeAsync();
		}

		// else if
		else if (state.isClosed()) {
			Log.i(TAG, "Logged out...");
			userInfoTextView.setVisibility(View.INVISIBLE);
			session.closeAndClearTokenInformation();
		}
	}

	public void step2(final Session session) {
		ArrayList<Request> requests = new ArrayList<Request>();
		Request r2 = new Request(session, "/me/friends", null, HttpMethod.GET,
				new Request.Callback() {
					public void onCompleted(Response response) {
						// IDs = new ArrayList<String>();
						ids.clearIds();

						Log.i("Tiny success...", response.toString());
						int limit = 0;
						JSONArray jArray;
						if (response.getGraphObject() != null) {
							try {
								jArray = response.getGraphObject()
										.getInnerJSONObject()
										.getJSONArray("data");
								Log.i("Minor success", "Woohoo");
								limit = jArray.length();
							} catch (JSONException e) {

							}
						}

						if (limit > 0) {
							for (int index = 0; index < limit; index++) {

								try {
									String id = response.getGraphObject()
											.getInnerJSONObject()
											.getJSONArray("data")
											.getJSONObject(index)
											.getString("id");

									Log.i("ID: ", id);

									ids.addId(id);
									Log.i("Size: ", Integer.toString(ids
											.getIds().size()));

								} catch (JSONException je) {
									Log.e("oops", "JSONException");
								}
							}
						}
						if (yes) {
							step3(session);
						}
						yes = false;
					}

				});
		requests.add(r2);
		requestBatch = new RequestBatch(requests);
		requestBatch.executeAsync();
	}

	public void step3(final Session session) {
		requestBatch.clear();
		ArrayList<Request> requests = new ArrayList<Request>();
		males = new ArrayList<String>();
		females = new ArrayList<String>();
		Log.i("Size now: ", Integer.toString(ids.getIds().size()));
		String aT = session.getAccessToken();

		// final??
		if (ids.getIds().size() > 0) {

			Log.i("Something!", "Something!");

			for(int j = 0; j < 20; j++) {
				int random = (int)(Math.random() * ids.getIds().size());

				String gender = "";

				URLReader reader = new URLReader();
				try {
					gender = reader.read("https://graph.facebook.com/" + ids.getIds().get(random)
							+ "?fields=gender");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Log.i("Gender: ", gender.substring(11, 15));

				if ((gender.substring(11, 15)).equals("male")) {
					males.add(ids.getIds().get(random));
					Log.i("Male ", "added!");
				} else if ((gender.substring(11, 17)).equals("female")) {
					females.add(ids.getIds().get(random));
					Log.i("Female ", "added!");
				}
				step4(session);

			}

		}
	}

	public void step4(final Session session) {
		if (males.size() > 0) {
			int index1 = (int) ((males.size() - 1) * Math.random());
			String imageMale = "http://graph.facebook.com/" + males.get(index1)
					+ "/picture";

			Bitmap man = loadBitmap(imageMale);

			ImageView imgView;
			imgView = (ImageView) this.getActivity().findViewById(
					R.id.imageView1);
			imgView.setImageBitmap(man);
		}
		if (females.size() > 0) {

			int index2 = (int) ((females.size() - 1) * Math.random());
			String imageFemale = "http://graph.facebook.com/"
					+ females.get(index2) + "/picture";

			Bitmap woman = loadBitmap(imageFemale);

			ImageView imgView2;
			imgView2 = (ImageView) this.getActivity().findViewById(
					R.id.imageView2);
			imgView2.setImageBitmap(woman);

		}

	}

	public static Bitmap loadBitmap(String url) {

		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;

		try {
			in = new BufferedInputStream(new URL(url).openStream(),
					IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);

			int n;
			byte[] buffer = new byte[1024];
			while ((n = in.read(buffer)) > -1) {
				out.write(buffer, 0, n);
			}

			final byte[] data = dataStream.toByteArray();
			BitmapFactory.Options options = new BitmapFactory.Options();

			out.close();
			out.flush();

			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					options);
		} catch (IOException e) {

		}
		return bitmap;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_main, container, false);

		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);

		LoginButton authButton = (LoginButton) view
				.findViewById(R.id.authButton);
		authButton.setFragment(this);
		authButton.setReadPermissions(Arrays.asList("basic_info",
				"user_friends"));

		userInfoTextView = (TextView) view.findViewById(R.id.userInfoTextView);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		// For scenarios where the main activity is launched and user
		// session is not null, the session state change notification
		// may not be triggered. Trigger it if it's open/closed.
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
	}
}
