package org.example.blogplarform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.blogplarform.mapper.AdminLogMapper;
import org.example.blogplarform.model.AdminLog;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminLogServiceImpl extends ServiceImpl<AdminLogMapper, AdminLog> implements AdminLogService {

    @Override
    public void recordAdminLog(Long adminId, String adminName, String actionType, String detail) {
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setAdminName(adminName);
        log.setAdminActionType(actionType);
        log.setDetail(detail);
        log.setCreateTime(LocalDateTime.now());
        this.save(log);
    }

    @Override
    public IPage<AdminLog> getAdminLogs(int page, int size, String adminName, String actionType, LocalDateTime startTime, LocalDateTime endTime) {
        // 将前端的1-based页码转换为0-based
        Page<AdminLog> pageParam = new Page<>(page - 1, size);
        LambdaQueryWrapper<AdminLog> queryWrapper = new LambdaQueryWrapper<>();

        // 如果所有筛选条件都为空，则返回所有日志
        if (adminName == null && actionType == null && startTime == null && endTime == null) {
            queryWrapper.orderByDesc(AdminLog::getCreateTime);
            return this.page(pageParam, queryWrapper);
        }

        // 添加查询条件（只有在提供了筛选条件时才添加）
        if (adminName != null && !adminName.trim().isEmpty()) {
            queryWrapper.like(AdminLog::getAdminName, adminName.trim());
        }
        if (actionType != null && !actionType.trim().isEmpty()) {
            queryWrapper.eq(AdminLog::getAdminActionType, actionType.trim());
        }
        if (startTime != null) {
            queryWrapper.ge(AdminLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(AdminLog::getCreateTime, endTime);
        }

        // 添加默认排序
        queryWrapper.orderByDesc(AdminLog::getCreateTime);

        try {
            return this.page(pageParam, queryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            // 如果查询出错，返回空页
            return new Page<>();
        }
    }
} 