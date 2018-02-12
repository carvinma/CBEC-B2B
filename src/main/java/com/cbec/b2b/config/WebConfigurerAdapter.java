package com.cbec.b2b.config;

import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import com.cbec.b2b.common.ApiInterceptor;
import com.cbec.b2b.common.ContentErrorMsg;
import com.cbec.b2b.common.ServiceException;
import com.cbec.b2b.common.Util;
import com.cbec.b2b.common.WebInterceptor;
import com.github.pagehelper.PageHelper;

@Configuration
public class WebConfigurerAdapter extends WebMvcConfigurerAdapter {
	
	private static final Logger logger = LogManager.getLogger(WebConfigurerAdapter.class);
	
	@Bean
	public ApiInterceptor apiInterceptor() {
		return new ApiInterceptor();
	}
	
	@Bean
	public WebInterceptor webInterceptor() {
		return new WebInterceptor();
	}
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(webInterceptor()).addPathPatterns("/llback/**");
		registry.addInterceptor(apiInterceptor()).addPathPatterns("/api/**");
	}
	
	//统一异常处理
    @Override
    public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
        exceptionResolvers.add(new HandlerExceptionResolver() {
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
                if (e instanceof ServiceException) {
                	String msg = ((ServiceException)e).getMsg();
                	if(msg !=null && !"".equals(msg)) {
                		Util.responseResult(response, "3", msg);
                	}else {
                		Util.responseResult(response, "3",e.getMessage());
                	}
                    
                } else if (e instanceof NoHandlerFoundException) {
                    Util.responseResult(response, "4",ContentErrorMsg.ERROR_4);
                } else if (e instanceof ServletException) {
                    Util.responseResult(response, "5",e.getMessage());
                } else {
                	Util.responseResult(response, "6",ContentErrorMsg.ERROR_6);
                    String message;
                    if (handler instanceof HandlerMethod) {
                        HandlerMethod handlerMethod = (HandlerMethod) handler;
                        message = String.format("接口 [%s] 出现异常，方法：%s.%s，异常摘要：%s",
                                request.getRequestURI(),
                                handlerMethod.getBean().getClass().getName(),
                                handlerMethod.getMethod().getName(),
                                e.getMessage());
                    } else {
                        message = e.getMessage();
                    }
                    logger.error(message, e);
                }
                return new ModelAndView();
            }
        });
    }
    
    //解决跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
        .allowedOrigins("http://console.llwell.net")
        .allowedMethods("GET", "POST")
        .allowCredentials(true).maxAge(3600);
    }
    
//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
//        FastJsonConfig config = new FastJsonConfig();
//        config.setSerializerFeatures(SerializerFeature.WriteMapNullValue,//保留空的字段
//                SerializerFeature.WriteNullStringAsEmpty,//String null -> ""
//                SerializerFeature.WriteNullNumberAsZero);//Number null -> 0
//        converter.setFastJsonConfig(config);
//        converter.setDefaultCharset(Charset.forName("UTF-8"));
//        converters.add(converter);
//    }
        
}
