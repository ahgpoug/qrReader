package ahgpoug.qrreader.objects;

import java.io.Serializable;

public class CombinedTask implements Serializable {
    private Task task;
    private String token;

    public CombinedTask(Task task, String token){
        this.task = task;
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Task getTask() {
        return task;
    }
}
