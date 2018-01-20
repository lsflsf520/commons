package com.ujigu.csai.msglog.dao;

import com.ujigu.csai.msglog.entity.MsgSendLog;
import com.ujigu.secure.db.dao.IBaseDao;
import org.springframework.stereotype.Repository;

@Repository
public interface MsgSendLogDao extends IBaseDao<Integer, MsgSendLog> {
    Integer insertReturnPK(MsgSendLog msgSendLog);
}