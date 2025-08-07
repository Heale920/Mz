package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.blogplarform.model.AdminLog;

import java.time.LocalDateTime;

public interface AdminLogService extends IService<AdminLog> {
    // 记录管理员操作日志
    void recordAdminLog(Long adminId, String adminName, String actionType, String detail);
    
    // 分页查询管理员日志
    IPage<AdminLog> getAdminLogs(int page, int size, String adminName, String actionType, LocalDateTime startTime, LocalDateTime endTime);
} 