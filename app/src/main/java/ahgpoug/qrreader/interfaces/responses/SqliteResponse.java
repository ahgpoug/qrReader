package ahgpoug.qrreader.interfaces.responses;

import ahgpoug.qrreader.objects.Task;

public interface SqliteResponse {
    void onSqliteResponseComplete(Task task, String token);
}
