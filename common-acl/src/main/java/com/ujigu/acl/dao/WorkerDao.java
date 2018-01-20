package com.ujigu.acl.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.ujigu.acl.entity.Worker;
import com.ujigu.acl.vo.WorkerVO;
import com.ujigu.secure.db.dao.IBaseDao;

@Repository
public interface WorkerDao extends IBaseDao<Integer, Worker> {
    Integer insertReturnPK(Worker worker);
    
    List<WorkerVO> findWorkerVOByPage(@Param("entity") Worker query, @Param("departIds") List<Integer> departIds, PageBounds bounds);
}