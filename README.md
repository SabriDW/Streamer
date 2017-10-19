# Streamer

Streamer is an ExoPlayer2 wrapper that simplifies streaming videos with subtitles.


### Usage

1. Add `jitpack` to the project's repositories

        allprojects {
            repositories {
                jcenter()
                maven { url "https://jitpack.io" }
            }
        }
        
2. Add `Streamer` to your dependencies
    
        compile 'com.github.SniperDW:Streamer:1.0.0'
        
3. Use the library

        PlayerActivity.play(context, videoTitle, videoURL, subtitleURL);
        
        
