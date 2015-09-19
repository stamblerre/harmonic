package bigredhacks.harmonic;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.Track;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivityTest";
    private static final String MUSIC_LOG = "music-log.txt";
    private static final String SONG_FILE = "song.3gp";
    private static final String API_KEY = "6FTQBUXCB2QEJN2IU";

    private boolean listening = false;
    private Song currentSong = null;
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

            final File songFile = new File(getFilesDir().getAbsolutePath() + "/" + SONG_FILE);
            final Track track = echoNest.uploadTrack(songFile);

            track.waitForAnalysis(30000);
            if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
                final Song song = new Song(echoNest, track.getSongID());
                Log.d(LOG_TAG, "Song is: " + song.getTitle() + " by " + song.getArtistName());

                if (!song.equals(currentSong)) {
                    logNewSong();
                }
            }
        }
        catch (final InterruptedException e) {
            Log.d(LOG_TAG, "Unable to sleep: " + e.getMessage());
        }
        catch (final IOException e) {
            Log.d(LOG_TAG, "Unable to read file: " + e.getMessage());
        }
        catch (final EchoNestException e) {
            Log.d(LOG_TAG, "Unable to analyze song: " + e.getMessage());
        }
    }

    private void logNewSong() {
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