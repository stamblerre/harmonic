package bigredhacks.harmonic;

import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.Track;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivityTest";
    private static final String MUSIC_LOG = "music-log.txt";
    private static final String SONG_FILE = "song.mp4";
    private static final String API_KEY = "6FTQBUXCB2QEJN2IU";

    private AtomicBoolean listening = new AtomicBoolean(false);
    private final EchoNestAPI echoNest = new EchoNestAPI(API_KEY);

    private Song currentSong = null;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startListening() {
        this.listening.set(true);
        while (this.listening.get()) {
            Log.d(LOG_TAG, "I'm listening!");
            listen();
            try {
                Thread.sleep(90000);
            }
            catch (final InterruptedException e) {
                Log.d(LOG_TAG, "Error sleeping.");
            }
        }
    }

    private void playSong(final File songFile) {
        Log.d(LOG_TAG, "Let's play the song!");
        final Uri fileURI = Uri.fromFile(songFile);
        this.mediaPlayer = MediaPlayer.create(this, fileURI);
        this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mPlayer) {
                mPlayer.start();
                Log.d(LOG_TAG, "Playing music!");
            }
        });
    }

    /** Start listening to music */
    private void listen() {
        Log.d(LOG_TAG, "In listen!");
        this.mediaRecorder = new MediaRecorder();
        final String songFile = getFilesDir().getAbsolutePath() + File.separator + SONG_FILE;
        Log.d(LOG_TAG, songFile);

        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        this.mediaRecorder.setOutputFile(songFile);

        try {
            this.mediaRecorder.prepare();
        }
        catch (final IOException e) {
            Log.d(LOG_TAG, "Unable to prepare recording device: " + e.getMessage());
        }

        this.mediaRecorder.start();
        Log.d(LOG_TAG, "Started media recorder.");

        try {
            Thread.sleep(20000);
        }
        catch (final InterruptedException e) {
            Log.d(LOG_TAG, "Unable to sleep for 2 seconds: " + e.getMessage());
        }

        this.mediaRecorder.stop();
        this.mediaRecorder.release();
        this.mediaRecorder = null;

        Log.d(LOG_TAG, "Media recorder released!");

        //processSong();
        playSong(new File(songFile));
    }

    private void processSong() {
        try {
            Log.d(LOG_TAG, "Processing song.");
            final File songFile = new File(getFilesDir().getAbsolutePath() + File.separator + SONG_FILE);

            final Track track = this.echoNest.uploadTrack(songFile);
            final Track.AnalysisStatus analysisStatus = track.waitForAnalysis(30000);
            if (analysisStatus == Track.AnalysisStatus.COMPLETE) {
                final Song song = new Song(this.echoNest, track.getSongID());
                Log.d(LOG_TAG, "Song is: " + song.getTitle() + " by " + song.getArtistName());
                if (!song.equals(currentSong)) {
                    logNewSong(song);
                }
            } else {
                Log.d(LOG_TAG, "Ok something");
            }
        }
        catch (final EchoNestException e) {
            Log.d(LOG_TAG, "Error with EchoNestAPI: " + e.getMessage());
        }
        catch (final IOException e) {
            Log.d(LOG_TAG, "Unable to load file: " + e.getMessage());
        }
    }

    private void logNewSong(final Song song) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final String title = song.getTitle();
                final String artist = song.getArtistName();

                final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout1);

                final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                );
                final TextView newText = new TextView(linearLayout.getContext());
                newText.setText(title + " " + artist);

                linearLayout.addView(newText, params);
            }
        });
    }

    // Stop listening to music and persist data
    private void stopListening() {
        Log.d(LOG_TAG, "Clicked stop!");
        this.listening.set(false);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
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
                    new Thread(new Runnable() {
                        public void run(){
                            startListening();
                        }
                    }).start();
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