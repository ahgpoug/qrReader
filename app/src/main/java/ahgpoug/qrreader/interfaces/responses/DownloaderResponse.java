package ahgpoug.qrreader.interfaces.responses;

import java.io.File;

public interface DownloaderResponse {
    void onDownloadFinish(File output);
}
