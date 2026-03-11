package test;

import javax.sound.sampled.*;
import java.io.File;

public class AudioPlayer {
    private Clip clip;

    public AudioPlayer(String filePath) {
        try {
            File file = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void play() {
        if (clip != null) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public static void main(String[] args) {
        AudioPlayer selectSound = new AudioPlayer("sounds/snd_select.wav");

        selectSound.play();
    }
}