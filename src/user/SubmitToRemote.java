package user;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.concurrent.Task;

public class SubmitToRemote extends Task<WorkRecord> {

    private BlockingQueue<WorkRecord> workrecords = new LinkedBlockingQueue<WorkRecord>();

    @Override
    protected WorkRecord call() throws Exception {
        // TODO Auto-generated method stub
        System.out.println("远程提交日志功能后面这里实现");
        return null;
    }

    public SubmitToRemote(WorkRecord workrecord) {
        super();
        this.workrecords.add(workrecord);
    }

    public SubmitToRemote(BlockingQueue<WorkRecord> added) {
        this.workrecords.addAll(added);
    }

}
