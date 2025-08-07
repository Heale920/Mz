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
import org.example.blogplarform.constant.Result; // 确保导入 Result 类

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
        // 捕获异常，确保日志记录失败不影响主业务流程
        try {
            // 获取注解信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AdminOperation adminOperation = method.getAnnotation(AdminOperation.class);

            // 获取操作类型
            String actionType = adminOperation.value();

            // 声明 adminId 和 adminName
            Long adminId = null;
            String adminName = null;

            // 【核心修改点】根据操作类型区分获取管理员信息的方式
            if ("管理员登录".equals(actionType) && result instanceof Result) {
                // 如果是登录操作，从返回结果中获取用户信息
                Result<?> loginResult = (Result<?>) result;
                if (loginResult.isSuccess() && loginResult.getData() instanceof Map) {
                    Map<String, Object> data = (Map<String, Object>) loginResult.getData();
                    // 确保获取到的用户对象是正确的类型
                    if (data.containsKey("user") && data.get("user") instanceof User) {
                        User user = (User) data.get("user");
                        adminId = user.getId();
                        adminName = user.getUsername();
                    }
                }
            } else {
                // 对于其他所有操作，从请求头的 Token 中获取用户信息
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String token = request.getHeader("Authorization");
                    if (token != null && token.startsWith("Bearer ")) {
                        token = token.substring(7);
                        adminId = JwtUtils.getUserIdFromToken(token);
                        adminName = new JwtUtils().getAdminNameFromToken(token);
                    }
                }
            }

            // 如果获取不到 adminId 或 adminName，则不记录日志
            if (adminId == null || adminName == null) {
                return;
            }

            // 获取描述模板和参数
            String description = adminOperation.description();
            // ... (下面这部分逻辑保持不变，因为你之前写的没问题)
            String[] paramNames = adminOperation.params();

            if (description.isEmpty()) {
                description = String.format("管理员[%s]执行了[%s]操作", adminName, actionType);
            } else {
                Object[] args = joinPoint.getArgs();
                Parameter[] parameters = method.getParameters();
                Map<String, Object> paramMap = new HashMap<>();

                for (int i = 0; i < parameters.length; i++) {
                    paramMap.put(parameters[i].getName(), args[i]);
                }

                Object[] descParams = new Object[paramNames.length + 1];
                descParams[0] = adminName;

                for (int i = 0; i < paramNames.length; i++) {
                    descParams[i + 1] = paramMap.get(paramNames[i]);
                }

                description = String.format(description, descParams);
            }

            // 记录日志
            adminLogService.recordAdminLog(adminId, adminName, actionType, description);

        } catch (Exception e) {
            // 记录日志时的异常不应影响主业务流程
            // 你也可以在这里使用日志框架(如slf4j)记录异常
            e.printStackTrace();
        }
    }
}