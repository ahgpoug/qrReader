package ahgpoug.qrreader.objects;

public class CombinedResult {
    private Task task;
    private String token;

    public CombinedResult(Task task, String token){
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
