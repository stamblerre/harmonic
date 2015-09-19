package bigredhacks.harmonic;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.echonest.api.v4.EchoNestAPI;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivityTest";
    private static final String MUSIC_LOG = "music-log.txt";
    private static final String SONG_FILE = "song.3gp";
    private static final String API_KEY = "";

    private boolean listening = false;
    //private String currentSong = null;
    private MediaRecorder mediaRecorder;
    private EchoNestAPI echoNest = new EchoNestAPI(API_KEY);

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Start listening to music
    public void startListening() {
        mediaRecorder = new MediaRecorder();
        final String songFile = getFilesDir().getAbsolutePath() + "/" + SONG_FILE;
        Log.d(LOG_TAG, songFile);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(songFile);
        listening = true;

        try {
            mediaRecorder.prepare();
        } catch (final IOException e) {
            Log.d(LOG_TAG, "Unable to prepare recording device: " + e.getMessage());
        }

        mediaRecorder.start();

        while (listening) {
            try {
                Thread.sleep(2400000);
            }
            catch (final InterruptedException e) {
                Log.d(LOG_TAG, "Unable to sleep: " + e.getMessage());
            }
            Log.d(LOG_TAG, "Listening!!!!");
            processSong();
        }

        mediaRecorder.release(); // TODO Will Android release this if the app is killed without calling stopListening()?

        Log.d(LOG_TAG, "Media recorder released!");
    }

    private void processSong() {
        try {
            Thread.sleep(20000);
        }
        catch (final InterruptedException e) {
            Log.d(LOG_TAG, "Unabled to sleep: " + e.getMessage());
        }

        final String songFile = getFilesDir().getAbsolutePath() + "/" + SONG_FILE;

        try {
            final InputStream fis = openFileInput(songFile);
            final DataInputStream dataInputStream = new DataInputStream(fis);

            String songData = dataInputStream.readUTF();
        }
        catch (final FileNotFoundException e) {
            Log.d(LOG_TAG, "Unable to find file: " + e.getMessage());
        }
        catch (final IOException e) {
            Log.d(LOG_TAG, "Unabled to read file: " + e.getMessage());
        }
    }

    public void postData() {
        // Create a new HttpClient and Post Header
        /*HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://www.yoursite.com/script.php");

        try {
            // Add your data
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("id", "12345"));
            nameValuePairs.add(new BasicNameValuePair("stringdata", "Hi"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }*/
    }

    // Stop listening to music and persist data
    private void stopListening() {
        Log.d(LOG_TAG, "Clicked stop!");
        listening = false;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_start:
                final CharSequence itemTitle = item.getTitle();
                final String startString = getResources().getString(R.string.start);
                final String stopString = getResources().getString(R.string.stop);
                if (itemTitle.equals(startString)) {
                    item.setTitle(stopString);
                    startListening();
                } else if (itemTitle.equals(stopString)) {
                    item.setTitle(startString);
                    stopListening();
                }
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}