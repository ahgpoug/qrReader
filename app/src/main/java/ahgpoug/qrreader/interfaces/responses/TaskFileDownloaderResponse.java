package ahgpoug.qrreader.interfaces.responses;

import java.io.File;

public interface TaskFileDownloaderResponse {
    void onTaskFileDownloadFinish(File output);
}
