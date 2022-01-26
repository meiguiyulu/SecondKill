# Secondkill

## 1. 项目搭建

```yaml
spring:
  # thymeleaf相关配置
  thymeleaf:
    # 关闭缓存
    cache: false

  # 数据库配置
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url:jdbc: jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: root
    password: 7012+2
    hikari:
      # 连接池名
      pool-name: DateHikariCP
      # 最小空闲连接池
      minimum-idle: 5
      # 最大连接数 默认10
      maximum-pool-size: 10
      # 连接池返回的连接自动提交
      auto-commit: true
      # 连接最大存活时间 0表示永久存活 默认1800000 （30分钟）
      max-lifetime: 1800000
      # 连接超时时间 默认30000（30秒）
      connection-timeout: 30000
      # 测试连接是否可用的查询语句
      connection-test-query: select 1

#Mybatis-plus配置
mybatis-plus:
  # 配置Mapper.xml映射文件
  mapper-locations: classpath*:/mapper/*Mapper.xml
  # 配置MyBatis数据返回类型别名(默认别名是类名)
  type-aliases-package: com.liuyj.SecondKill.pojo


# MyBatis SQL打印(方法接口所在的包，不是Mapper.xml所在的包)
logging:
  level:
    com.liuyj.SecondKill.mapper: debug

```

## 2.登录功能

### 2.1 两次MD5加密

- 用户端: password＝MD5(明文+固定Salt)
- 服务端: password＝MD5(用户输入+随机Salt)

#### 2.1.1 新建工具类 `MD5Util`

```java
/**
 * @author LYJ
 * @create 2022-01-18 17:34
 * MD5工具类
 */

public class MD5Util {

    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d";

    public static String fromInputPassword(String inputPassword){
        String str = String.valueOf("" + salt.charAt(0) + salt.charAt(2) + inputPassword +
                salt.charAt(5) + salt.charAt(4));
        return md5(str);
    }

    public static String fromPasswordToDBPassword(String fromPassword, String salt){
        String str = String.valueOf("" + salt.charAt(0) + salt.charAt(2) + fromPassword +
                salt.charAt(5) + salt.charAt(4));
        return md5(str);
    }

    public static String inputToDBPassword(String inputPassword, String salt){
        String fromPassword = fromInputPassword(inputPassword);
        String DBPassword = fromPasswordToDBPassword(fromPassword, salt);
        return DBPassword;
    }

    public static void main(String[] args) {
        System.out.println(fromInputPassword("123456"));
        System.out.println(fromPasswordToDBPassword("d3b1294a61a07da9b49b6e22b2cbd7f9",
                "1a2b3c4d"));
        System.out.println(inputToDBPassword("123456",
                "1a2b3c4d"));

        /**
         * d3b1294a61a07da9b49b6e22b2cbd7f9
         * b7797cce01b4b131b433b6acf4add449
         * b7797cce01b4b131b433b6acf4add449
         */
    }
```

创建 `user` 数据库表

```mysql
create TABLE t_user(
	`id` BIGINT(20) NOT NULL COMMENT '用户ID，手机号码',
	`nickname` VARCHAR(255) NOT NULL,
	`password` VARCHAR(32) DEFAULT NULL COMMENT 'MD5(MD5(pass明文+固定salt)+固定salt)',
	`salt` VARCHAR(10) DEFAULT NULL,
	`head` VARCHAR(128) DEFAULT NULL COMMENT '头像',
	`register_date` datetime DEFAULT NULL COMMENT '注册时间',
	`last_login_date` datetime DEFAULT NULL COMMENT '最后一次登录时间',
	`login_count` INT(11) DEFAULT '0' COMMENT '登录次数',
	PRIMARY KEY(`id`)
)
```

#### 2.1.2 逆向工程(mybatisPlus自动生成代码)

导入需要的依赖

```xml
        <!--lombok-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-generator</artifactId>
            <version>3.5.0</version>
        </dependency>

        <dependency>
            <groupId>org.freemarker</groupId>
            <artifactId>freemarker</artifactId>
            <version>2.3.31</version>
        </dependency>

        <!--mysql驱动-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-generator</artifactId>
            <version>3.0.5</version>
        </dependency>
```



```java
package com.liuyj.generator;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.*;

/**
 * @author LYJ
 * @create 2022-01-18 22:02
 */
public class CodeGenerator {
    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    public static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        StringBuilder help = new StringBuilder();
        help.append("请输入" + tip + "：");
        System.out.println(help.toString());
        if (scanner.hasNext()) {
            String ipt = scanner.next();
            if (StringUtils.isNotEmpty(ipt)) {
                return ipt;
            }
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        String projectPath = System.getProperty("user.dir");
        gc.setOutputDir(projectPath + "/src/main/java");
        //作者
        gc.setAuthor("LiuYunJie");
        //打开输出目录
        gc.setOpen(false);
        //xml开启 BaseResultMap
        gc.setBaseResultMap(true);
        //xml 开启BaseColumnList
        gc.setBaseColumnList(true);
        //日期格式，采用Date
        gc.setDateType(DateType.ONLY_DATE);
        mpg.setGlobalConfig(gc);
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://localhost:3306/secondkill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("7012+2");
        mpg.setDataSource(dsc);
        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("com.liuyj.secondkill")
                .setEntity("pojo")
                .setMapper("mapper")
                .setService("service")
                .setServiceImpl("service.impl")
                .setController("controller");
        mpg.setPackageInfo(pc);
        // 自定义配置
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
        // to do nothing
                Map<String,Object> map = new HashMap<>();
                map.put("date1","1.0.0");
                this.setMap(map);
            }
        };
        // 如果模板引擎是 freemarker
        String templatePath = "/templates/mapper.xml.ftl";
        // 如果模板引擎是 velocity
        // String templatePath = "/templates/mapper.xml.vm";

        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(new FileOutConfig(templatePath) {
            @Override
            public String outputFile(TableInfo tableInfo) {
        // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                return projectPath + "/src/main/resources/mapper/" +
                        tableInfo.getEntityName() + "Mapper"
                        + StringPool.DOT_XML;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);
        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig()
                .setEntity("templates/entity.java")
                .setMapper("templates/mapper.java")
                .setService("templates/service.java")
                .setServiceImpl("templates/serviceImpl.java")
                .setController("templates/controller.java");
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        //数据库表映射到实体的命名策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        //数据库表字段映射到实体的命名策略
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        //lombok模型
        strategy.setEntityLombokModel(true);
        //生成 @RestController 控制器
        //strategy.setRestControllerStyle(true);
        strategy.setInclude(scanner("表名，多个英文逗号分割").split(","));
        strategy.setControllerMappingHyphenStyle(true);
        //表前缀
        strategy.setTablePrefix("t_");
        mpg.setStrategy(strategy);
        mpg.setTemplateEngine(new FreemarkerTemplateEngine());
        mpg.execute();
        }
    }
```

### 2.2 实现登录功能

1. 新建枚举类，保存登录成功与失败的信息

   ```java
   package com.liuyj.secondkill.vo;
   
   import lombok.AllArgsConstructor;
   import lombok.Getter;
   import lombok.ToString;
   
   /**
    * @author LYJ
    * @create 2022-01-19 18:15
    * 公共返回对象枚举
    */
   
   @Getter
   @ToString
   @AllArgsConstructor
   public enum ResponseBeanEnum {
       //通用
       SUCCESS(200, "SUCCESS"),
       ERROR(500, "服务端异常"),
       //登录模块5002xx
       LOGIN_ERROR(500210, "用户名或密码不正确"),
       MOBILE_ERROR(500211, "手机号码格式不正确"),
       BIND_ERROR(500212, "参数校验异常"),
       MOBILE_NOT_EXIST(500213, "手机号码不存在"),
       PASSWORD_UPDATE_FAIL(500214, "密码更新失败"),
       SESSION_ERROR(500215, "用户不存在"),
       //秒杀模块5005xx
       EMPTY_STOCK(500500, "库存不足"),
       REPEATE_ERROR(500501, "该商品每人限购一件"),
       REQUEST_ILLEGAL(500502, "请求非法，请重新尝试"),
       ERROR_CAPTCHA(500503, "验证码错误，请重新输入"),
       ACCESS_LIMIT_REAHCED(500504, "访问过于频繁，请稍后再试"),
       //订单模块5003xx
       ORDER_NOT_EXIST(500300, "订单信息不存在"),
       ;
       private final Integer code;
       private final String message;
   }
   
   ```

2. 实现跳转功能

   - ```java
     package com.liuyj.secondkill.controller;
     
     import com.liuyj.secondkill.service.IUserService;
     import com.liuyj.secondkill.vo.LoginVo;
     import com.liuyj.secondkill.vo.ResponseBean;
     import lombok.extern.slf4j.Slf4j;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.boot.autoconfigure.AutoConfigureOrder;
     import org.springframework.stereotype.Controller;
     import org.springframework.web.bind.annotation.RequestMapping;
     import org.springframework.web.bind.annotation.ResponseBody;
     
     /**
      * @author LYJ
      * @create 2022-01-19 18:09
      */
     
     @Controller
     @RequestMapping("/login")
     @Slf4j
     public class LoginController {
     
         @Autowired
         IUserService iUserService;
     
         /**
          * 跳转到登陆页面
          * */
         @RequestMapping("/toLogin")
         public String toLogin(){
             return "login";
         }
     
         /**
          * 登录功能
          * */
         @RequestMapping("/doLogin")
         @ResponseBody
         public ResponseBean doLogin(LoginVo loginVo) {
             return iUserService.doLogin(loginVo);
         }
     }
     ```

   - `Service`层

     ```java
     package com.liuyj.secondkill.service.impl;
     
     import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
     import com.liuyj.secondkill.mapper.UserMapper;
     import com.liuyj.secondkill.pojo.User;
     import com.liuyj.secondkill.service.IUserService;
     import com.liuyj.secondkill.utils.MD5Util;
     import com.liuyj.secondkill.utils.ValidatorUtil;
     import com.liuyj.secondkill.vo.LoginVo;
     import com.liuyj.secondkill.vo.ResponseBean;
     import com.liuyj.secondkill.vo.ResponseBeanEnum;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.stereotype.Service;
     import org.thymeleaf.util.StringUtils;
     import org.thymeleaf.util.Validate;
     
     /**
      * <p>
      *  服务实现类
      * </p>
      *
      * @author LiuYunJie
      * @since 2022-01-18
      */
     @Service
     public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
     
         @Autowired
         private UserMapper userMapper;
     
         /**
          * 登录功能
          * */
         @Override
         public ResponseBean doLogin(LoginVo loginVo) {
             String mobile = loginVo.getMobile();
             String password = loginVo.getPassword();
             if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
                 return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
             }
             if (!ValidatorUtil.isMobile(mobile)) {
                 return ResponseBean.error(ResponseBeanEnum.MOBILE_ERROR);
             }
             // 根据手机号获取用户
             User user = userMapper.selectById(mobile);
             if (null == user) {
                 return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
             }
     
             // 判断密码是否正确
             if (!MD5Util.fromPasswordToDBPassword(password, user.getSalt()).equals(user.getPassword())) {
                 return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
             }
     
             return ResponseBean.success();
         }
     }
     ```

### 2.3 自定义注解实现参数校验

1. 添加 `validator` 组件

   - ```xml
     		<!-- validation 组件 -->
       		<dependency>
       			<groupId>org.springframework.boot</groupId>
       			<artifactId>spring-boot-starter-validation</artifactId>
       		</dependency>
     ```

2. 自定义注解以及规则

   - ```java
     package com.liuyj.secondkill.validator;
     
     import com.liuyj.secondkill.vo.IsMobileValidator;
     
     import javax.validation.Constraint;
     import javax.validation.Payload;
     
     import java.lang.annotation.Documented;
     import java.lang.annotation.Retention;
     import java.lang.annotation.Target;
     
     import static java.lang.annotation.ElementType.*;
     import static java.lang.annotation.RetentionPolicy.RUNTIME;
     
     /**
      * @author LYJ
      * @create 2022-01-20 10:15
      * 自定义注解实现验证手机号的功能
      */
     
     @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
     @Retention(RUNTIME)
     @Documented
     @Constraint(validatedBy = {IsMobileValidator.class})
     public @interface IsMobile {
     
         // 手机号必填
         boolean required() default true;
     
         String message() default "手机号码格式错误";
     
         Class<?>[] groups() default { };
     
         Class<? extends Payload>[] payload() default { };
     }
     ```

   - ```java
     package com.liuyj.secondkill.vo;
     
     import com.liuyj.secondkill.utils.ValidatorUtil;
     import com.liuyj.secondkill.validator.IsMobile;
     import org.thymeleaf.util.StringUtils;
     
     import javax.validation.ConstraintValidator;
     import javax.validation.ConstraintValidatorContext;
     
     /**
      * @author LYJ
      * @create 2022-01-20 10:33
      * 手机号校验规则
      */
     public class IsMobileValidator implements ConstraintValidator<IsMobile, String> {
     
         /*
         *  required记录手机号是否必填
         * */
         private boolean required = false;
     
         @Override
         public void initialize(IsMobile constraintAnnotation) {
             required = constraintAnnotation.required();
     //        ConstraintValidator.super.initialize(constraintAnnotation);
         }
     
         @Override
         public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
             if (required) {
                 /*
                 必填
                 */
                 return ValidatorUtil.isMobile(s);
             } else {
                 /*
                 非必填
                 * */
                 if (StringUtils.isEmpty(s)) {
                     return true;
                 } else {
                     return ValidatorUtil.isMobile(s);
                 }
             }
         }
     }
     ```

3. 在 `controller`层添加注解

   - ```java
         /**
          * 登录功能
          * */
         @RequestMapping("/doLogin")
         @ResponseBody
         public ResponseBean doLogin(@Valid LoginVo loginVo) {
             return iUserService.doLogin(loginVo);
         }
     ```

   - ```java
     /**
      * @author LYJ
      * @create 2022-01-19 20:33
      * 登录参数: 手机号 密码
      */
     
     @Data
     public class LoginVo {
         @NotNull
         @IsMobile
         private String mobile;
     
         @NotNull
         @Length(min = 32)  // 长度最小32号
         private String password;
     }
     ```

4. 将 `service` 层中参数校验的代码注释

   - ```java
     package com.liuyj.secondkill.service.impl;
     
     import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
     import com.liuyj.secondkill.mapper.UserMapper;
     import com.liuyj.secondkill.pojo.User;
     import com.liuyj.secondkill.service.IUserService;
     import com.liuyj.secondkill.utils.MD5Util;
     import com.liuyj.secondkill.utils.ValidatorUtil;
     import com.liuyj.secondkill.vo.LoginVo;
     import com.liuyj.secondkill.vo.ResponseBean;
     import com.liuyj.secondkill.vo.ResponseBeanEnum;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.stereotype.Service;
     import org.thymeleaf.util.StringUtils;
     import org.thymeleaf.util.Validate;
     
     /**
      * <p>
      *  服务实现类
      * </p>
      *
      * @author LiuYunJie
      * @since 2022-01-18
      */
     @Service
     public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
     
         @Autowired
         private UserMapper userMapper;
     
         /**
          * 登录功能
          * */
         @Override
         public ResponseBean doLogin(LoginVo loginVo) {
             String mobile = loginVo.getMobile();
             String password = loginVo.getPassword();
             // 参数校验
     //
     //        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
     //            return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
     //        }
     //        if (!ValidatorUtil.isMobile(mobile)) {
     //            return ResponseBean.error(ResponseBeanEnum.MOBILE_ERROR);
     //        }
             // 根据手机号获取用户
             User user = userMapper.selectById(mobile);
             if (null == user) {
                 return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
             }
     
             // 判断密码是否正确
             if (!MD5Util.fromPasswordToDBPassword(password, user.getSalt()).equals(user.getPassword())) {
                 return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
             }
     
             return ResponseBean.success();
         }
     }
     ```

5. 异常处理

   - 当前存在问题：手机号码格式不正确的时候，前端没有显示异常的信息，但是控制台日志正常显示

   - ![image-20220120104800553](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220120104800553.png)

   - 解决方法：捕获异常 `BindExceprtion`

     - 新建一个异常类 `GlobalException`

       - ```java
         package com.liuyj.secondkill.exception;
         
         import com.liuyj.secondkill.vo.ResponseBeanEnum;
         import lombok.AllArgsConstructor;
         import lombok.Data;
         import lombok.NoArgsConstructor;
         
         /**
          * @author LYJ
          * @create 2022-01-20 10:49
          * 全局异常
          */
         
         @Data
         @NoArgsConstructor
         @AllArgsConstructor
         public class GlobalException extends RuntimeException{
         
             private ResponseBeanEnum responseBeanEnum;
         
         }
         ```

     - 异常处理

       - ```java
         package com.liuyj.secondkill.exception;
         
         import com.liuyj.secondkill.vo.ResponseBean;
         import com.liuyj.secondkill.vo.ResponseBeanEnum;
         import org.springframework.validation.BindException;
         import org.springframework.web.bind.annotation.ExceptionHandler;
         import org.springframework.web.bind.annotation.RestControllerAdvice;
         
         /**
          * @author LYJ
          * @create 2022-01-20 10:50'
          * 全局异常处理类
          */
         
         @RestControllerAdvice
         public class GlobalExceptionHandler {
         
             @ExceptionHandler(Exception.class)
             public ResponseBean ExceptionHandler(Exception e) {
                 if (e instanceof GlobalException) {
                     GlobalException ex = (GlobalException) e;
                     return ResponseBean.error(ex.getResponseBeanEnum());
                 } else if (e instanceof BindException){
                     BindException ex = (BindException) e;
                     ResponseBean responseBean = ResponseBean.error(ResponseBeanEnum.BIND_ERROR);
                     responseBean.setMessage("参数校验异常: " +
                             ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
                     return responseBean;
                 }
                 return ResponseBean.error(ResponseBeanEnum.ERROR);
             }
         }
         
         ```

     - 在此基础上可以对原先的代码进行修改

       - ```java
         /**
          * <p>
          *  服务实现类
          * </p>
          *
          * @author LiuYunJie
          * @since 2022-01-18
          */
         @Service
         public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
         
             @Autowired
             private UserMapper userMapper;
         
             /**
              * 登录功能
              * */
             @Override
             public ResponseBean doLogin(LoginVo loginVo) {
                 String mobile = loginVo.getMobile();
                 String password = loginVo.getPassword();
         
                 // 根据手机号获取用户
                 User user = userMapper.selectById(mobile);
                 if (null == user) {
         //            return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
                     throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
                 }
         
                 // 判断密码是否正确
                 if (!MD5Util.fromPasswordToDBPassword(password, user.getSalt()).equals(user.getPassword())) {
         //            return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
                     throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
                 }
         
                 return ResponseBean.success();
             }
         }
         ```

### 2.4 分布式Session

#### 2.4.1 完善登录功能

> 使用cookie与session保存登录状态

1. 添加了两个工具类

   - `UUIDUtil`

     - ```java
       package com.liuyj.secondkill.utils;
       
       import java.util.UUID;
       
       /**
        * UUID工具类
        *
        * @author zhoubin
        * @since 1.0.0
        */
       public class UUIDUtil {
       
          public static String uuid() {
             return UUID.randomUUID().toString().replace("-", "");
          }
       }
       ```

   - `CookieUtil`

     - ```java
       package com.liuyj.secondkill.utils;
       
       import javax.servlet.http.Cookie;
       import javax.servlet.http.HttpServletRequest;
       import javax.servlet.http.HttpServletResponse;
       import java.io.UnsupportedEncodingException;
       import java.net.URLDecoder;
       import java.net.URLEncoder;
       
       /**
        * Cookie工具类
        *
        * @author zhoubin
        * @since 1.0.0
        */
       public final class CookieUtil {
       
           /**
            * 得到Cookie的值, 不编码
            *
            * @param request
            * @param cookieName
            * @return
            */
           public static String getCookieValue(HttpServletRequest request, String cookieName) {
               return getCookieValue(request, cookieName, false);
           }
       
           /**
            * 得到Cookie的值,
            *
            * @param request
            * @param cookieName
            * @return
            */
           public static String getCookieValue(HttpServletRequest request, String cookieName, boolean isDecoder) {
               Cookie[] cookieList = request.getCookies();
               if (cookieList == null || cookieName == null) {
                   return null;
               }
               String retValue = null;
               try {
                   for (int i = 0; i < cookieList.length; i++) {
                       if (cookieList[i].getName().equals(cookieName)) {
                           if (isDecoder) {
                               retValue = URLDecoder.decode(cookieList[i].getValue(), "UTF-8");
                           } else {
                               retValue = cookieList[i].getValue();
                           }
                           break;
                       }
                   }
               } catch (UnsupportedEncodingException e) {
                   e.printStackTrace();
               }
               return retValue;
           }
       
           /**
            * 得到Cookie的值,
            *
            * @param request
            * @param cookieName
            * @return
            */
           public static String getCookieValue(HttpServletRequest request, String cookieName, String encodeString) {
               Cookie[] cookieList = request.getCookies();
               if (cookieList == null || cookieName == null) {
                   return null;
               }
               String retValue = null;
               try {
                   for (int i = 0; i < cookieList.length; i++) {
                       if (cookieList[i].getName().equals(cookieName)) {
                           retValue = URLDecoder.decode(cookieList[i].getValue(), encodeString);
                           break;
                       }
                   }
               } catch (UnsupportedEncodingException e) {
                   e.printStackTrace();
               }
               return retValue;
           }
       
           /**
            * 设置Cookie的值 不设置生效时间默认浏览器关闭即失效,也不编码
            */
           public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                        String cookieValue) {
               setCookie(request, response, cookieName, cookieValue, -1);
           }
       
           /**
            * 设置Cookie的值 在指定时间内生效,但不编码
            */
           public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                        String cookieValue, int cookieMaxage) {
               setCookie(request, response, cookieName, cookieValue, cookieMaxage, false);
           }
       
           /**
            * 设置Cookie的值 不设置生效时间,但编码
            */
           public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                        String cookieValue, boolean isEncode) {
               setCookie(request, response, cookieName, cookieValue, -1, isEncode);
           }
       
           /**
            * 设置Cookie的值 在指定时间内生效, 编码参数
            */
           public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                        String cookieValue, int cookieMaxage, boolean isEncode) {
               doSetCookie(request, response, cookieName, cookieValue, cookieMaxage, isEncode);
           }
       
           /**
            * 设置Cookie的值 在指定时间内生效, 编码参数(指定编码)
            */
           public static void setCookie(HttpServletRequest request, HttpServletResponse response, String cookieName,
                                        String cookieValue, int cookieMaxage, String encodeString) {
               doSetCookie(request, response, cookieName, cookieValue, cookieMaxage, encodeString);
           }
       
           /**
            * 删除Cookie带cookie域名
            */
           public static void deleteCookie(HttpServletRequest request, HttpServletResponse response,
                                           String cookieName) {
               doSetCookie(request, response, cookieName, "", -1, false);
           }
       
           /**
            * 设置Cookie的值，并使其在指定时间内生效
            *
            * @param cookieMaxage cookie生效的最大秒数
            */
           private static final void doSetCookie(HttpServletRequest request, HttpServletResponse response,
                                                 String cookieName, String cookieValue, int cookieMaxage, boolean isEncode) {
               try {
                   if (cookieValue == null) {
                       cookieValue = "";
                   } else if (isEncode) {
                       cookieValue = URLEncoder.encode(cookieValue, "utf-8");
                   }
                   Cookie cookie = new Cookie(cookieName, cookieValue);
                   if (cookieMaxage > 0)
                       cookie.setMaxAge(cookieMaxage);
                   if (null != request) {// 设置域名的cookie
                       String domainName = getDomainName(request);
                       System.out.println(domainName);
                       if (!"localhost".equals(domainName)) {
                           cookie.setDomain(domainName);
                       }
                   }
                   cookie.setPath("/");
                   response.addCookie(cookie);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
       
           /**
            * 设置Cookie的值，并使其在指定时间内生效
            *
            * @param cookieMaxage cookie生效的最大秒数
            */
           private static final void doSetCookie(HttpServletRequest request, HttpServletResponse response,
                                                 String cookieName, String cookieValue, int cookieMaxage, String encodeString) {
               try {
                   if (cookieValue == null) {
                       cookieValue = "";
                   } else {
                       cookieValue = URLEncoder.encode(cookieValue, encodeString);
                   }
                   Cookie cookie = new Cookie(cookieName, cookieValue);
                   if (cookieMaxage > 0) {
                      cookie.setMaxAge(cookieMaxage);
                   }
                   if (null != request) {// 设置域名的cookie
                       String domainName = getDomainName(request);
                       System.out.println(domainName);
                       if (!"localhost".equals(domainName)) {
                           cookie.setDomain(domainName);
                       }
                   }
                   cookie.setPath("/");
                   response.addCookie(cookie);
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
       
           /**
            * 得到cookie的域名
            */
           private static final String getDomainName(HttpServletRequest request) {
               String domainName = null;
               // 通过request对象获取访问的url地址
               String serverName = request.getRequestURL().toString();
               if (serverName == null || serverName.equals("")) {
                   domainName = "";
               } else {
                   // 将url地下转换为小写
                   serverName = serverName.toLowerCase();
                   // 如果url地址是以http://开头  将http://截取
                   if (serverName.startsWith("http://")) {
                       serverName = serverName.substring(7);
                   }
                   int end = serverName.length();
                   // 判断url地址是否包含"/"
                   if (serverName.contains("/")) {
                       //得到第一个"/"出现的位置
                       end = serverName.indexOf("/");
                   }
       
                   // 截取
                   serverName = serverName.substring(0, end);
                   // 根据"."进行分割
                   final String[] domains = serverName.split("\\.");
                   int len = domains.length;
                   if (len > 3) {
                       // www.xxx.com.cn
                       domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
                   } else if (len <= 3 && len > 1) {
                       // xxx.com or xxx.cn
                       domainName = domains[len - 2] + "." + domains[len - 1];
                   } else {
                       domainName = serverName;
                   }
               }
       
               if (domainName != null && domainName.indexOf(":") > 0) {
                   String[] ary = domainName.split("\\:");
                   domainName = ary[0];
               }
               return domainName;
           }
       }
       ```

2. 修改登录相关的代码

   - > 需要添加 `HttpServletRequest request` 与 `HttpServletResponse response` 两个参数

   - `LoginController`

     - ```java
           /**
            * 登录功能
            * */
           @RequestMapping("/doLogin")
           @ResponseBody
           public ResponseBean doLogin(@Valid LoginVo loginVo, HttpServletRequest request,
                                       HttpServletResponse response) {
               return iUserService.doLogin(loginVo, request, response);
           }
       ```

   - `UserServiceImpl.java`

     - ```java
       package com.liuyj.secondkill.service.impl;
       
       import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
       import com.liuyj.secondkill.exception.GlobalException;
       import com.liuyj.secondkill.mapper.UserMapper;
       import com.liuyj.secondkill.pojo.User;
       import com.liuyj.secondkill.service.IUserService;
       import com.liuyj.secondkill.utils.CookieUtil;
       import com.liuyj.secondkill.utils.MD5Util;
       import com.liuyj.secondkill.utils.UUIDUtil;
       import com.liuyj.secondkill.utils.ValidatorUtil;
       import com.liuyj.secondkill.vo.LoginVo;
       import com.liuyj.secondkill.vo.ResponseBean;
       import com.liuyj.secondkill.vo.ResponseBeanEnum;
       import org.springframework.beans.factory.annotation.Autowired;
       import org.springframework.stereotype.Service;
       import org.thymeleaf.util.StringUtils;
       import org.thymeleaf.util.Validate;
       
       import javax.servlet.http.HttpServletRequest;
       import javax.servlet.http.HttpServletResponse;
       
       /**
        * <p>
        *  服务实现类
        * </p>
        *
        * @author LiuYunJie
        * @since 2022-01-18
        */
       @Service
       public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
       
           @Autowired
           private UserMapper userMapper;
       
           /**
            * 登录功能
            * */
           @Override
           public ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request,
                                       HttpServletResponse response) {
               String mobile = loginVo.getMobile();
               String password = loginVo.getPassword();
               // 参数校验
       //
       //        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(password)) {
       //            return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
       //        }
       //        if (!ValidatorUtil.isMobile(mobile)) {
       //            return ResponseBean.error(ResponseBeanEnum.MOBILE_ERROR);
       //        }
               // 根据手机号获取用户
               User user = userMapper.selectById(mobile);
               if (null == user) {
       //            return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
                   throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
               }
       
               // 判断密码是否正确
               if (!MD5Util.fromPasswordToDBPassword(password, user.getSalt()).equals(user.getPassword())) {
       //            return ResponseBean.error(ResponseBeanEnum.LOGIN_ERROR);
                   throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
               }
       
               // 生成cookie
               String ticket = UUIDUtil.uuid();
               request.getSession().setAttribute(ticket, user);
               CookieUtil.setCookie(request, response, "userTicket", ticket);
       
               return ResponseBean.success();
           }
       }
       
       ```

   - 前端页面

     - ![image-20220120201306733](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220120201306733.png)

3. 新建跳转到商品页的相关代码

   - `goodsController`

     - ```java
       @Controller
       @RequestMapping("/goods")
       public class GoodsController {
       
           /*
           * 跳转到商品页面
           * */
           @RequestMapping("/toList")
           public String toList(HttpSession session, Model model,
                                @CookieValue("userTicket") String ticket) {
               if (StringUtils.isEmpty(ticket)) {
                   return "login";
               }
               User user = (User) session.getAttribute(ticket);
               if (null == user) {
                   return "login";
               }
               model.addAttribute("user", user);
               return "goodsList";
           }
       }
       ```

   - 新建 `goodsList.html` 测试

     - ```html
       <!DOCTYPE html>
       <html lang="en"
             xmlns:th="http://www.thymeleaf.org">
       <head>
           <meta charset="UTF-8">
           <title>商品列表</title>
       </head>
       <body>
           <P th:text="'测试登录功能' + ${user.nickname}"></P>
       </body>
       </html>
       ```

   - ![image-20220120201452775](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220120201452775.png)

#### 2.4.2 分布式 Session 问题

之前的代码在在一台Tomcat上，没有什么问题。当我们部署多台系统，配合Nginx的时候会出现用户登录的问题。

原因：

由于 Nginx 使用默认负载均衡策略（轮询），请求将会按照时间顺序逐一分发到后端应用上。也就是说刚开始我们在 Tomcat1 登录之后，用户信息放在 Tomcat1 的 Session 里。过了一会，请求又被 Nginx 分发到了 Tomcat2 上，这时 Tomcat2 上 Session 里还没有用户信息，于是又要登录。

解决方法：

- Session复制
  - 优点
    - 无需修改代码，只需要修改Tomcat配置
  - 缺点
    - Session同步传输占用内网带宽
    - 多台Tomcat同步性能指数级下降
    - Session占用内存，无法有效水平扩展
- 前端存储
  - 优点
    - 不占用服务器内存
  - 缺点
    - 前端是放在cookie中存储，存在安全风险
    - 数据大小受cookie限制
    - 占用外网带宽
- Session粘滞
  - 优点
    - 无需修改代码
    - 服务器可以水平扩展
  - 缺点
    - 增加新机器，会重新hash，导致重新登录
    - 引用重启，需要重新登录
- 后端集中存储
  - 优点
    - 安全
    - 容易水平扩展
  - 缺点
    - 增加复杂度
    - 需要修改代码

#### 2.4.3  Redis实现分布式Session

##### 2.4.1 SpringSession实现分布式Session

> 不需要修改代码

1. 添加 SpringSession 依赖

   - ```xml
     		<!--spring data redis 依赖-->
       		<dependency>
       			<groupId>org.springframework.boot</groupId>
       			<artifactId>spring-boot-starter-data-redis</artifactId>
       		</dependency>
       		<!--commons-pool2 对象池依赖-->
       		<dependency>
       			<groupId>org.apache.commons</groupId>
       			<artifactId>commons-pool2</artifactId>
       		</dependency>
       		<!--Spring Session依赖-->
       		<dependency>
       			<groupId>org.springframework.session</groupId>
       			<artifactId>spring-session-data-redis</artifactId>
       		</dependency>
     ```

2. 添加配置

   - > 注意：
     >
     >  	Redis要与datasource同一级

   - ```yml
     # redis配置
       redis:
         #服务器地址
         host: 127.0.0.1
         #端口
         port: 6379
         #数据库
         database: 0
         #超时时间
         timeout: 10000
         #密码
     #    password: root
         lettuce:
           pool:
             #最大连接数，默认8
             max-active: 8
             #最大连接阻塞等待时间，默认-1
             max-wait: 10000ms
             #最大空闲连接，默认8
             max-idle: 200
             #最小空闲连接，默认0
             min-idle: 5
     ```

   - 

3. 成功

   - ![image-20220120214727009](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220120214727009.png)
   - ![image-20220120214743376](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220120214743376.png)

##### 2.4.2 方式二：将用户信息存入Redis

- 添加依赖

  - ```xml
    <!--spring data redis 依赖-->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    <!--commons-pool2 对象池依赖-->
    <dependency>
        <groupId>org.apache.commons</groupId>
        <artifactId>commons-pool2</artifactId>
    </dependency>
    ```

- 配置 Redis

  - ```yml
      # redis配置
      redis:
        #服务器地址
        host: 127.0.0.1
        #端口
        port: 6379
        #数据库
        database: 0
        #超时时间
        timeout: 10000
        #密码
    #    password: root
        lettuce:
          pool:
            #最大连接数，默认8
            max-active: 8
            #最大连接阻塞等待时间，默认-1
            max-wait: 10000ms
            #最大空闲连接，默认8
            max-idle: 200
            #最小空闲连接，默认0
            min-idle: 5
    ```

- 新建 Redis 配置类 RedisConfig

  - ```java
    /**
     * @author LYJ
     * @create 2022-01-21 13:53
     * Redis配置类
     */
    
    @Configuration
    public class RedisConfig {
    
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
            RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    
            // key序列化
            redisTemplate.setKeySerializer(new StringRedisSerializer());
            // value序列化
            redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
            // hash类型 key序列化
            redisTemplate.setHashKeySerializer(new StringRedisSerializer());
            // hash类型 value序列化
            redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    
    
            // 注入连接工厂
            redisTemplate.setConnectionFactory(factory);
            return redisTemplate;
        }
    }
    ```

- 修改登录功能

  - > 存储到Session改成存储到Redis

  - ```java
        @Autowired
        private UserMapper userMapper;
        @Autowired
        private RedisTemplate redisTemplate;    
    
    /**
         * 登录功能
         */
        @Override
        public ResponseBean doLogin(LoginVo loginVo, HttpServletRequest request, HttpServletResponse response) {
            String mobile = loginVo.getMobile();
            String password = loginVo.getPassword();
    
            // 根据手机号获取用户
            User user = userMapper.selectById(mobile);
            if (null == user) {
                throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
            }
    
            // 判断密码是否正确
            if (!MD5Util.fromPasswordToDBPassword(password, user.getSalt()).equals(user.getPassword())) {
                throw new GlobalException(ResponseBeanEnum.LOGIN_ERROR);
            }
    
            // 生成cookie
            String ticket = UUIDUtil.uuid();
    /*        放在session中
            request.getSession().setAttribute(ticket, user);*/
            /*将用户信息存入redis中*/
            redisTemplate.opsForValue().set("user:" + ticket, user);
            CookieUtil.setCookie(request, response, "userTicket", ticket);
    
            return ResponseBean.success();
        }
    
    ```

- 新添加一个方法

  - 从cookie中获取用户

  - ```java
        /**
         * 根据cookie获取用户
         *
         * @param userTicket
         * @return
         */
        @Override
        public User getUserByCookie(String userTicket, HttpServletRequest request,
                                    HttpServletResponse response) {
            if (StringUtils.isEmpty(userTicket)) {
                return null;
            }
            User user = (User) redisTemplate.opsForValue().get("user:" + userTicket);
            if (user != null) {
                CookieUtil.setCookie(request, response, "userTicket", userTicket);
            }
            return user;
        }
    ```

- ![image-20220121222005289](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220121222005289.png)

#### 2.4.4 优化登录功能

> 现在登录以后得所有操作都存在重复得代码：
>
> ​	即根据cookie判断用户是否成功登录
>
> 优化以后：
>
> ​	只需要判断一次，然后将需要用户的函数处传User对象

1. 新建配置类 WebConfig

   - ```java
     package com.liuyj.secondkill.config;
     
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.context.annotation.Configuration;
     import org.springframework.web.method.support.HandlerMethodArgumentResolver;
     import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
     
     import java.util.List;
     
     /**
      * @author LYJ
      * @create 2022-01-21 22:23
      * MVC配置类
      */
     @Configuration
     public class WebConfig implements WebMvcConfigurer {
     
         @Autowired
         private UserArgumentResolver userArgumentResolver;
     
     
         @Override
         public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
             resolvers.add(userArgumentResolver);
         }
     }
     ```

2. 自定义用户参数

   - ```java
     package com.liuyj.secondkill.config;
     
     import com.liuyj.secondkill.pojo.User;
     import com.liuyj.secondkill.service.IUserService;
     import com.liuyj.secondkill.utils.CookieUtil;
     import org.springframework.beans.factory.annotation.Autowired;
     import org.springframework.core.MethodParameter;
     import org.springframework.stereotype.Component;
     import org.springframework.web.bind.support.WebDataBinderFactory;
     import org.springframework.web.context.request.NativeWebRequest;
     import org.springframework.web.method.support.HandlerMethodArgumentResolver;
     import org.springframework.web.method.support.ModelAndViewContainer;
     import org.thymeleaf.util.StringUtils;
     
     import javax.servlet.http.HttpServletRequest;
     import javax.servlet.http.HttpServletResponse;
     
     /**
      * @author LYJ
      * @create 2022-01-21 22:26
      * 自定义用户参数
      */
     @Component
     public class UserArgumentResolver implements HandlerMethodArgumentResolver {
     
         @Autowired
         private IUserService userService;
     
         @Override
         public boolean supportsParameter(MethodParameter parameter) {
             Class<?> aClass = parameter.getParameterType();
             return aClass == User.class;
         }
     
         @Override
         public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                   NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
             HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
             HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
             String ticket = CookieUtil.getCookieValue(request, "userTicket");
             if (StringUtils.isEmpty(ticket)) {
                 return null;
             }
     
             return userService.getUserByCookie(ticket, request, response);
         }
     }
     ```

3. 修改原先代码

   - ```java
         @Autowired
         private UserServiceImpl userService;
     
         /*
         * 跳转到商品页面
         * */
         @RequestMapping("/toList")
         public String toList(Model model, User user) {
         /*    if (StringUtils.isEmpty(ticket)) {
                 return "login";
             }
             通过session获取
             User user = (User) session.getAttribute(ticket);*//*
     
             User user = (User) userService.getUserByCookie(ticket, request, response);
             if (null == user) {
                 return "login";
             }*/
             model.addAttribute("user", user);
             return "goodsList";
         }
     ```

## 3. 秒杀

> 准备工作：新建数据库表

商品表和订单表：

```mysql
create table `t_goods`(
	`id` BIGINT(20) not null AUTO_INCREMENT COMMENT '商品id',
	`goods_name` VARCHAR(16) DEFAULT NULL COMMENT '商品名称',
	`goods_title` VARCHAR(64) DEFAULT NULL COMMENT '商品标题',
	`goods_img` VARCHAR(64) DEFAULT NULL COMMENT '商品图片',
	`goods_detail` LONGTEXT  COMMENT '商品描述',
	`goods_price` DECIMAL(10, 2) DEFAULT '0.00' COMMENT '商品价格',
	`goods_stock` INT(11) DEFAULT '0' COMMENT '商品库存,-1表示没有限制',
	PRIMARY KEY(`id`)
)ENGINE = INNODB AUTO_INCREMENT = 3 DEFAULT CHARSET = utf8mb4;


CREATE TABLE `t_order` (
	`id` BIGINT(20) NOT NULL  AUTO_INCREMENT COMMENT '订单ID',
	`user_id` BIGINT(20) DEFAULT NULL COMMENT '用户ID',
	`goods_id` BIGINT(20) DEFAULT NULL COMMENT '商品ID',
	`delivery_addr_id` BIGINT(20) DEFAULT NULL  COMMENT '收获地址ID',
	`goods_name` VARCHAR(16) DEFAULT NULL  COMMENT '商品名字',
	`goods_count` INT(20) DEFAULT '0'  COMMENT '商品数量',
	`goods_price` DECIMAL(10,2) DEFAULT '0.00'  COMMENT '商品价格',
	`order_channel` TINYINT(4) DEFAULT '0'  COMMENT '1 pc,2 android, 3 ios',
	`status` TINYINT(4) DEFAULT '0'  COMMENT '订单状态，0新建未支付，1已支付，2已发货，3已收货，4已退货，5已完成',
	`create_date` datetime DEFAULT NULL  COMMENT '订单创建时间',
	`pay_date` datetime DEFAULT NULL  COMMENT '支付时间',
	PRIMARY KEY(`id`)
)ENGINE = INNODB AUTO_INCREMENT=12 DEFAULT CHARSET = utf8mb4;
```

秒杀商品表和秒杀订单表：

```mysql
CREATE TABLE `t_seckill_goods`(
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
	`goods_id` BIGINT(20) NOT NULL COMMENT '商品ID',
	`seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价',
	`stock_count` INT(10) NOT NULL  COMMENT '库存数量',
	`start_date` datetime NOT NULL  COMMENT '秒杀开始时间',
	`end_date` datetime NOT NULL COMMENT '秒杀结束时间',
	PRIMARY KEY(`id`)
)ENGINE = INNODB AUTO_INCREMENT=3 DEFAULT CHARSET = utf8mb4;


CREATE TABLE `t_seckill_order` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '秒杀订单ID',
	`user_id` BIGINT(20) NOT NULL  COMMENT '用户ID',
	`order_id` BIGINT(20) NOT NULL  COMMENT '订单ID',
	`goods_id` BIGINT(20) NOT NULL  COMMENT '商品ID',
	PRIMARY KEY(`id`)
)ENGINE = INNODB AUTO_INCREMENT=3 DEFAULT CHARSET = utf8mb4;
```

创建两条数据：

```mysql
INSERT INTO `t_goods`
VALUES
	( 1, 'IPHONE12', 'IPHONE 12 64GB', '/img/iphone12.png', 'IPHONE12 64GB', '6299.0', 100 ),
	(
		2,
		'IPHONE12 PRO',
		'IPHONE12 PRO 64 GB',
		'/img/iphone12.png',
		'IPHONE12 PRO 64 GB',
	'9299.0',
	100)
	
INSERT INTO `t_seckill_goods`
VALUES
	( 1, 1, '629', 10, '2020-11-01 08:00:00', '2020-11-01 09:00:00'),(
		2,
		2,
		'929',
		10,
		'2020-11-01 08:00:00',
		'2020-11-01 09:00:00'
	)
```



### 3.1 商品列表页

1. 逆向工程生成代码

2. 添加一个辅助类 GoodsVo

   - > 因为页面上显示的信息很多，单单 Goods 或者 Seckill 类不够用，所以新添加一个辅助类 GoodsVo存储必要的信息。

3. 显示商品列表需要查询数据库，故在GoodsController类中注入IGoodsService，修改登陆成功的方法

   - ```java
     /**
      * @author LYJ
      * @create 2022-01-20 19:53
      * 判断用户是否正确登录
      *      若正确登录，则跳到商品页面
      *      否则，回到登陆页面
      */
     
     @Controller
     @RequestMapping("/goods")
     public class GoodsController {
     
         @Autowired
         private UserServiceImpl userService;
         @Autowired
         private IGoodsService goodsService;
     
         /*
         * 跳转到商品页面
         * */
         @RequestMapping("/toList")
         public String toList(Model model, User user) {
             model.addAttribute("user", user);
             model.addAttribute("goodsList", goodsService.findGoodsVo());
     
             return "goodsList";
         }
     }
     ```

4. 修改相应的 IGoodsService、GoodsServiceImpl、GoodsMapper.xml等文件

   - ```java
     /**
      * <p>
      *  服务类
      * </p>
      *
      * @author LiuYunJie
      * @since 2022-01-23
      */
     public interface IGoodsService extends IService<Goods> {
     
         /*获取产品列表*/
         List<GoodsVo> findGoodsVo();
     }
     
     /**
      * <p>
      *  服务实现类
      * </p>
      *
      * @author LiuYunJie
      * @since 2022-01-23
      */
     @Service
     public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
     
         @Autowired
         private GoodsMapper goodsMapper;
     
         /*获取商品列表*/
         @Override
         public List<GoodsVo> findGoodsVo() {
             return goodsMapper.findGoodsVo();
         }
     }
     ```

   - ```xml
     <?xml version="1.0" encoding="UTF-8"?>
     <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
     <mapper namespace="com.liuyj.secondkill.mapper.GoodsMapper">
     
         <!-- 通用查询映射结果 -->
         <resultMap id="BaseResultMap" type="com.liuyj.secondkill.pojo.Goods">
             <id column="id" property="id" />
             <result column="goods_name" property="goodsName" />
             <result column="goods_title" property="goodsTitle" />
             <result column="goods_img" property="goodsImg" />
             <result column="goods_detail" property="goodsDetail" />
             <result column="goods_price" property="goodsPrice" />
             <result column="goods_stock" property="goodsStock" />
         </resultMap>
     
         <!-- 通用查询结果列 -->
         <sql id="Base_Column_List">
             id, goods_name, goods_title, goods_img, goods_detail, goods_price, goods_stock
         </sql>
     
         <!--获取商品列表-->
         <select id="findGoodsVo" resultType="com.liuyj.secondkill.vo.GoodsVo">
             SELECT
                 g.*,
                 tsg.seckill_price,
                 tsg.stock_count,
                 tsg.start_date,
                 tsg.end_date
             FROM
                 t_goods AS g
                     LEFT JOIN t_seckill_goods AS tsg ON g.id = tsg.goods_id
         </select>
     </mapper>
     ```



### 3.2 商品详情页

思路与[3.1 商品列表页](###3.1 商品列表页)类似。

1. 在GoodsController类中添加跳转商品详情的方法

   - ```java
         /*
         *  功能描述: 跳转商品详情页
         * */
         @RequestMapping("/toDetail/{goodsId}")
         public String toDetail(Model model, User user,
                                @PathVariable Long goodsId) {
             model.addAttribute("user", user);
             model.addAttribute("goods", goodsService.findGoodsVoById(goodsId));
             return "goodsDetail";
         }
     ```

2. 实现 findGoodsVoById 方法

   - ```java
     /**
      * <p>
      *  服务类
      * </p>
      *
      * @author LiuYunJie
      * @since 2022-01-23
      */
     public interface IGoodsService extends IService<Goods> {
     
         /*获取商品列表*/
         List<GoodsVo> findGoodsVo();
     
         /*获取商品详情*/
         GoodsVo findGoodsVoById(Long goodsId);
     }
     ```

   - ```java
     /**
      * <p>
      *  服务实现类
      * </p>
      *
      * @author LiuYunJie
      * @since 2022-01-23
      */
     @Service
     public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements IGoodsService {
     
         @Autowired
         private GoodsMapper goodsMapper;
     
         /*获取商品列表*/
         @Override
         public List<GoodsVo> findGoodsVo() {
             return goodsMapper.findGoodsVo();
         }
     
         /*获取商品详情*/
         @Override
         public GoodsVo findGoodsVoById(Long goodsId) {
             return goodsMapper.findGoodsVoById(goodsId);
         }
     }
     ```

   - ```java
     /**
      * <p>
      *  Mapper 接口
      * </p>
      *
      * @author LiuYunJie
      * @since 2022-01-23
      */
     public interface GoodsMapper extends BaseMapper<Goods> {
     
         /*获取商品列表*/
         List<GoodsVo> findGoodsVo();
     
         /*获取商品详情*/
         GoodsVo findGoodsVoById(Long goodsId);
     }
     ```

   - ```xml
         <!--获取商品详情-->
         <select id="findGoodsVoById" resultType="com.liuyj.secondkill.vo.GoodsVo">
             SELECT
                 g.*,
                 tsg.seckill_price,
                 tsg.stock_count,
                 tsg.start_date,
                 tsg.end_date
             FROM
                 t_goods AS g
                     LEFT JOIN t_seckill_goods AS tsg ON g.id = tsg.goods_id
             Where g.id = #{goodsId}
         </select>
     ```

### 3.3 秒杀倒计时处理

> 思路：
>
> - 设置两个变量
>   - secKillStatus 记录秒杀的状态：0代表未开始，1代表秒杀进行中，2代表秒杀已结束。
>   - remainSeconds 记录秒杀的倒计时：如果秒杀未开始，则显示倒计时；0代表秒杀进行中；-1代表秒杀结束。

```java
    /*
    *  功能描述: 跳转商品详情页
    * */
    @RequestMapping("/toDetail/{goodsId}")
    public String toDetail(Model model, User user,
                           @PathVariable Long goodsId) {
        model.addAttribute("user", user);
        GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
        Date startDate = goodsVo.getStartDate();
        Date endDate = goodsVo.getEndDate();
        Date nowDate = new Date();
        /*秒杀状态*/
        int secKillStatus = 0;
        /*秒杀倒计时*/
        int remainSeconds = 0;

        if (nowDate.before(startDate)) {
            /*秒杀还未开始*/
            remainSeconds = ((int) (nowDate.getTime() - startDate.getTime())) / 1000;
        } else if (nowDate.after(endDate)) {
            /*秒杀已经结束*/
            secKillStatus = 2;
            remainSeconds = -1;
        } else {
            /*秒杀进行中*/
            secKillStatus = 1;
            remainSeconds = 0;
        }

        model.addAttribute("secKillStatus", secKillStatus);
        model.addAttribute("remainSeconds", remainSeconds);
        model.addAttribute("goods", goodsVo);
        return "goodsDetail";
    }
```

```html
<tr>
    <td>秒杀开始时间</td>
    <td th:text="${#dates.format(goods.startDate,'yyyy-MM-dd HH:mm:ss')}"></td>
    <td id="seckillTip">
        <input type="hidden" id="remainSeconds" th:value="${remainSeconds}">
        <span th:if="${secKillStatus eq 0}">秒杀倒计时:
            <span id="countDown" th:text="${remainSeconds}"></span>秒
        </span>
        <span th:if="${secKillStatus eq 1}">秒杀进行中</span>
        <span th:if="${secKillStatus eq 2}">秒杀已结束</span>
    </td>
    <td>
        <form id="secKillForm" method="post" action="/seckill/doSeckill">
            <input type="hidden" name="goodsId" th:value="${goods.id}">
            <button class="btn btn-primary btn-block" type="submit" id="buyButton">立即秒杀</button>
        </form>
    </td>
</tr>

<script>
    $(function () {
        countDown();
    });

    function countDown() {
        var remainSeconds = $("#remainSeconds").val();
        var timeout;
        //秒杀还未开始
        if (remainSeconds > 0) {
            $("#buyButton").attr("disabled", true);
            timeout = setTimeout(function () {
                $("#countDown").text(remainSeconds - 1);
                $("#remainSeconds").val(remainSeconds - 1);
                countDown();
            }, 1000);
            // 秒杀进行中
        } else if (remainSeconds == 0) {
            $("#buyButton").attr("disabled", false);
            if (timeout) {
                clearTimeout(timeout);
            }
            $("#seckillTip").html("秒杀进行中")
        } else {
            $("#buyButton").attr("disabled", true);
            $("#seckillTip").html("秒杀已经结束");
        }
    };
</script>
```

### 3.4 秒杀功能实现

> 点击秒杀按钮需要判断：
>
> 1. 库存是否为0
> 2. 此用户是否已经抢购过一次
>
> 若可以秒杀，则：
>
> 1. 生成订单
> 2. 生成秒杀订单

- SecKillController

  - ```java
    package com.liuyj.secondkill.controller;
    
    import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
    import com.liuyj.secondkill.pojo.Order;
    import com.liuyj.secondkill.pojo.SeckillOrder;
    import com.liuyj.secondkill.pojo.User;
    import com.liuyj.secondkill.service.IGoodsService;
    import com.liuyj.secondkill.service.IOrderService;
    import com.liuyj.secondkill.service.ISeckillOrderService;
    import com.liuyj.secondkill.vo.GoodsVo;
    import com.liuyj.secondkill.vo.ResponseBeanEnum;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.RequestMapping;
    
    import java.nio.IntBuffer;
    
    /**
     * @author LYJ
     * @create 2022-01-24 9:25
     * 秒杀功能实现
     */
    
    @Controller
    @RequestMapping("/seckill")
    public class SecKillController {
    
        @Autowired
        private IGoodsService goodsService;
        @Autowired
        private ISeckillOrderService secKillOrderService;
        @Autowired
        private IOrderService orderService;
    
        /*简化：跳转到订单页面*/
        @RequestMapping("doSeckill")
        public String doSeckill(Model model, User user,
                                Long goodsId) {
            if (user == null) {
                return "login";
            }
            model.addAttribute("user", user);
            /**
             * 这里再次查看商品的库存原因在于前端页面上的库存数量很容易通过F12被修改
             * */
            GoodsVo goodsVo = goodsService.findGoodsVoById(goodsId);
            if (goodsVo.getStockCount() < 1) {
                model.addAttribute("errmsg",
                        ResponseBeanEnum.EMPTY_STOCK.getMessage());
                return "secKillFail";
            }
    
            /*查看是否重复抢购*/
            SeckillOrder seckillOrder = secKillOrderService.getOne(new QueryWrapper<SeckillOrder>()
                    .eq("user_id", user.getId())
                    .eq("goods_id", goodsVo.getId()));
            if (seckillOrder != null) {
                model.addAttribute("errmsg", ResponseBeanEnum.REPEATE_ERROR.getMessage());
                return "secKillFail";
            }
            Order order = orderService.secKill(user, goodsVo);
            model.addAttribute("order", order);
            model.addAttribute("goods", goodsVo);
            return "orderDetail";
        }
    }
    ```

- OrderServiceImpl

  - ```java
    package com.liuyj.secondkill.service.impl;
    
    import com.baomidou.mybatisplus.core.conditions.Wrapper;
    import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
    import com.liuyj.secondkill.pojo.Order;
    import com.liuyj.secondkill.mapper.OrderMapper;
    import com.liuyj.secondkill.pojo.SeckillGoods;
    import com.liuyj.secondkill.pojo.SeckillOrder;
    import com.liuyj.secondkill.pojo.User;
    import com.liuyj.secondkill.service.IOrderService;
    import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
    import com.liuyj.secondkill.service.ISeckillGoodsService;
    import com.liuyj.secondkill.service.ISeckillOrderService;
    import com.liuyj.secondkill.vo.GoodsVo;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.ui.Model;
    
    import java.util.Date;
    
    /**
     * <p>
     *  服务实现类
     * </p>
     *
     * @author LiuYunJie
     * @since 2022-01-23
     */
    @Service
    public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    
        @Autowired
        private ISeckillGoodsService seckillGoodsService;
        @Autowired
        private OrderMapper orderMapper;
        @Autowired
        private ISeckillOrderService seckillOrderService;
    
        /*秒杀功能*/
        @Override
        public Order secKill(User user, GoodsVo goodsVo) {
            // 秒杀商品表减库存
            SeckillGoods seckillGoods = seckillGoodsService.getOne(new QueryWrapper<SeckillGoods>().
                    eq("goods_id", goodsVo.getId()));
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            seckillGoodsService.updateById(seckillGoods);
    
            /*生成订单*/
            Order order = new Order();
            order.setUserId(user.getId());
            order.setGoodsId(goodsVo.getId());
            order.setDeliveryAddrId(0L);
            order.setGoodsName(goodsVo.getGoodsName());
            order.setGoodsCount(1);
            order.setGoodsPrice(goodsVo.getSeckillPrice());
            order.setOrderChannel(1);
            order.setStatus(0);
            order.setCreateDate(new Date());
    //        order.setPayDate();
            orderMapper.insert(order);
    
            /*生成秒杀订单*/
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setUserId(user.getId());
            seckillOrder.setOrderId(order.getId());
            seckillOrder.setGoodsId(goodsVo.getId());
            seckillOrderService.save(seckillOrder);
            return order;
        }
    }
    ```

## 4. 系统压测

> 工具：JMeter
>
> 官网：https://jmeter.apache.org/

### 4.1 JMeter检测使用

- 配置：
  - ![image-20220124160611657](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220124160611657.png)
  - ![](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220124160611657.png)
  - ![image-20220124160654754](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220124160654754.png)
- 结果：
  - ![image-20220124160714731](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220124160714731.png)
  - ![image-20220124160729521](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220124160729521.png)
  - ![image-20220124160739892](https://gitee.com/yun-xiaojie/blog-image/raw/master/img/image-20220124160739892.png)





