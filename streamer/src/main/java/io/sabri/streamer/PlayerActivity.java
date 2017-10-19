package io.sabri.streamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;

public class PlayerActivity extends Activity {

    private static final String VIDEO_NAME = "VIDEO_NAME";
    private static final String VIDEO_URL = "VIDEO_URL";
    private static final String SUBTITLE_URL = "SUBTITLE_URL";

    SimpleExoPlayerView exoPlayerView;
    SimpleExoPlayer exoPlayer;

    long duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        String videoURL = getIntent().getStringExtra(VIDEO_URL);
        String subtitleURL = getIntent().getStringExtra(SUBTITLE_URL);

        exoPlayerView = findViewById(R.id.exo_player_view);
        exoPlayer = createPlayer();
        exoPlayerView.setPlayer(exoPlayer);

        exoPlayer.prepare(prepareDataSource(videoURL, subtitleURL));

    }

    private SimpleExoPlayer createPlayer() {
        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);

        // 2. Create the player
        return ExoPlayerFactory.newSimpleInstance(this, trackSelector);
    }

    @NonNull
    private MergingMediaSource prepareDataSource(String videoURL, String subtitleURL) {
        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "STREAMER"), defaultBandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(Uri.parse(videoURL),
                dataSourceFactory, extractorsFactory, null, null);

        // Build the subtitle MediaSource.
        Format subtitleFormat = Format.createTextSampleFormat(
                null, // An identifier for the track. May be null.
                MimeTypes.APPLICATION_SUBRIP, // The mime type. Must be set correctly.
                0, // Selection flags for the track.
                "Arabic"); // The subtitle language. May be null.
        MediaSource subtitleSource = new SingleSampleMediaSource(
                Uri.parse(subtitleURL), dataSourceFactory, subtitleFormat, C.TIME_UNSET);
        // Plays the video with the sideloaded subtitle.
        MergingMediaSource mergedSource =
                new MergingMediaSource(videoSource, subtitleSource);

        return mergedSource;
    }


    public static void play(@NonNull Context context, @Nullable String name, @NonNull String videoURL, @Nullable String subtitleURL) {

        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(VIDEO_NAME, videoURL);
        intent.putExtra(VIDEO_URL, videoURL);
        intent.putExtra(SUBTITLE_URL, subtitleURL);

        context.startActivity(intent);

    }


    @Override
    protected void onResume() {
        super.onResume();
        exoPlayer.seekTo(duration);
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        exoPlayer.setPlayWhenReady(false);
        duration = exoPlayer.getCurrentPosition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exoPlayer.release();
    }
}
