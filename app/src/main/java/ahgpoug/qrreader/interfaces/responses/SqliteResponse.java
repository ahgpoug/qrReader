package ahgpoug.qrreader.interfaces.responses;

import java.io.File;

import ahgpoug.qrreader.objects.Task;

public interface SqliteResponse {
    void onSqliteResponseComplete(Task task, String token);
}
