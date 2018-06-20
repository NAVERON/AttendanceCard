package start;

import com.sun.glass.ui.PlatformFactory;
import java.util.Date;
import java.util.List;

import user.WorkRecord;
import database.WorkRecordDAO;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

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
    }

    public void addNewRecord(WorkRecord workRecord){
        System.out.println("添加的记录数据："+workRecord.toString());
        this.workrecordDAO.insertWorkrecord(workRecord);
    }
    
    public void deleteRecord(WorkRecord workrecord) {
        System.out.println("将要删除的数据 : " + workrecord.toString());
        this.workrecordDAO.deleteWorkrecord(workrecord);
    }

    public List<WorkRecord> getIsDraft(int isDraft) {  //根据输入   确定是要草稿还是已经提交的历史纪录
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
