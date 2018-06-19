package start;

import java.util.Date;
import java.util.List;

import user.WorkRecord;
import database.WorkRecordDAO;
import javafx.scene.control.Alert;

/**
 * 连接控制和数据存储的桥接
 *
 * @author ERON
 */
public class RecoderModel {

    private final WorkRecordDAO workrecordDAO;  //存储接口，传入具体实现接口的类

    public RecoderModel(WorkRecordDAO workrecordDAO) throws Exception {
        this.workrecordDAO = workrecordDAO;
        this.workrecordDAO.setup();
        
        this.workrecordDAO.connect();
    }

    public void setup() throws Exception {  //创建数据库，应当在程序第一次运行就创建好
        this.workrecordDAO.setup();
    }

    public void addNewRecord(String owner, String work_name, String system_name, double work_acount, String work_content, Date record_time, int isDraft) {

        WorkRecord workrecord = new WorkRecord(owner, work_name, system_name, work_acount, work_content, record_time, isDraft);
        System.out.println(workrecord.toString());  ////////////////////////////////////////////////
        this.workrecordDAO.insertWorkrecord(workrecord);
    }
    public void addNewRecord(WorkRecord workRecord){
        this.workrecordDAO.insertWorkrecord(workRecord);
    }
    
    public void deleteRecord(WorkRecord workrecord) {
        this.workrecordDAO.deleteWorkrecord(workrecord);
    }

    public List<WorkRecord> getIsDraft(boolean isDraft) {  //根据输入   确定是要草稿还是已经提交的历史纪录
        List<WorkRecord> workrecords = workrecordDAO.findWorkrecordisDraft(isDraft);
        if (workrecords.isEmpty()) {
            return null;
        }
        return workrecords;
    }

    public void close() throws Exception {
        workrecordDAO.close();
    }
}
