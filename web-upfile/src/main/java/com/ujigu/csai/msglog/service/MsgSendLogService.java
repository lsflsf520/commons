package com.ujigu.csai.msglog.service;

import com.ujigu.csai.msglog.dao.MsgSendLogDao;
import com.ujigu.csai.msglog.entity.MsgSendLog;
import com.ujigu.secure.db.dao.IBaseDao;
import com.ujigu.secure.db.service.AbstractBaseService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class MsgSendLogService extends AbstractBaseService<Integer, MsgSendLog> {
    @Resource
    private MsgSendLogDao msgSendLogDao;

    @Override
    protected IBaseDao<Integer, MsgSendLog> getBaseDao() {
        return msgSendLogDao;
    }

    public Integer insertReturnPK(MsgSendLog msgSendLog) {
        msgSendLogDao.insertReturnPK(msgSendLog);
        return msgSendLog.getPK();
    }

    public Integer doSave(MsgSendLog msgSendLog) {
        if (msgSendLog.getPK() == null) {
            return this.insertReturnPK(msgSendLog);
        }
        this.update(msgSendLog);
        return msgSendLog.getPK();
    }
    
}