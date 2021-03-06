package com.jkinvest.jkl.base.xss;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.jkinvest.jkl.base.xss.converter.XssStringJsonDeserializer;
import com.jkinvest.jkl.base.xss.filter.XssFilter;

import static com.jkinvest.jkl.base.xss.filter.XssFilter.IGNORE_PARAM_VALUE;
import static com.jkinvest.jkl.base.xss.filter.XssFilter.IGNORE_PATH;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * XSS 跨站攻击自动配置
 *
 * @author zuihou
 * @date 2019/07/25
 */
public class XssAuthConfiguration {
    /**
     * 配置跨站攻击 反序列化处理器
     *
     * @return
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer2() {
        return builder -> builder.deserializerByType(String.class, new XssStringJsonDeserializer());
    }


    /**
     * 配置跨站攻击过滤器
     *
     * @return
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        //TODO 想想这里如何扩展

        FilterRegistrationBean filterRegistration = new FilterRegistrationBean(new XssFilter());
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setOrder(1);

        Map<String, String> initParameters = new HashMap<>(2);
        String ignorePaths = new StringJoiner(",")
                .add("/favicon.ico")
                .add("/doc.html")
                .add("/swagger-ui.html")
                .add("/csrf")
                .add("/webjars/*")
                .add("/v2/*")
                .add("/swagger-resources/*")
                .add("/resources/*")
                .add("/static/*")
                .add("/public/*")
                .add("/classpath:*")
                .add("/actuator/*")
                .add("/**/noxss/**")
                .toString();
        initParameters.put(IGNORE_PATH, ignorePaths);

        String ignoreParamNames = new StringJoiner(",")
                .add("noxss")
                .toString();
        initParameters.put(IGNORE_PARAM_VALUE, ignoreParamNames);
        filterRegistration.setInitParameters(initParameters);
        return filterRegistration;
    }
}
