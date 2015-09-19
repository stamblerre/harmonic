package bigredhacks.harmonic;

import android.app.ActionBar;
import android.media.AudioManager;
import android.media.MediaPlayer;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.Track;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivityTest";
    private static final String MUSIC_LOG = "music-log.txt";
    private static final String SONG_FILE = "song.mp4";
    private static final String API_KEY = "6FTQBUXCB2QEJN2IU";

    private final AtomicBoolean listening = new AtomicBoolean(false);
    private final EchoNestAPI echoNest = new EchoNestAPI(API_KEY);

    private Song currentSong = null;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // Start listening to music
    public void startListening() {
        mediaRecorder = new MediaRecorder();
        final String songFile = getFilesDir().getAbsolutePath() + File.separator + SONG_FILE;
        Log.d(LOG_TAG, songFile);

        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        this.mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        this.mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        this.mediaRecorder.setOutputFile(songFile);
        this.listening.set(true);

        try {
            mediaRecorder.prepare();
        } catch (final IOException e) {
            Log.d(LOG_TAG, "Unable to prepare recording device: " + e.getMessage());
        }

        this.mediaRecorder.start();
        Log.d(LOG_TAG, "Started media recorder.");

        try {
            Thread.sleep(20000);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        mediaRecorder.stop();

        mediaRecorder.release();

        /*while (this.listening.get()) {
            /*try {
                Thread.sleep(2400000);
            }
            catch (final InterruptedException e) {
                Log.d(LOG_TAG, "Unable to sleep: " + e.getMessage());
            }*/
            //Log.d(LOG_TAG, "Listening!!!!");
            //processSong();
        //}
        processSong();

        Log.d(LOG_TAG, "Media recorder released!");
    }

    private void processSong() {
        try {
            Log.d(LOG_TAG, "Process song!");

            final File songFile = new File(getFilesDir().getAbsolutePath() + File.separator + SONG_FILE);
            Log.d(LOG_TAG, songFile.toString());
            final Track track = this.echoNest.uploadTrack(songFile);
            Log.d(LOG_TAG, "Post track");
            track.waitForAnalysis(30000);
            Log.d(LOG_TAG, "After analysis");
            if (track.getStatus() == Track.AnalysisStatus.COMPLETE) {
                final Song song = new Song(this.echoNest, track.getSongID());
                Log.d(LOG_TAG, "Song is: " + song.getTitle() + " by " + song.getArtistName());

                if (!song.equals(currentSong)) {
                    logNewSong(song);

                }
            } else {
                Log.d(LOG_TAG, "Ok something");
            }
            /*final Uri fileURI = Uri.fromFile(songFile);
            Log.d(LOG_TAG, songFile.toString());
            Log.d(LOG_TAG, fileURI.toString());
            Log.d(LOG_TAG, "Exists: " + songFile.exists());
            mediaPlayer = MediaPlayer.create(this, fileURI);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });*/
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
        /*catch (final InterruptedException e) {
            Log.d(LOG_TAG, "Unable to sleep: " + e.getMessage());
        }
        catch (final EchoNestException e) {
            Log.d(LOG_TAG, "Unable to analyze song: " + e.getMessage());
        }*/
    }
    private void logShit(){



    }
private void logNewSong(final Song song) {
    runOnUiThread(new Runnable() {
        @Override
        public void run() {
            String title = song.getTitle();
            String artist = song.getArtistName();

            LinearLayout v = (LinearLayout) findViewById(R.id.linearLayout1);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            );
            TextView newText = new TextView(v.getContext());
            newText.setText(title + " " + artist);

            v.addView(newText, params);


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