package ahgpoug.qrreader.objects;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String taskName;
    private String groupName;
    private String expDate;


    public Task(String id, String taskName, String groupName, String expDate){
        this.id = id;
        this.taskName = taskName;
        this.groupName = groupName;
        this.expDate = expDate;
    }

    public Task(){

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }
}
