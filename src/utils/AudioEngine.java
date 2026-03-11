package src.utils;

import javax.sound.sampled.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AudioEngine {
    public class PlayingSound {
        private final byte[] audioData;
        private int cursorPosition;

        public PlayingSound(byte[] audioData) {
            this.audioData = audioData;
            this.cursorPosition = 0;
        }

        public byte[] getNextChunk(int chunkSize) {
            if (cursorPosition >= audioData.length) {
                return null;
            }
            int bytesRemaining = audioData.length - cursorPosition;
            int actualSize = Math.min(chunkSize, bytesRemaining);

            byte[] chunk = new byte[actualSize];
            System.arraycopy(audioData, cursorPosition, chunk, 0, actualSize);
            cursorPosition += actualSize;

            return chunk;
        }
    }

    private SourceDataLine line;
    private final List<PlayingSound> activeSounds = new CopyOnWriteArrayList<>();
    private boolean isRunning = true;
    public static byte[] selectSound = AudioEngine.loadRawAudioData("sounds/snd_select.wav");
    public static byte[] squeakSound = AudioEngine.loadRawAudioData("sounds/snd_squeak.wav");
    public static AudioEngine engine = new AudioEngine();

    public static byte[] loadRawAudioData(String filePath) {
        try {
            File file = new File(filePath);
            AudioInputStream rawStream = AudioSystem.getAudioInputStream(file);
            
            AudioFormat targetFormat = new AudioFormat(44100, 16, 2, true, false);
            AudioInputStream convertedStream = AudioSystem.getAudioInputStream(targetFormat, rawStream);
            
            return convertedStream.readAllBytes(); 
        } catch (Exception e) {
            System.err.println("Failed to load audio file: " + filePath);
            e.printStackTrace();
            return null;
        }
    }

    public void startEngine() throws Exception {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, false);
        line = AudioSystem.getSourceDataLine(format);
        line.open(format, 4096);
        line.start();

        Thread mixerThread = new Thread(() -> {
            byte[] finalMixBuffer = new byte[1024]; 
            
            while (isRunning) {
                if (activeSounds.isEmpty()) {
                    line.write(new byte[1024], 0, 1024);
                    continue;
                }

                int[] mixingMathBuffer = new int[finalMixBuffer.length / 2];

                for (PlayingSound sound : activeSounds) {
                    byte[] chunk = sound.getNextChunk(finalMixBuffer.length);
                    
                    if (chunk == null) {
                        activeSounds.remove(sound);
                        continue;
                    }

                    for (int i = 0, j = 0; i < chunk.length - 1; i += 2, j++) {
                        short sample = (short) ((chunk[i + 1] << 8) | (chunk[i] & 0xFF));
                        mixingMathBuffer[j] += sample;
                    }
                }

                for (int i = 0, j = 0; j < mixingMathBuffer.length; i += 2, j++) {
                    int mixedSample = mixingMathBuffer[j];

                    if (mixedSample > Short.MAX_VALUE) mixedSample = Short.MAX_VALUE;
                    if (mixedSample < Short.MIN_VALUE) mixedSample = Short.MIN_VALUE;

                    finalMixBuffer[i] = (byte) (mixedSample & 0xFF);         
                    finalMixBuffer[i + 1] = (byte) ((mixedSample >> 8) & 0xFF); 
                }

                line.write(finalMixBuffer, 0, finalMixBuffer.length);
            }
        });
        
        mixerThread.setDaemon(true);
        mixerThread.setPriority(Thread.MAX_PRIORITY);
        mixerThread.start();
    }

    public void playSound(byte[] preloadedAudioBytes) {
        if (preloadedAudioBytes != null) {
            activeSounds.add(new PlayingSound(preloadedAudioBytes));
        }
    }

    public static void init() throws IOException {
        try
        {
            engine.startEngine();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }
    }
}