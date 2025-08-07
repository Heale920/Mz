package org.example.blogplarform.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.blogplarform.annotation.AdminOperation;
import org.example.blogplarform.model.User;
import org.example.blogplarform.service.AdminLogService;
import org.example.blogplarform.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class AdminOperationAspect {

    @Autowired
    private AdminLogService adminLogService;

    @AfterReturning(pointcut = "@annotation(org.example.blogplarform.annotation.AdminOperation)", returning = "result")
    public void afterReturning(JoinPoint joinPoint, Object result) {
        try {
            // 获取当前请求
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();

            // 从请求头获取token
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 从token中获取管理员信息
            Long adminId = JwtUtils.getUserIdFromToken(token);
            String adminName = new JwtUtils().getAdminNameFromToken(token);

            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AdminOperation adminOperation = method.getAnnotation(AdminOperation.class);

            // 获取操作类型
            String actionType = adminOperation.value();
            
            // 获取描述模板和参数
            String description = adminOperation.description();
            String[] paramNames = adminOperation.params();
            
            // 如果没有提供描述模板，使用默认描述
            if (description.isEmpty()) {
                description = String.format("管理员[%s]执行了[%s]操作", adminName, actionType);
            } else {
                // 构建参数映射
                Object[] args = joinPoint.getArgs();
                Parameter[] parameters = method.getParameters();
                Map<String, Object> paramMap = new HashMap<>();
                
                // 将方法参数名和值放入映射
                for (int i = 0; i < parameters.length; i++) {
                    paramMap.put(parameters[i].getName(), args[i]);
                }
                
                // 收集描述所需的参数值
                Object[] descParams = new Object[paramNames.length + 1];
                descParams[0] = adminName; // 第一个参数总是管理员名称
                
                for (int i = 0; i < paramNames.length; i++) {
                    descParams[i + 1] = paramMap.get(paramNames[i]);
                }
                
                // 格式化描述
                description = String.format(description, descParams);
            }

            // 记录日志
            adminLogService.recordAdminLog(adminId, adminName, actionType, description);
        } catch (Exception e) {
            // 记录日志时的异常不应影响主业务流程
            e.printStackTrace();
        }
    }
} 