package com.jkinvest.jkl.base.database.mybatis;

import static org.apache.ibatis.mapping.SqlCommandType.DELETE;
import static org.apache.ibatis.mapping.SqlCommandType.INSERT;
import static org.apache.ibatis.mapping.SqlCommandType.UPDATE;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.jkinvest.jkl.base.context.BaseContextHandler;
import com.jkinvest.jkl.base.exception.BizException;
import com.jkinvest.jkl.base.utils.SpringUtils;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;


/**
 * 演示环境写权限控制 拦截器
 * 该拦截器只用于演示环境， 开发和生产都不需要
 * <p>
 *
 * @author zuihou
 * @date 2019/2/1
 */
@Slf4j
@NoArgsConstructor
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class WriteInterceptor extends AbstractSqlParserHandler implements Interceptor {


    @Override
    @SneakyThrows
    public Object intercept(Invocation invocation) {
        // 为什么在拦截器里使用 @RefreshScope 无效？
        if (!SpringUtils.getApplicationContext().getEnvironment().getProperty("zuihou.database.isNotWrite", Boolean.class, false)) {
            return invocation.proceed();
        }

        StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        sqlParser(metaObject);
        // 读操作 放行
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (SqlCommandType.SELECT.equals(mappedStatement.getSqlCommandType())) {
            return invocation.proceed();
        }
        // 记录日志相关的 放行
        if (StrUtil.containsAnyIgnoreCase(mappedStatement.getId(), "UserToken", "sms", "MsgsCenterInfo", "resetPassErrorNum", "updateLastLoginTime", "OptLog", "LoginLog", "File", "xxl")) {
            return invocation.proceed();
        }
        // userId=1 的超级管理员 放行
        Long userId = BaseContextHandler.getUserId();
        String tenant = BaseContextHandler.getTenant();
        log.info("mapperid={}, userId={}", mappedStatement.getId(), userId);


        //演示用的超级管理员 能查 和 增
        if (userId == 2 && (DELETE.equals(mappedStatement.getSqlCommandType()))) {
            throw new BizException(-1, "演示环境，无删除权限，请本地部署后测试");
        }

        //内置的租户 不能写入
        boolean isWrite = CollectionUtil.contains(Arrays.asList(DELETE, INSERT, UPDATE), mappedStatement.getSqlCommandType());
        if ("0000".equals(tenant) && isWrite) {
            throw new BizException(-1, "演示环境，无权限, 请自行创建租户后测试写入");
        }

        // 你还可以自定义其他限制规则， 比如：IP 等

        //其他用户
        return invocation.proceed();
    }

    /**
     * 生成拦截对象的代理
     *
     * @param target 目标对象
     * @return 代理对象
     */
    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    /**
     * mybatis配置的属性
     *
     * @param properties mybatis配置的属性
     */
    @Override
    public void setProperties(Properties properties) {

    }

}
