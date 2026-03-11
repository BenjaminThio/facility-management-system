package src.utils;

import java.awt.FileDialog;
import java.awt.Frame;

public class FileExplorer {
    public class Path
    {
        String directory;
        String file;

        public Path(String directory, String file)
        {
            this.directory = directory;
            this.file = file;
        }

        public String getDirectory()
        {
            return this.directory;
        }

        public String getFile()
        {
            return this.file;
        }

        public String getFullPath()
        {
            return this.directory + this.file;
        }
    }
    private Path path = null;

    public FileExplorer() {
        Frame frame = new Frame();
        FileDialog fileDialog = new FileDialog(frame, "Select a File", 0);

        fileDialog.setVisible(true);

        String directory = fileDialog.getDirectory();
        String file = fileDialog.getFile();

        if (directory != null && file != null)
            this.path = new Path(directory, file);
        else
            this.path = null;

        frame.dispose();
    }

    public Path getPath()
    {
        return this.path;
    }

    public static void main(String[] args)
    {
        FileExplorer fileExplorer = new FileExplorer();

        if (fileExplorer.getPath() != null)
            System.out.println("Success! You selected: " + fileExplorer.getPath().getFullPath());
        else
            System.out.println("User cancelled the file selection.");
    }
}
