package start;

import java.util.Date;
import java.util.List;

import user.WorkRecord;
import database.WorkRecordDAO;

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

    public void addNewRecord(String owner, String work_name, String system_name, double work_acount, String work_content, Date record_time, int isDraft) {

        WorkRecord workrecord = new WorkRecord(owner, work_name, system_name, work_acount, work_content, record_time, isDraft);
        System.out.println(workrecord.toString());  ////////////////////////////////////////////////输出测试数据
        this.workrecordDAO.insertWorkrecord(workrecord);
    }
    public void addNewRecord(WorkRecord workRecord){
        System.out.println(workRecord.toString());  //////////////////////////////////////////////
        this.workrecordDAO.insertWorkrecord(workRecord);
    }
    
    public void deleteRecord(WorkRecord workrecord) {
        System.out.println("delete workrecord : " + workrecord.toString());
        this.workrecordDAO.deleteWorkrecord(workrecord);
    }

    public List<WorkRecord> getIsDraft(boolean isDraft) {  //根据输入   确定是要草稿还是已经提交的历史纪录
        List<WorkRecord> workrecords = workrecordDAO.findWorkrecordisDraft(isDraft);
        if (workrecords.isEmpty()) {
            return null;
        }
        System.out.println("输出查询到的数据 : \n" + workrecords.toString());
        return workrecords;
    }

    public void close() throws Exception {
        workrecordDAO.close();
    }
}
