/*
   Copyright 2010 Roger Heim

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.aremaitch.codestock2010;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.XMLReader;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import com.aremaitch.codestock2010.repository.DataHelper;
import com.aremaitch.codestock2010.repository.Session;

public class DisplaySessionDetailsActivity extends Activity {

//	private boolean _gotSession = false;
	TextView sessiontitletv = null;
	TextView sessionwhenwhere = null;
	TextView synopsistv = null;
	TextView speakernametv = null;
	TextView speakerbiotv = null;
	TextView presentedby = null;
	ScrollView scroller = null;
	
	TransformFilter twitterFilter = null;
	Pattern twitterPattern = null;
	String twitterScheme = "http://twitter.com/";	// I can't find a standard for calling a default Twitter activity
	final String photoCachePath = "com.aremaitch.codestock2010/speakerphotocache/";
	
	//	Orientation change fires onPause(), onCreate(), onStart()
	//		(there may be a different layout for landscape vs. portrait so Android will
	//			need the layout re-inflated.)
	//	Leaving this activity fires onPause() then onStop()
	//	Returning to this activity fires onStart()
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		twitterFilter = new TransformFilter() {
			
			@Override
			public String transformUrl(Matcher match, String url) {
				return match.group(1);
			}
		};
		twitterPattern = Pattern.compile("@([A-Za-z0-9_-]+)");
		
		setContentView(R.layout.session_details);
	}
	
	@Override
	protected void onStart() {
		// There are clickable HTML links in most speaker bio's. Clicking one will fire up
		// the browser. Back will then return here. If we alreay have the session data, do not
		// get it again.
		super.onStart();
		
//		if (!_gotSession) {
			sessiontitletv = (TextView) findViewById(R.id.session_details_title);
			sessionwhenwhere = (TextView) findViewById(R.id.session_details_when_and_where);
			synopsistv = (TextView) findViewById(R.id.session_details_synopsis_text);
			speakernametv = (TextView) findViewById(R.id.session_details_speaker_name);
			speakerbiotv = (TextView) findViewById(R.id.session_details_speaker_bio);
			sessiontitletv.setText("");
			synopsistv.setText("");
			speakernametv.setText("");
			speakerbiotv.setText("");

			presentedby = (TextView) findViewById(R.id.session_details_presentedby_label);
			scroller = (ScrollView) findViewById(R.id.session_details_scroller);
			Intent i = getIntent();
			long sessionid = i.getLongExtra(getString(R.string.session_details_intent_sessionid), -1);
			if (sessionid >= 0) {
				
				Session s = getSessionInfo(sessionid);
				
				displaySessionInfo(s);
//				_gotSession = true;
				
//				QuerySessionDetails q = new QuerySessionDetails(sessionid);
//				q.execute();
				
			}
//		}
	}
	
	//	Since this is coming from a local database and is fairly quick we can probably
	//	get away with not using an AsyncTask.
	private Session getSessionInfo(long sessionid) {
		DataHelper dh = new DataHelper(this);
		Session s = null;
		try {
			s = dh.getSession(sessionid);
		} finally {
			dh.close();
		}
		return s;
	}
	
	private void displaySessionInfo(Session s) {
		
		String fullName = s.getSpeaker().getSpeakerName();
		if (!TextUtils.isEmpty(s.getSpeaker().getTwitterHandle()) && !s.getSpeaker().getTwitterHandle().equalsIgnoreCase("null")) {
			fullName = fullName + " (@" + s.getSpeaker().getTwitterHandle() + ")";
		}
		speakernametv.setText(fullName);
		Linkify.addLinks(speakernametv, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);		// not phone numbers; too many false positives
		Linkify.addLinks(speakernametv, twitterPattern, twitterScheme, null, twitterFilter);
		
		sessiontitletv.setText(s.getSessionTitle());
//		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT);
		SimpleDateFormat df = new SimpleDateFormat(getString(R.string.standard_where_when_format_string));	//  Sat, June 26 2010 8:30 AM
		
		
		sessionwhenwhere.setText(df.format(s.getStartDate().getTime()) + " Rm:" + s.getRoom());
		
		if (TextUtils.isEmpty(s.getSynopsis()) || s.getSynopsis().equalsIgnoreCase("null")) {
			synopsistv.setText(Html.fromHtml("<i>To be announced</i>"));
		} else {
			synopsistv.setText(Html.fromHtml(hackText(s.getSynopsis()), 
					null, 
					new MyTagHandler()));
		}
		
		
		//	From http://mgmblog.com/2010/06/04/setcompounddrawable-to-add-drawables-to-textview/
		//	This is how to add a drawable to a TextView (Android calls it a compound drawable.
		//	Basically you get the image as a drawable then call setBounds() to define a bounding rectangle
		//	around it.
		//	Then call setCompoundDrawables() on the TextView.
		
		if (TextUtils.isEmpty(s.getSpeaker().getSpeakerBio()) || s.getSpeaker().getSpeakerBio().equalsIgnoreCase("null")) {
			speakerbiotv.setText(Html.fromHtml("<i>Speaker bio not provided</i>"));
		} else {
			speakerbiotv.setText(Html.fromHtml(
					hackText(s.getSpeaker().getSpeakerBio())
					, null, new MyTagHandler()));
		}
		
		String speakerPhotoUrl = s.getSpeaker().getSpeakerPhotoUrl();
		if (TextUtils.isEmpty(speakerPhotoUrl) || speakerPhotoUrl.equalsIgnoreCase("null")) {
			//	No photo
		} else {
			GetSpeakerPhoto gsp = new GetSpeakerPhoto(speakerPhotoUrl);
			gsp.execute();
		}
		Linkify.addLinks(speakerbiotv, Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
		
		//	This technique for Linkifying Twitter comes from http://www.indelible.org/ink/android-linkify/
		
		Linkify.addLinks(speakerbiotv, twitterPattern, twitterScheme, null, twitterFilter);
//		_gotSession = true;

	}
	
	
	private void displaySpeakerPhoto(Drawable photo) {
//		photo.setBounds(0, 0, 60, 60);
//		presentedby.setCompoundDrawables(null, null, null, photo);
		presentedby.setCompoundDrawablesWithIntrinsicBounds(null, null, null, photo);
	}
	
	private String hackText(String textToHack) {
		//	Change HtmlEncoded text (&lt; to < and &gt; to >) so that the tag handler will get called
		//	for <pre> tags.
		//	Also, fromHtml() handles the <em> and <strong> tag incorrectly. <em> should be italic while <strong>
		//	should be bold. The source of Html.java shows they have it reversed.
		//	Replace all occurences of 'em>' with 'i>' and all occurences of 'strong>' with 'b>'
		return textToHack
			.replaceAll("&lt;", "<")
			.replaceAll("&gt;", ">")
			.replaceAll("em>", "i>")
			.replaceAll("strong>", "b>"); 
	}
	
	
	
	private class MyTagHandler implements Html.TagHandler {

		boolean inUL = false;		// true if we are processing an un-numbered list
		boolean inOL = false;		// true if we are processing an ordered (numbered) list
		int lastOLItem = 0;			// last number used while generating an ordered list
		
		@Override
		public void handleTag(boolean opening, String tag, Editable output,
				XMLReader xmlReader) {
			//	So what am I supposed to do with un-known html tags?
			
			//	Handle '<code>' the same as '<pre>'
			if (tag.equalsIgnoreCase("pre") || tag.equalsIgnoreCase("code")) {
				if (opening) {
					startPre(output);
				} else {
					endPre(output);
				}
			} else if (tag.equalsIgnoreCase("meta")) {
				if (opening) {
					startNullOut(output);
				} else {
					endNullOut(output);
				}
			} else if (tag.equalsIgnoreCase("style")) {
				if (opening) {
					startNullOut(output);
				} else {
					endNullOut(output);
				}
			} else if (tag.equalsIgnoreCase("ui")) {
				if (opening) {
					startUnNumberedList(output);
				} else {
					endUnNumberedList(output);
				}
			} else if (tag.equalsIgnoreCase("ol")) {
				if (opening) {
					startOrderedList(output);
				} else {
					endOrderdList(output);
				}
			} else if (tag.equalsIgnoreCase("li")) {
				if (opening) {
					startListItem(output);
				} else {
					endListItem(output);
				}
			}
		}
		
		private void startUnNumberedList(Editable output) {
			inUL = true;
			output.append("\n");
		}
		
		private void endUnNumberedList(Editable output) {
			inUL = false;
			output.append("\n");
		}

		private void startOrderedList(Editable output) {
			inOL = true;
			lastOLItem = 0;
			output.append("\n");
		}
		
		private void endOrderdList(Editable output) {
			inOL = false;
			output.append("\n");
		}
		
		private void startListItem(Editable output) {
			if (inUL) {
				output.append("\t * ");
			} else if (inOL) {
				output.append("\t" + String.valueOf(++lastOLItem) + ". ");
			}
		}
		
		private void endListItem(Editable output) {
			output.append("\n");
		}
		
		private void startNullOut(Editable output) {
			int len = output.length();
			output.setSpan(new SpanMarker(), len, len, Spannable.SPAN_MARK_MARK);
		}
		
		private void endNullOut(Editable output) {
			int len = output.length();
			Object obj = getLast(output, SpanMarker.class);
			int where = output.getSpanStart(obj);
			output.removeSpan(obj);
			if (where != len) {
				output.delete(where, len);		// This should strip out the tag.
			}
		}
		
		//	These two methods were pieced together from code in Html.java.
		//	At the start of the pre tag, insert a custom Font object as a marker. 
		private void startPre(Editable output) {
			int len = output.length();
			output.setSpan(new Font("Black", "Courier"), len, len, Spannable.SPAN_MARK_MARK);
		}
		
		//	At the end of the pre tag, find the custom Font object we inserted as a marker.
		//	Remove it, then add a new TypefaceSpan for monospace at the tag's start and end.
		private void endPre(Editable output) {
			int len = output.length();
			Object obj = getLast(output, Font.class);
			int where = output.getSpanStart(obj);
			
			output.removeSpan(obj);
			if (where != len) {
				output.setSpan(new TypefaceSpan("monospace"), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
			}
		}
		
		//	This method was lifted from Html.java. It assumes that the last object returned from getSpans() will be the
		//	most recently added.
		@SuppressWarnings("unchecked")
		private Object getLast(Spanned text, Class kind) {
			Object[] objs = text.getSpans(0, text.length(), kind);
			if (objs.length == 0) {
				return null;
			} else {
				return objs[objs.length - 1];
			}
		}
		//	This class was swiped from the built-in Html.java.
		private class Font {
			@SuppressWarnings("unused")
			public String _color;
			@SuppressWarnings("unused")
			public String _face;
			public Font(String color, String face) {
				_color = color;
				_face = face;
			}
		}
		
		private class SpanMarker {
			public SpanMarker() {
				;
			}
		}
		
	}
	
	private class GetSpeakerPhoto extends AsyncTask<Void, Void, Drawable> {
		private String _urlString;

		public GetSpeakerPhoto(String urlString) {
			_urlString = urlString;
		}
		
		@Override
		protected Drawable doInBackground(Void... params) {
			Drawable photo = null;
			
			try {
				URL theUrl = new URL(_urlString);
				if (isSpeakerPhotoCached(stripFileName(theUrl.getFile()))) {
					photo = getSpeakerPhotoFromCache(stripFileName(theUrl.getFile()));
				} else {
					
					photo = downloadSpeakerPhoto(theUrl);
				}
			} catch (MalformedURLException e) {
				photo = null;
				Log.e(getString(R.string.logging_tag), "Bad speaker photo url");
			}
			return photo;
		}
		
		
		private String stripFileName(String longFileName) {
			//	Takes /Assets/Speakers/guid.jpg and just returns guid.jpg
			return longFileName.substring(longFileName.lastIndexOf('/') + 1);
		}
		
		private Drawable getSpeakerPhotoFromCache(String speakerPhotoName) {
			File photoPath = getCachedPhotoName(speakerPhotoName);
			if (photoPath != null) {
				return Drawable.createFromPath(photoPath.getAbsolutePath());
			} else
				return null;

		}
		

		@Override
		protected void onPostExecute(Drawable result) {
			displaySpeakerPhoto(result);
		}
		
		/**
		 * Returns a File object containing the full path to the cached speaker photo.<br>
		 * Note the File object may not yet exist.
		 * @param speakerPhotoName	The simple name of the photo file.
		 * @return	The File object.
		 */
		private File getCachedPhotoName(String speakerPhotoName) {
			File cachedFile = null;
			
			if (isExternalStorageReady()) {
				//	The external storage is mounted and ready to go.
				File cacheDirectory = new File(Environment.getExternalStorageDirectory(), photoCachePath);
				boolean result = createPhotoCacheDirectoryIfNecessary(cacheDirectory);
				if (result) {
					cachedFile = new File(cacheDirectory, speakerPhotoName);
				}
			}
			return cachedFile;
		}
		
		private boolean isExternalStorageReady() {
			String state = Environment.getExternalStorageState();
			if (Environment.MEDIA_MOUNTED.equals(state)) {
				return true;
			}
			return false;
		}
		
		private boolean isSpeakerPhotoCached(String speakerPhotoName) {
			boolean result = false;
		
			result = getCachedPhotoName(speakerPhotoName).exists();
			return result;
		}
		
		private boolean createPhotoCacheDirectoryIfNecessary(File cacheDirectory) {
			boolean result = true;
			if (!cacheDirectory.exists()) {
				result = cacheDirectory.mkdirs();
			}
			return result;
		}
		
		private Drawable downloadSpeakerPhoto(URL theUrl) {
			InputStream is = null;
			BufferedInputStream bis = null;
			Drawable photo = null;
			
			try {
				URLConnection cn = theUrl.openConnection();
				int contentLength = cn.getContentLength();
				byte[] data = new byte[contentLength];
				
				is = cn.getInputStream();
				bis = new BufferedInputStream(is);
				int bytesRead = 0;
				int offset = 0;
				while (offset < contentLength) {
					bytesRead = bis.read(data, offset, data.length - offset);
					if (bytesRead == -1)
						break;
					offset += bytesRead;
				}
				
				//	If the external storage is ready, save the photo to the cache and return
				//	the photo from the cache.
				//	If the external storage is not ready, convert the byte array directly to a
				//	drawable.
				if (isExternalStorageReady()) {
					savePhotoToCache(stripFileName(theUrl.getFile()), data);
					photo = getSpeakerPhotoFromCache(stripFileName(theUrl.getFile()));
				} else {
					photo = Drawable.createFromStream(new ByteArrayInputStream(data), "session_details_speaker_photo");
				}
				
			} catch (SocketTimeoutException e) {
				Log.i(getString(R.string.logging_tag), "Timeout getting speaker photo");
			} catch (Exception e) {
				Log.e(getString(R.string.logging_tag), "Error getting speaker photo");
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (Exception e) {
					}
				}
				if (is != null) {
					try {
						is.close();
					} catch (Exception e) {
					}
				}
			}
			return photo;
		}
		
		private void savePhotoToCache(String fileName, byte[] data) {
			File cacheFile = getCachedPhotoName(fileName);
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(cacheFile);
				out.write(data);
				out.flush();
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (Exception e) {
					}
				}
				
			}
					
		}
	}
	
	public class QuerySessionDetails extends AsyncTask<Void, Void, Session> {

		long _sessionid = -1;
		ProgressDialog progress;
		
		public QuerySessionDetails(long sessionid) {
			_sessionid = sessionid;
		}

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(DisplaySessionDetailsActivity.this, "CodeStock 2010", "Getting session...");
		}
		
		@Override
		protected Session doInBackground(Void... params) {
			DataHelper dh = new DataHelper(DisplaySessionDetailsActivity.this);
			Session s = null;
			try {
				s = dh.getSession(_sessionid);
			} finally {
				dh.close();
			}
			return s;
		}
		
		@Override
		protected void onPostExecute(Session result) {
			progress.dismiss();
			displaySessionInfo(result);
		}

	}


	
}
