# 智能问卷生态系统 — 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the Smart Survey Ecosystem backend — AI-powered survey platform with social features, economic incentives, marketplace, and deep analytics.

**Architecture:** Monolithic Spring Boot 2.6.1 + Java 8 backend, layered by feature package. JWT auth, MyBatis-Plus ORM, MySQL + Redis. RESTful API serving Vue3 frontend (frontend developed separately).

**Tech Stack:** Spring Boot 2.6.1, Java 8, Maven, MyBatis-Plus, MySQL 8.0, Redis 7.0, JWT (jjwt), Spring Security, Lombok, Swagger/OpenAPI, Alibaba Druid

**Spec:** `D:/projs/other/javadiaochawenjuan/需求.md`

---

## Architecture Overview

### Package Structure

```
com.smartsurvey
├── SmartSurveyApplication.java          // Spring Boot entry
├── common/                               // Shared utilities
│   ├── config/                           // Security, CORS, MyBatis, Redis config
│   │   ├── SecurityConfig.java
│   │   ├── CorsConfig.java
│   │   ├── MyBatisPlusConfig.java
│   │   └── RedisConfig.java
│   ├── exception/                        // Global exception handling
│   │   ├── GlobalExceptionHandler.java
│   │   ├── BusinessException.java
│   │   └── ErrorCode.java
│   ├── dto/                              // Shared DTOs
│   │   ├── ApiResponse.java
│   │   └── PageResult.java
│   ├── annotation/                       // Custom annotations
│   │   └── CurrentUser.java
│   ├── interceptor/                      // Auth interceptor
│   │   └── AuthInterceptor.java
│   └── utils/                            // Utility classes
│       ├── JwtUtils.java
│       ├── RedisUtils.java
│       ├── SmsUtils.java
│       └── DeviceFingerprintUtils.java
├── module/                               // Business modules (by feature)
│   ├── user/                             // M1: User & Social
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── UserController.java
│   │   │   └── SocialController.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── UserService.java
│   │   │   ├── FollowService.java
│   │   │   └── NotificationService.java
│   │   ├── mapper/
│   │   │   ├── UserMapper.java
│   │   │   ├── FollowMapper.java
│   │   │   └── NotificationMapper.java
│   │   └── entity/
│   │       ├── User.java
│   │       ├── Follow.java
│   │       └── Notification.java
│   ├── survey/                           // M2: Survey Engine
│   │   ├── controller/
│   │   │   ├── SurveyController.java
│   │   │   └── QuestionController.java
│   │   ├── service/
│   │   │   ├── SurveyService.java
│   │   │   ├── QuestionService.java
│   │   │   ├── SurveyLogicService.java   // Jump/show/random/quota logic
│   │   │   └── AiGenerationService.java
│   │   ├── mapper/
│   │   │   ├── SurveyMapper.java
│   │   │   └── QuestionMapper.java
│   │   └── entity/
│   │       ├── Survey.java
│   │       └── Question.java
│   ├── response/                         // M3: Responses & Collection
│   │   ├── controller/
│   │   │   └── ResponseController.java
│   │   ├── service/
│   │   │   ├── ResponseService.java
│   │   │   ├── AnswerService.java
│   │   │   └── AntiCheatService.java
│   │   ├── mapper/
│   │   │   ├── ResponseMapper.java
│   │   │   └── AnswerMapper.java
│   │   └── entity/
│   │       ├── Response.java
│   │       └── Answer.java
│   ├── incentive/                        // M3: Rewards & Payments
│   │   ├── controller/
│   │   │   ├── RewardController.java
│   │   │   └── PaymentController.java
│   │   ├── service/
│   │   │   ├── RewardService.java
│   │   │   ├── PaymentService.java
│   │   │   └── TransactionService.java
│   │   ├── mapper/
│   │   │   └── TransactionMapper.java
│   │   └── entity/
│   │       └── Transaction.java
│   ├── marketplace/                      // M5: Survey Marketplace
│   │   ├── controller/
│   │   │   └── MarketplaceController.java
│   │   ├── service/
│   │   │   ├── MarketplaceService.java
│   │   │   ├── RecommendationService.java
│   │   │   └── AuditService.java
│   │   └── mapper/
│   │       └── DispatchMapper.java
│   ├── analytics/                        // M4: Data Analytics
│   │   ├── controller/
│   │   │   └── AnalyticsController.java
│   │   ├── service/
│   │   │   ├── StatisticsService.java
│   │   │   ├── AiAnalysisService.java
│   │   │   └── ReportService.java
│   │   └── mapper/
│   │       └── AnalyticsMapper.java
│   └── admin/                            // M6: Admin System
│       ├── controller/
│       │   ├── AdminUserController.java
│       │   ├── AdminSurveyController.java
│       │   ├── AdminFinanceController.java
│       │   └── AdminSystemController.java
│       └── service/
│           ├── AdminUserService.java
│           ├── AdminSurveyService.java
│           └── AdminFinanceService.java
```

### API Route Convention

```
/api/v1/auth/*           // Public: login, register, refresh token
/api/v1/users/*          // Auth required: profile, wallet
/api/v1/surveys/*        // Auth required: CRUD surveys, questions
/api/v1/responses/*      // Auth required: answer, submit
/api/v1/marketplace/*    // Auth required: browse, claim
/api/v1/analytics/*      // Auth required: stats, reports
/api/v1/admin/*          // Admin role: management endpoints
```

---

## Phase 0: Project Foundation (Day 1-2)

### Task 0.1: Update Maven Dependencies

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Replace pom.xml with complete dependency set**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.6.1</version>
    </parent>
    <groupId>com.smartsurvey</groupId>
    <artifactId>smart-survey</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>smart-survey</name>

    <properties>
        <java.version>1.8</java.version>
        <mybatis-plus.version>3.5.1</mybatis-plus.version>
        <druid.version>1.2.8</druid.version>
        <jjwt.version>0.9.1</jjwt.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <!-- MyBatis-Plus & MySQL -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt</artifactId>
            <version>${jjwt.version}</version>
        </dependency>
        <!-- Utilities -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
            <version>5.7.20</version>
        </dependency>
        <dependency>
            <groupId>com.github.xiaoymin</groupId>
            <artifactId>knife4j-spring-boot-starter</artifactId>
            <version>3.0.3</version>
        </dependency>
        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Run Maven install to verify**

```bash
cd D:/projs/other/javadiaochawenjuan && mvn clean install -DskipTests
```

Expected: BUILD SUCCESS

---

### Task 0.2: Create Application Configuration

**Files:**
- Create: `src/main/resources/application.yml`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-prod.yml`
- Delete: `src/main/resources/application.properties`

- [ ] **Step 1: Create application.yml**

```yaml
spring:
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8
    default-property-inclusion: non_null
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

server:
  port: 8080

mybatis-plus:
  mapper-locations: classpath*:/mapper/**/*.xml
  type-aliases-package: com.smartsurvey.module.*.entity
  global-config:
    db-config:
      id-type: auto
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

knife4j:
  enable: true

jwt:
  secret: CHANGE_ME_IN_PRODUCTION_USE_256BIT_KEY
  expiration: 7200000        # 2 hours
  refresh-expiration: 604800000  # 7 days

app:
  sms:
    provider: aliyun
    template-code: SMS_XXXXXXXXX
  payment:
    wechat-mchid: ""
    alipay-appid: ""
  upload:
    path: ./uploads
    max-size: 10485760
```

- [ ] **Step 2: Create application-dev.yml**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/smart_survey?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
    type: com.alibaba.druid.pool.DruidDataSource
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
    timeout: 3000ms

logging:
  level:
    com.smartsurvey: DEBUG
```

- [ ] **Step 3: Create application-prod.yml with production placeholders**

- [ ] **Step 4: Rename DemoApplication to SmartSurveyApplication**

Move: `src/main/java/com/demo/DemoApplication.java` → `src/main/java/com/smartsurvey/SmartSurveyApplication.java`

```java
package com.smartsurvey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartSurveyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartSurveyApplication.class, args);
    }
}
```

---

### Task 0.3: Database Initialization

**Files:**
- Create: `src/main/resources/db/migration/V1__init.sql`

- [ ] **Step 1: Create DDL script with all tables**

Write the complete SQL script (users, surveys, questions, responses, answers, follows, messages, transactions, notifications, system_configs) as defined in 需求.md Section 7.2.

- [ ] **Step 2: Execute DDL against MySQL**

```bash
mysql -u root -p smart_survey < src/main/resources/db/migration/V1__init.sql
```

- [ ] **Step 3: Verify all tables created**

```bash
mysql -u root -p -e "SHOW TABLES FROM smart_survey;"
```

Expected: 10 tables listed

---

### Task 0.4: Common Framework Classes

**Files:**
- Create: `src/main/java/com/smartsurvey/common/dto/ApiResponse.java`
- Create: `src/main/java/com/smartsurvey/common/dto/PageResult.java`
- Create: `src/main/java/com/smartsurvey/common/exception/ErrorCode.java`
- Create: `src/main/java/com/smartsurvey/common/exception/BusinessException.java`
- Create: `src/main/java/com/smartsurvey/common/exception/GlobalExceptionHandler.java`
- Create: `src/main/java/com/smartsurvey/common/config/SecurityConfig.java`
- Create: `src/main/java/com/smartsurvey/common/config/CorsConfig.java`
- Create: `src/main/java/com/smartsurvey/common/config/MyBatisPlusConfig.java`
- Create: `src/main/java/com/smartsurvey/common/interceptor/AuthInterceptor.java`
- Create: `src/main/java/com/smartsurvey/common/annotation/CurrentUser.java`

- [ ] **Step 1: Create ApiResponse**

```java
package com.smartsurvey.common.dto;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private long timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static <T> ApiResponse<T> ok() {
        return ok(null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.message = message;
        r.timestamp = System.currentTimeMillis();
        return r;
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return fail(errorCode.getCode(), errorCode.getMessage());
    }
}
```

- [ ] **Step 2: Create ErrorCode enum**

```java
package com.smartsurvey.common.exception;

public enum ErrorCode {
    // 400 Bad Request
    BAD_REQUEST(40000, "请求参数错误"),
    VALIDATION_ERROR(40001, "参数校验失败"),

    // 401 Unauthorized
    UNAUTHORIZED(40100, "未登录或登录已过期"),
    TOKEN_INVALID(40101, "Token无效"),
    LOGIN_FAILED(40102, "用户名或密码错误"),

    // 403 Forbidden
    FORBIDDEN(40300, "无权限访问"),
    ACCOUNT_FROZEN(40301, "账号已被冻结"),

    // 404 Not Found
    NOT_FOUND(40400, "资源不存在"),
    SURVEY_NOT_FOUND(40401, "问卷不存在"),
    USER_NOT_FOUND(40402, "用户不存在"),

    // 409 Conflict
    DUPLICATE_SUBMIT(40900, "请勿重复提交"),
    QUOTA_FULL(40901, "问卷配额已满"),
    SURVEY_CLAIMED(40902, "您已抢过此问卷"),

    // 422 Unprocessable Entity
    INSUFFICIENT_BALANCE(42200, "余额不足"),
    WITHDRAW_MINIMUM(42201, "提现金额不能低于10元"),
    SURVEY_CANNOT_PUBLISH(42202, "问卷信息不完整，无法发布"),
    ANSWER_EXPIRED(42203, "答题时间已过期"),

    // 429 Too Many Requests
    RATE_LIMIT(42900, "操作过于频繁，请稍后再试"),
    SMS_LIMIT(42901, "验证码发送过于频繁"),

    // 500 Internal Server Error
    INTERNAL_ERROR(50000, "服务器内部错误"),
    AI_GENERATION_FAILED(50001, "AI生成失败，请重试"),
    PAYMENT_FAILED(50002, "支付处理失败");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
```

- [ ] **Step 3: Create BusinessException**

```java
package com.smartsurvey.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
```

- [ ] **Step 4: Create GlobalExceptionHandler**

```java
package com.smartsurvey.common.exception;

import com.smartsurvey.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        log.warn("Business exception: code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return ApiResponse.fail(ErrorCode.VALIDATION_ERROR.getCode(), msg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnknown(Exception e) {
        log.error("Unknown exception", e);
        return ApiResponse.fail(ErrorCode.INTERNAL_ERROR);
    }
}
```

- [ ] **Step 5: Create SecurityConfig, CorsConfig, JwtUtils, AuthInterceptor**

Create the standard Spring Security configuration with JWT filter chain, CORS open configuration for dev, and JWT utility class supporting create/parse/validate.

- [ ] **Step 6: Run application to verify setup**

```bash
cd D:/projs/other/javadiaochawenjuan && mvn spring-boot:run
```

Expected: Application starts without errors, Swagger UI available at http://localhost:8080/doc.html

---

## Phase 1: User & Auth System (Day 3-6)

Covers requirements: U-001~U-005, U-101~U-105, U-201~U-205

### Task 1.1: User Entity & Mapper

**Files:**
- Create: `src/main/java/com/smartsurvey/module/user/entity/User.java`
- Create: `src/main/java/com/smartsurvey/module/user/mapper/UserMapper.java`

- [ ] **Step 1: Create User entity**

```java
package com.smartsurvey.module.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String phone;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String bio;
    private String tags;          // JSON array
    private String realName;
    private String idCard;
    private Integer isVerified;   // 0/1
    private Integer level;        // 1-10
    private Integer experience;
    private Integer reputation;   // 0-200
    private BigDecimal balance;           // available
    private BigDecimal frozenBalance;
    private String role;          // user/pro/enterprise/admin
    private String status;        // normal/frozen/banned
    private String loginIp;
    private LocalDateTime loginAt;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Create UserMapper**

```java
package com.smartsurvey.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartsurvey.module.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

---

### Task 1.2: Register & Login (U-001, U-002)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/user/service/AuthService.java`
- Create: `src/main/java/com/smartsurvey/module/user/controller/AuthController.java`
- Create: `src/main/java/com/smartsurvey/module/user/dto/RegisterRequest.java`
- Create: `src/main/java/com/smartsurvey/module/user/dto/LoginRequest.java`
- Create: `src/main/java/com/smartsurvey/module/user/dto/LoginResponse.java`

- [ ] **Step 1: Create DTOs**

```java
// RegisterRequest.java
package com.smartsurvey.module.user.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
public class RegisterRequest {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度8-20位")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 50, message = "昵称最长50字")
    private String username;
}

// LoginRequest.java
@Data
public class LoginRequest {
    @NotBlank(message = "账号不能为空")
    private String account;        // phone or email

    @NotBlank(message = "密码不能为空")
    private String password;
}

// LoginResponse.java
@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String avatarUrl;
        private Integer level;
        private Integer reputation;
        private String role;
    }
}
```

- [ ] **Step 2: Implement AuthService.register()**

```java
package com.smartsurvey.module.user.service;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.common.utils.JwtUtils;
import com.smartsurvey.common.utils.RedisUtils;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisUtils redisUtils;

    public LoginResponse register(RegisterRequest req) {
        // Verify SMS code
        String cachedCode = redisUtils.get("sms:" + req.getPhone());
        if (cachedCode == null || !cachedCode.equals(req.getSmsCode())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR.getCode(), "验证码错误或已过期");
        }

        // Check phone uniqueness
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>().eq(User::getPhone, req.getPhone()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR.getCode(), "该手机号已注册");
        }

        // Create user
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPhone(req.getPhone());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setLevel(1);
        user.setReputation(100);
        user.setBalance(BigDecimal.ZERO);
        user.setFrozenBalance(BigDecimal.ZERO);
        user.setRole("user");
        user.setStatus("normal");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        // Generate tokens
        String accessToken = jwtUtils.createToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.createRefreshToken(user.getId());

        // Clear SMS code
        redisUtils.del("sms:" + req.getPhone());

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getPhone, req.getAccount())
                .or()
                .eq(User::getEmail, req.getAccount()));

        if (user == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        if ("banned".equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_FROZEN);
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // Update login info
        user.setLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String accessToken = jwtUtils.createToken(user.getId(), user.getRole());
        String refreshToken = jwtUtils.createRefreshToken(user.getId());
        return buildLoginResponse(user, accessToken, refreshToken);
    }

    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        LoginResponse resp = new LoginResponse();
        resp.setAccessToken(accessToken);
        resp.setRefreshToken(refreshToken);
        resp.setExpiresIn(7200000L);

        LoginResponse.UserInfo info = new LoginResponse.UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setAvatarUrl(user.getAvatarUrl());
        info.setLevel(user.getLevel());
        info.setReputation(user.getReputation());
        info.setRole(user.getRole());
        resp.setUserInfo(info);
        return resp;
    }
}
```

- [ ] **Step 3: Implement AuthController**

```java
package com.smartsurvey.module.user.controller;

import com.smartsurvey.common.dto.ApiResponse;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @PostMapping("/send-sms")
    public ApiResponse<Void> sendSms(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return ApiResponse.ok();
    }

    @PostMapping("/refresh-token")
    public ApiResponse<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        return ApiResponse.ok(authService.refreshToken(refreshToken));
    }
}
```

- [ ] **Step 4: Add AuthService.sendSmsCode() and refreshToken()**

Implement SMS code generation (6-digit random, 5min TTL in Redis, rate limit per IP), and token refresh logic using refresh token validation.

---

### Task 1.3: User Profile Management (U-101~U-105)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/user/service/UserService.java`
- Create: `src/main/java/com/smartsurvey/module/user/controller/UserController.java`
- Create: `src/main/java/com/smartsurvey/module/user/dto/UserProfileResponse.java`
- Create: `src/main/java/com/smartsurvey/module/user/dto/UpdateProfileRequest.java`

- [ ] **Step 1: Create UserService with profile CRUD**

```java
package com.smartsurvey.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartsurvey.common.exception.BusinessException;
import com.smartsurvey.common.exception.ErrorCode;
import com.smartsurvey.module.user.dto.*;
import com.smartsurvey.module.user.entity.User;
import com.smartsurvey.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public UserProfileResponse getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return buildProfile(user);
    }

    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userMapper.selectById(userId);
        if (req.getBio() != null) user.setBio(req.getBio());
        if (req.getTags() != null) user.setTags(req.getTags());
        if (req.getAvatarUrl() != null) user.setAvatarUrl(req.getAvatarUrl());
        userMapper.updateById(user);
        return buildProfile(user);
    }

    public UserDashboardResponse getDashboard(Long userId) {
        // Aggregate: surveys count, responses count, total earnings, followers count
        // Query from respective tables
        UserDashboardResponse dashboard = new UserDashboardResponse();
        dashboard.setSurveyCount(surveyMapper.selectCount(
            new LambdaQueryWrapper<Survey>().eq(Survey::getUserId, userId)));
        dashboard.setResponseCount(responseMapper.selectCount(
            new LambdaQueryWrapper<Response>().eq(Response::getUserId, userId)));
        dashboard.setTotalEarnings(transactionMapper.getTotalEarnings(userId));
        dashboard.setFollowerCount(followMapper.selectCount(
            new LambdaQueryWrapper<Follow>().eq(Follow::getFolloweeId, userId)));
        return dashboard;
    }

    private UserProfileResponse buildProfile(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setBio(user.getBio());
        resp.setTags(user.getTags());
        resp.setLevel(user.getLevel());
        resp.setReputation(user.getReputation());
        resp.setRole(user.getRole());
        resp.setVerified(user.getIsVerified() == 1);
        return resp;
    }
}
```

- [ ] **Step 2: Create UserController**

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(@CurrentUser Long userId) {
        return ApiResponse.ok(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @CurrentUser Long userId, @Valid @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(userId, req));
    }

    @GetMapping("/dashboard")
    public ApiResponse<UserDashboardResponse> getDashboard(@CurrentUser Long userId) {
        return ApiResponse.ok(userService.getDashboard(userId));
    }
}
```

---

### Task 1.4: Social Features — Follow System (U-201, U-202)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/user/entity/Follow.java`
- Create: `src/main/java/com/smartsurvey/module/user/mapper/FollowMapper.java`
- Create: `src/main/java/com/smartsurvey/module/user/service/FollowService.java`
- Create: `src/main/java/com/smartsurvey/module/user/controller/SocialController.java`

- [ ] **Step 1: Create Follow entity and mapper**

```java
// Follow.java
@Data
@TableName("follows")
public class Follow {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long followerId;
    private Long followeeId;
    private LocalDateTime createdAt;
}

// FollowMapper.java
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
    @Select("SELECT u.id, u.username, u.avatar_url, u.bio FROM users u " +
            "INNER JOIN follows f ON u.id = f.follower_id WHERE f.followee_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<UserBrief> getFollowers(@Param("userId") Long userId);

    @Select("SELECT u.id, u.username, u.avatar_url, u.bio FROM users u " +
            "INNER JOIN follows f ON u.id = f.followee_id WHERE f.follower_id = #{userId} " +
            "ORDER BY f.created_at DESC")
    List<UserBrief> getFollowing(@Param("userId") Long userId);
}
```

- [ ] **Step 2: Implement FollowService with follow/unfollow, list followers/following, counts**

```java
@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowMapper followMapper;
    private final NotificationService notificationService;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "不能关注自己");
        }
        Long count = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId));
        if (count > 0) return; // already following

        // Check follow limit (max 2000)
        Long followingCount = followMapper.selectCount(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId));
        if (followingCount >= 2000) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "关注人数已达上限2000人");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFolloweeId(followeeId);
        follow.setCreatedAt(LocalDateTime.now());
        followMapper.insert(follow);

        notificationService.sendInteraction(followeeId, "有人关注了你", "follow");
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        followMapper.delete(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId));
    }

    public boolean isFollowing(Long followerId, Long followeeId) {
        return followMapper.selectCount(new LambdaQueryWrapper<Follow>()
            .eq(Follow::getFollowerId, followerId)
            .eq(Follow::getFolloweeId, followeeId)) > 0;
    }
}
```

- [ ] **Step 3: Create SocialController**

```java
@RestController
@RequestMapping("/api/v1/social")
@RequiredArgsConstructor
public class SocialController {
    private final FollowService followService;

    @PostMapping("/follow/{userId}")
    public ApiResponse<Void> follow(@CurrentUser Long currentUserId, @PathVariable Long userId) {
        followService.follow(currentUserId, userId);
        return ApiResponse.ok();
    }

    @DeleteMapping("/follow/{userId}")
    public ApiResponse<Void> unfollow(@CurrentUser Long currentUserId, @PathVariable Long userId) {
        followService.unfollow(currentUserId, userId);
        return ApiResponse.ok();
    }

    @GetMapping("/followers/{userId}")
    public ApiResponse<List<UserBrief>> getFollowers(@PathVariable Long userId) {
        return ApiResponse.ok(followService.getFollowers(userId));
    }

    @GetMapping("/following/{userId}")
    public ApiResponse<List<UserBrief>> getFollowing(@PathVariable Long userId) {
        return ApiResponse.ok(followService.getFollowing(userId));
    }
}
```

---

### Task 1.5: Wallet & Transaction System (U-105, D-301~D-305)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/incentive/entity/Transaction.java`
- Create: `src/main/java/com/smartsurvey/module/incentive/mapper/TransactionMapper.java`
- Create: `src/main/java/com/smartsurvey/module/incentive/service/TransactionService.java`
- Create: `src/main/java/com/smartsurvey/module/incentive/service/PaymentService.java`
- Create: `src/main/java/com/smartsurvey/module/incentive/controller/PaymentController.java`
- Create: `src/main/java/com/smartsurvey/module/incentive/controller/WalletController.java`

- [ ] **Step 1: Create Transaction entity matching DB schema**

```java
@Data
@TableName("transactions")
public class Transaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String transactionNo;   // 32-char unique SN
    private Long userId;
    private String type;            // recharge/reward/withdraw/refund/freeze/unfreeze
    private BigDecimal amount;      // positive=income, negative=expense
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Long relatedId;
    private String relatedType;
    private String status;          // pending/success/failed
    private String remark;
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Implement TransactionService with createTransaction, getBillList queries**

- [ ] **Step 3: Implement WalletController (GET /wallet, GET /wallet/bills)**

- [ ] **Step 4: Implement PaymentController stubs (POST /payment/recharge, POST /payment/withdraw)**

---

## Phase 2: Survey Engine (Day 7-12)

Covers requirements: Q-001~Q-005, Q-301~Q-305, 8 question types

### Task 2.1: Survey CRUD (Q-001, Q-003)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/survey/entity/Survey.java`
- Create: `src/main/java/com/smartsurvey/module/survey/entity/Question.java`
- Create: `src/main/java/com/smartsurvey/module/survey/mapper/SurveyMapper.java`
- Create: `src/main/java/com/smartsurvey/module/survey/mapper/QuestionMapper.java`
- Create: `src/main/java/com/smartsurvey/module/survey/service/SurveyService.java`
- Create: `src/main/java/com/smartsurvey/module/survey/service/QuestionService.java`
- Create: `src/main/java/com/smartsurvey/module/survey/controller/SurveyController.java`
- Create: `src/main/java/com/smartsurvey/module/survey/dto/CreateSurveyRequest.java`
- Create: `src/main/java/com/smartsurvey/module/survey/dto/SurveyDetailResponse.java`

- [ ] **Step 1: Create Survey entity**

```java
@Data
@TableName("surveys")
public class Survey {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String coverImage;
    private String status;             // draft/published/closed/archived
    private Integer totalQuestions;
    private Integer totalResponses;
    private Integer targetQuota;
    private Integer remainingQuota;
    private String rewardType;         // fixed/dynamic/tiered
    private BigDecimal rewardPerResponse;
    private BigDecimal rewardTotalBudget;
    private String dispatchType;       // public/targeted/smart/viral
    private String targetAudience;     // JSON
    private Integer isAnonymous;
    private Integer allowResume;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String closingMessage;
    private Integer viewCount;
    private Integer shareCount;
    private Integer aiGenerated;
    private String auditStatus;        // pending/approved/rejected
    private String auditNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

- [ ] **Step 2: Create Question entity**

```java
@Data
@TableName("questions")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long surveyId;
    private String type;               // single/multiple/judge/fill/essay/rating/matrix/ranking
    private String content;
    private Integer required;          // 0/1
    private Integer orderIndex;
    private String options;            // JSON: [{"id":1,"text":"A"},...]
    private String settings;           // JSON: validation rules, scale ranges, etc.
    private String logicJump;          // JSON: [{"option_id":2,"jump_to":5},...]
    private String logicShow;          // JSON: {"operator":"AND","conditions":[...]}
    private Integer isRandomOptions;
    private String quotaLimit;         // JSON: [{"option_id":1,"limit":100}]
    private LocalDateTime createdAt;
}
```

- [ ] **Step 3: Implement SurveyService**

Core methods:
```java
public SurveyDetailResponse create(Long userId, CreateSurveyRequest req)
public SurveyDetailResponse update(Long userId, Long surveyId, CreateSurveyRequest req)
public SurveyDetailResponse getDetail(Long surveyId)
public PageResult<SurveyBrief> getMySurveys(Long userId, String status, int page, int size)
public void changeStatus(Long userId, Long surveyId, String newStatus)
public void delete(Long userId, Long surveyId) // soft delete to archived
```

The `create` method transactionally inserts Survey + all Questions with order_index management.

- [ ] **Step 4: Implement SurveyController**

```java
@RestController
@RequestMapping("/api/v1/surveys")
@RequiredArgsConstructor
public class SurveyController {
    private final SurveyService surveyService;

    @PostMapping
    public ApiResponse<SurveyDetailResponse> create(
            @CurrentUser Long userId, @Valid @RequestBody CreateSurveyRequest req) {
        return ApiResponse.ok(surveyService.create(userId, req));
    }

    @GetMapping("/{id}")
    public ApiResponse<SurveyDetailResponse> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(surveyService.getDetail(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<SurveyDetailResponse> update(
            @CurrentUser Long userId, @PathVariable Long id,
            @Valid @RequestBody CreateSurveyRequest req) {
        return ApiResponse.ok(surveyService.update(userId, id, req));
    }

    @GetMapping("/my")
    public ApiResponse<PageResult<SurveyBrief>> getMySurveys(
            @CurrentUser Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(surveyService.getMySurveys(userId, status, page, size));
    }

    @PostMapping("/{id}/publish")
    public ApiResponse<Void> publish(@CurrentUser Long userId, @PathVariable Long id,
                                     @Valid @RequestBody PublishRequest req) {
        surveyService.publish(userId, id, req);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/close")
    public ApiResponse<Void> close(@CurrentUser Long userId, @PathVariable Long id) {
        surveyService.changeStatus(userId, id, "closed");
        return ApiResponse.ok();
    }
}
```

- [ ] **Step 5: Write unit tests for SurveyService**

Create `src/test/java/com/smartsurvey/module/survey/service/SurveyServiceTest.java` with tests for create, update, status transitions, and authorization checks.

---

### Task 2.2: Question Logic Engine (Q-301~Q-305)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/survey/service/SurveyLogicService.java`

- [ ] **Step 1: Implement logic resolution**

```java
@Service
public class SurveyLogicService {

    /**
     * For a given answer, determine which question should be shown next.
     * Applies: jump logic → show/hide logic → randomized order
     */
    public Long resolveNextQuestion(Long currentQuestionId, Answer currentAnswer,
                                     List<Question> allQuestions, List<Answer> allPreviousAnswers) {
        Question currentQ = findById(allQuestions, currentQuestionId);

        // 1. Check jump logic
        if (currentQ.getLogicJump() != null && currentAnswer != null) {
            JSONArray jumps = JSONUtil.parseArray(currentQ.getLogicJump());
            for (int i = 0; i < jumps.size(); i++) {
                JSONObject jump = jumps.getJSONObject(i);
                Long optionId = jump.getLong("option_id");
                Long jumpTo = jump.getLong("jump_to");
                if (currentAnswer.getAnswerOptions() != null
                        && currentAnswer.getAnswerOptions().contains(optionId)) {
                    return jumpTo; // jump_to could be null meaning "submit"
                }
            }
        }

        // 2. Get next question in order (respecting randomization)
        List<Question> orderedQuestions = getOrderedQuestions(allQuestions);
        int currentIdx = indexOf(orderedQuestions, currentQuestionId);
        if (currentIdx + 1 < orderedQuestions.size()) {
            Long nextId = orderedQuestions.get(currentIdx + 1).getId();

            // 3. Check show/hide logic on the next question
            if (shouldShowQuestion(nextId, allQuestions, allPreviousAnswers)) {
                return nextId;
            }
            // Recurse to skip hidden questions
            return resolveNextQuestion(nextId, null, allQuestions, allPreviousAnswers);
        }

        return null; // End of survey
    }

    private boolean shouldShowQuestion(Long questionId, List<Question> allQuestions,
                                        List<Answer> previousAnswers) {
        Question q = findById(allQuestions, questionId);
        if (q.getLogicShow() == null) return true;

        JSONObject logic = JSONUtil.parseObj(q.getLogicShow());
        String operator = logic.getStr("operator", "AND");
        JSONArray conditions = logic.getJSONArray("conditions");

        for (int i = 0; i < conditions.size(); i++) {
            JSONObject cond = conditions.getJSONObject(i);
            boolean met = evaluateCondition(cond, previousAnswers);
            if ("AND".equals(operator) && !met) return false;
            if ("OR".equals(operator) && met) return true;
        }
        return "AND".equals(operator);
    }

    /**
     * Apply randomization to questions based on survey settings.
     * Some questions may be marked as "fixed position" (never randomized).
     */
    public List<Question> getOrderedQuestions(List<Question> allQuestions) {
        // Sort by order_index first
        List<Question> sorted = allQuestions.stream()
            .sorted(Comparator.comparingInt(Question::getOrderIndex))
            .collect(Collectors.toList());

        // If survey has randomization, shuffle non-fixed questions
        // Keep logical order integrity (jump targets must remain after source)
        // Return final order
        return sorted;
    }

    /**
     * Randomize options for a specific question, respecting "fixed" options.
     */
    public List<Option> getOrderedOptions(Question question) {
        if (question.getIsRandomOptions() == 0) {
            return parseOptions(question.getOptions());
        }
        List<Option> options = parseOptions(question.getOptions());
        // Separate fixed items (e.g., "以上都不是" always last)
        List<Option> fixed = options.stream().filter(Option::isFixed).collect(Collectors.toList());
        List<Option> random = options.stream().filter(o -> !o.isFixed()).collect(Collectors.toList());
        Collections.shuffle(random);
        random.addAll(fixed);
        return random;
    }
}
```

- [ ] **Step 2: Write unit tests covering all logic paths**

Test cases:
- Simple jump: selecting option A jumps to Q5
- Multi-condition show/hide: Q4 visible only when Q2=A AND Q3>18
- Randomization: verify random order differs from original, fixed items stay in place
- Circular jump detection: prevent infinite loops
- Quota limit: option becomes unavailable when quota reached

---

### Task 2.3: AI Survey Generation (Q-002, Q-005)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/survey/service/AiGenerationService.java`
- Create: `src/main/java/com/smartsurvey/module/survey/controller/AiController.java`
- Create: `src/main/java/com/smartsurvey/module/survey/dto/AiGenerateRequest.java`
- Create: `src/main/java/com/smartsurvey/module/survey/dto/AiDiagnoseRequest.java`

- [ ] **Step 1: Define AI generation interface**

```java
package com.smartsurvey.module.survey.service;

import com.smartsurvey.module.survey.dto.*;

public interface AiGenerationService {

    /**
     * Generate a complete survey from natural language description.
     * Returns a survey draft ready for user review/edit.
     */
    AiGenerateResponse generateSurvey(AiGenerateRequest req);

    /**
     * Diagnose an existing survey for quality issues.
     * Checks: option completeness, logical conflicts, wording bias,
     *         question redundancy, missing demographics, estimated duration.
     */
    AiDiagnoseResponse diagnoseSurvey(Long surveyId);

    /**
     * Get AI optimization suggestions for a single question.
     */
    String suggestQuestionImprovement(Long questionId);
}
```

- [ ] **Step 2: Implement AiGenerationServiceImpl with prompt templates**

```java
@Service
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {
    // Phase 2: Implement with template-based generation (rule-based)
    // Phase 5: Upgrade to LLM-based generation

    @Override
    public AiGenerateResponse generateSurvey(AiGenerateRequest req) {
        // V1: Template matching + rule-based generation
        // 1. Parse user intent from natural language prompt using keyword extraction
        // 2. Match to closest template category (academic/market/hr/product/satisfaction)
        // 3. Fill template with context-specific content
        // 4. Apply question logic (jumps, randomization) based on industry best practices
        // 5. Score the result and return with improvement suggestions

        SurveyDetailResponse survey = buildSurveyFromTemplate(matchTemplate(req.getPrompt()),
                                                               req.getIndustry(), req.getQuestionCount());
        AiGenerateResponse resp = new AiGenerateResponse();
        resp.setSurveyDraft(survey);
        resp.setAiScore(7.5);
        resp.setEstimatedTimeMinutes(estimateTime(survey));
        resp.setSuggestions(generateImprovementTips(survey));
        return resp;
    }

    @Override
    public AiDiagnoseResponse diagnoseSurvey(Long surveyId) {
        Survey survey = surveyMapper.selectById(surveyId);
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);

        AiDiagnoseResponse report = new AiDiagnoseResponse();
        List<DiagnosisItem> items = new ArrayList<>();

        // Check 1: Title too short/long
        if (survey.getTitle().length() < 5) {
            items.add(new DiagnosisItem("title", "warning", "标题过短，建议至少5个字"));
        }

        // Check 2: Missing demographic questions
        boolean hasAge = questions.stream().anyMatch(q -> q.getContent().contains("年龄"));
        boolean hasGender = questions.stream().anyMatch(q -> q.getContent().contains("性别"));
        if (!hasAge && !hasGender) {
            items.add(new DiagnosisItem("demographics", "suggestion", "建议添加年龄/性别等人口统计问题"));
        }

        // Check 3: Estimate completion time
        int estimatedSeconds = questions.stream().mapToInt(this::estimateQuestionTime).sum();
        if (estimatedSeconds > 900) { // 15 minutes
            items.add(new DiagnosisItem("duration", "warning",
                String.format("预计答题时长%d分钟，超过15分钟建议精简或增加奖励", estimatedSeconds / 60)));
        }

        // Check 4: Detect option overlap
        for (Question q : questions) {
            if ("single".equals(q.getType()) || "multiple".equals(q.getType())) {
                List<String> overlaps = detectOptionOverlap(q.getOptions());
                for (String overlap : overlaps) {
                    items.add(new DiagnosisItem("q" + q.getId(), "error", overlap));
                }
            }
        }

        report.setItems(items);
        report.setEstimatedDurationSeconds(estimatedSeconds);
        return report;
    }

    private int estimateQuestionTime(Question q) {
        switch (q.getType()) {
            case "single": return 8;
            case "multiple": return 12;
            case "judge": return 5;
            case "fill": return 15;
            case "essay": return 40;
            case "rating": return 10;
            case "matrix": return 30 + 5 * countMatrixRows(q.getOptions());
            case "ranking": return 20;
            default: return 10;
        }
    }
}
```

- [ ] **Step 3: Create AiController**

```java
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiGenerationService aiService;

    @PostMapping("/generate-survey")
    public ApiResponse<AiGenerateResponse> generate(@Valid @RequestBody AiGenerateRequest req) {
        return ApiResponse.ok(aiService.generateSurvey(req));
    }

    @PostMapping("/diagnose/{surveyId}")
    public ApiResponse<AiDiagnoseResponse> diagnose(@PathVariable Long surveyId) {
        return ApiResponse.ok(aiService.diagnoseSurvey(surveyId));
    }
}
```

---

### Task 2.4: Survey Template System (Q-003)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/survey/service/TemplateService.java`
- Create: `src/main/java/com/smartsurvey/module/survey/controller/TemplateController.java`

- [ ] **Step 1: Seed 30+ free templates covering all categories**

Store templates as JSON files in `src/main/resources/templates/` or as database seed data. Each template contains pre-defined questions with types, options, and basic logic.

- [ ] **Step 2: Implement TemplateService (list, search, preview, apply)**

---

## Phase 3: Response Collection (Day 13-17)

Covers: Answer submission, anti-cheat, quality scoring

### Task 3.1: Answer Submission Flow

**Files:**
- Create: `src/main/java/com/smartsurvey/module/response/entity/Response.java`
- Create: `src/main/java/com/smartsurvey/module/response/entity/Answer.java`
- Create: `src/main/java/com/smartsurvey/module/response/mapper/ResponseMapper.java`
- Create: `src/main/java/com/smartsurvey/module/response/mapper/AnswerMapper.java`
- Create: `src/main/java/com/smartsurvey/module/response/service/ResponseService.java`
- Create: `src/main/java/com/smartsurvey/module/response/service/AnswerService.java`
- Create: `src/main/java/com/smartsurvey/module/response/controller/ResponseController.java`
- Create: `src/main/java/com/smartsurvey/module/response/dto/StartResponseRequest.java`
- Create: `src/main/java/com/smartsurvey/module/response/dto/SubmitAnswerRequest.java`

- [ ] **Step 1: Create Response entity**

```java
@Data
@TableName("responses")
public class Response {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long surveyId;
    private Long userId;
    private String ipAddress;
    private String deviceFingerprint;
    private String userAgent;
    private String channel;            // marketplace/share/direct
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationSeconds;
    private BigDecimal rewardAmount;
    private String status;             // in_progress/submitted/approved/rejected/expired
    private String reviewType;         // auto/manual
    private String reviewNote;
    private BigDecimal qualityScore;   // 0-10
    private String behaviorData;       // JSON: per-question timing, modification counts
    private LocalDateTime createdAt;
}
```

- [ ] **Step 2: Implement startResponse — begin answering a survey**

```java
public StartResponseResponse startResponse(Long userId, StartResponseRequest req) {
    Survey survey = surveyMapper.selectById(req.getSurveyId());
    if (survey == null) throw new BusinessException(ErrorCode.SURVEY_NOT_FOUND);
    if (!"published".equals(survey.getStatus())) throw new BusinessException(ErrorCode.BAD_REQUEST);

    // Check quota remaining
    if (survey.getRemainingQuota() != null && survey.getRemainingQuota() <= 0) {
        throw new BusinessException(ErrorCode.QUOTA_FULL);
    }

    // Check user hasn't exceeded max attempts
    Long existingCount = responseMapper.selectCount(new LambdaQueryWrapper<Response>()
        .eq(Response::getSurveyId, req.getSurveyId())
        .eq(Response::getUserId, userId)
        .ne(Response::getStatus, "expired"));
    if (existingCount >= survey.getMaxAttempts()) {
        throw new BusinessException(ErrorCode.DUPLICATE_SUBMIT);
    }

    Response response = new Response();
    response.setSurveyId(req.getSurveyId());
    response.setUserId(userId);
    response.setStatus("in_progress");
    response.setStartTime(LocalDateTime.now());
    response.setChannel(req.getChannel());
    response.setCreatedAt(LocalDateTime.now());
    responseMapper.insert(response);

    // Get all questions (respecting randomization + logic)
    List<Question> questions = questionMapper.selectBySurveyId(req.getSurveyId());
    List<Question> orderedQuestions = surveyLogicService.getOrderedQuestions(questions);

    StartResponseResponse resp = new StartResponseResponse();
    resp.setResponseId(response.getId());
    resp.setFirstQuestion(buildQuestionResponse(orderedQuestions.get(0), userId));
    resp.setTotalQuestions(orderedQuestions.size());
    resp.setExpireAt(LocalDateTime.now().plusMinutes(survey.getTimeLimitMinutes() > 0
        ? survey.getTimeLimitMinutes() : 15));
    return resp;
}
```

- [ ] **Step 3: Implement submitAnswer — submit answers for one or more questions**

```java
@Transactional
public void submitAnswers(Long userId, Long responseId, SubmitAnswerRequest req) {
    Response response = responseMapper.selectById(responseId);
    if (response == null || !response.getUserId().equals(userId)) {
        throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    if (!"in_progress".equals(response.getStatus())) {
        throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    // Check expiration
    Survey survey = surveyMapper.selectById(response.getSurveyId());
    if (survey.getTimeLimitMinutes() > 0) {
        long elapsed = Duration.between(response.getStartTime(), LocalDateTime.now()).toSeconds();
        if (elapsed > survey.getTimeLimitMinutes() * 60L) {
            response.setStatus("expired");
            responseMapper.updateById(response);
            throw new BusinessException(ErrorCode.ANSWER_EXPIRED);
        }
    }

    // Upsert answers
    for (SubmitAnswerRequest.AnswerItem item : req.getAnswers()) {
        Answer answer = answerMapper.selectOne(new LambdaQueryWrapper<Answer>()
            .eq(Answer::getResponseId, responseId)
            .eq(Answer::getQuestionId, item.getQuestionId()));
        if (answer == null) {
            answer = new Answer();
            answer.setResponseId(responseId);
            answer.setQuestionId(item.getQuestionId());
        }
        answer.setAnswerText(item.getAnswerText());
        answer.setAnswerOptions(item.getAnswerOptions() != null
            ? JSONUtil.toJsonStr(item.getAnswerOptions()) : null);
        answer.setAnswerRating(item.getAnswerRating());
        answer.setCreatedAt(LocalDateTime.now());
        answerMapper.insertOrUpdate(answer);
    }

    // Record behavior data (timing, modifications)
    if (req.getBehaviorData() != null) {
        response.setBehaviorData(updateBehaviorData(response.getBehaviorData(), req.getBehaviorData()));
        responseMapper.updateById(response);
    }

    // Determine next question
    Long nextQuestionId = surveyLogicService.resolveNextQuestion(
        req.getLastAnsweredQuestionId(), /* ... */);
    // Return next question ID in response for frontend navigation
}
```

- [ ] **Step 4: Implement submitResponse — finalize the response**

```java
@Transactional
public void submitResponse(Long userId, Long responseId) {
    Response response = responseMapper.selectById(responseId);
    if (!response.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);

    // Verify all required questions answered
    List<Question> questions = questionMapper.selectBySurveyId(response.getSurveyId());
    List<Answer> answers = answerMapper.selectByResponseId(responseId);
    for (Question q : questions) {
        if (q.getRequired() == 1) {
            boolean answered = answers.stream().anyMatch(a -> a.getQuestionId().equals(q.getId()));
            if (!answered) {
                throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(),
                    "题目『" + q.getContent() + "』为必答题");
            }
        }
    }

    // Record completion
    response.setEndTime(LocalDateTime.now());
    response.setDurationSeconds((int) Duration.between(
        response.getStartTime(), response.getEndTime()).getSeconds());
    response.setStatus("submitted");
    responseMapper.updateById(response);

    // Async: trigger anti-cheat check + auto review
    antiCheatService.evaluateAsync(response);
}
```

---

### Task 3.2: Anti-Cheat System (D-401~D-403)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/response/service/AntiCheatService.java`
- Create: `src/main/java/com/smartsurvey/common/utils/DeviceFingerprintUtils.java`

- [ ] **Step 1: Implement AntiCheatService**

```java
@Service
@Slf4j
public class AntiCheatService {

    @Async
    public void evaluateAsync(Response response) {
        int riskScore = 0;
        StringBuilder reason = new StringBuilder();

        // Check 1: Duration anomaly
        // (completed too fast compared to estimated minimum)
        Survey survey = surveyMapper.selectById(response.getSurveyId());
        int minExpectedSeconds = estimateMinimumDuration(survey);
        if (response.getDurationSeconds() < minExpectedSeconds * 0.3) {
            riskScore += 30;
            reason.append("完成时间异常短(").append(response.getDurationSeconds()).append("s); ");
        }

        // Check 2: Pattern answers (all first option / alternating)
        List<Answer> answers = answerMapper.selectByResponseId(response.getId());
        if (isPatternAnswer(answers)) {
            riskScore += 40;
            reason.append("检测到规律性选择; ");
        }

        // Check 3: Text quality for open-ended questions
        for (Answer a : answers) {
            Question q = questionMapper.selectById(a.getQuestionId());
            if (("essay".equals(q.getType()) || "fill".equals(q.getType()))
                    && a.getAnswerText() != null) {
                if (a.getAnswerText().length() < 3 || isGibberish(a.getAnswerText())) {
                    riskScore += 20;
                    reason.append("开放题回答质量低; ");
                }
                if (isPastedText(a.getAnswerText(), response.getBehaviorData())) {
                    riskScore += 15;
                    reason.append("检测到粘贴行为; ");
                }
            }
        }

        // Check 4: IP/device fingerprint multi-account
        if (response.getDeviceFingerprint() != null) {
            int accountsOnDevice = responseMapper.countAccountsByFingerprint(
                response.getDeviceFingerprint(), response.getSurveyId());
            if (accountsOnDevice > 2) {
                riskScore += 25;
                reason.append("同设备多账号回答; ");
            }
        }

        // Decision
        if (riskScore >= 60) {
            rejectResponse(response.getId(), reason.toString());
        } else if (riskScore >= 30) {
            markForManualReview(response.getId(), reason.toString());
        } else {
            approveResponse(response.getId());
        }
    }

    private boolean isPatternAnswer(List<Answer> answers) {
        // Detect: all first option, alternating A/B, straight-line selections
        List<String> optionSeqs = answers.stream()
            .filter(a -> a.getAnswerOptions() != null)
            .map(Answer::getAnswerOptions)
            .collect(Collectors.toList());

        // Check if all selections are the same (e.g., all "A")
        if (optionSeqs.size() >= 5) {
            Set<String> unique = new HashSet<>(optionSeqs);
            if (unique.size() == 1) return true;
        }

        return false;
    }

    @Transactional
    public void approveResponse(Long responseId) {
        Response r = responseMapper.selectById(responseId);
        r.setStatus("approved");
        r.setReviewType("auto");
        r.setQualityScore(calculateQualityScore(r));
        r.setReviewNote("自动审核通过");
        responseMapper.updateById(r);

        // Grant reward
        rewardService.grantReward(r);
    }

    @Transactional
    public void rejectResponse(Long responseId, String reason) {
        Response r = responseMapper.selectById(responseId);
        r.setStatus("rejected");
        r.setReviewType("auto");
        r.setReviewNote(reason);
        r.setQualityScore(BigDecimal.ZERO);
        responseMapper.updateById(r);

        // Deduct reputation
        reputationService.deduct(r.getUserId(), 5, "回答质量不合格");

        // Release quota back
        surveyService.incrementRemainingQuota(r.getSurveyId());
    }
}
```

---

### Task 3.3: Reward Granting (D-201~D-203)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/incentive/service/RewardService.java`

- [ ] **Step 1: Implement RewardService**

```java
@Service
@RequiredArgsConstructor
public class RewardService {
    private final TransactionService transactionService;
    private final UserMapper userMapper;

    @Transactional
    public void grantReward(Response response) {
        Survey survey = surveyMapper.selectById(response.getSurveyId());
        BigDecimal reward = calculateReward(survey, response);

        if (reward.compareTo(BigDecimal.ZERO) <= 0) return;

        // Deduct from survey creator
        User creator = userMapper.selectById(survey.getUserId());
        creator.setBalance(creator.getBalance().subtract(reward));
        userMapper.updateById(creator);

        // Add to responder
        User responder = userMapper.selectById(response.getUserId());
        responder.setBalance(responder.getBalance().add(reward));
        userMapper.updateById(responder);

        // Create transaction records
        transactionService.createRewardTransaction(creator.getId(), reward.negate(),
            response.getId(), "问卷奖励支出");
        transactionService.createRewardTransaction(responder.getId(), reward,
            response.getId(), "回答奖励收入");

        // Update response
        response.setRewardAmount(reward);
        responseMapper.updateById(response);
    }

    private BigDecimal calculateReward(Survey survey, Response response) {
        BigDecimal base = survey.getRewardPerResponse();
        if ("tiered".equals(survey.getRewardType())) {
            BigDecimal qualityMult = BigDecimal.ONE;
            if (response.getQualityScore() != null
                    && response.getQualityScore().compareTo(new BigDecimal("8")) >= 0) {
                qualityMult = new BigDecimal("1.5");
            }
            BigDecimal speedMult = BigDecimal.ONE;
            // speed bonus for first N respondents handled elsewhere
            return base.multiply(qualityMult.add(speedMult).subtract(BigDecimal.ONE))
                       .min(base.multiply(new BigDecimal("2"))); // cap at 2x
        }
        return base;
    }
}
```

---

## Phase 4: Marketplace & Incentive System (Day 18-24)

Covers requirements: D-101~D-104, M5 all

### Task 4.1: Survey Marketplace (S-101~S-104)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/marketplace/service/MarketplaceService.java`
- Create: `src/main/java/com/smartsurvey/module/marketplace/controller/MarketplaceController.java`
- Create: `src/main/java/com/smartsurvey/module/marketplace/dto/MarketplaceQuery.java`
- Create: `src/main/java/com/smartsurvey/module/marketplace/dto/SurveyCardResponse.java`

- [ ] **Step 1: Implement marketplace listing with multi-dimensional sort**

```java
@Service
@RequiredArgsConstructor
public class MarketplaceService {
    private final SurveyMapper surveyMapper;
    private final RedisUtils redisUtils;

    public PageResult<SurveyCardResponse> listSurveys(MarketplaceQuery query) {
        // Build dynamic query:
        // - Category filter: industry tags on survey
        // - Reward range filter
        // - Duration range filter
        // - Sort: recommended (default) / newest / highest_reward / ending_soon
        // - Only published surveys with remaining_quota > 0 and audit_status=approved

        Page<Survey> page = new Page<>(query.getPage(), query.getSize());
        LambdaQueryWrapper<Survey> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Survey::getStatus, "published")
               .eq(Survey::getAuditStatus, "approved")
               .gt(Survey::getRemainingQuota, 0);

        if ("recommended".equals(query.getSort())) {
            // Recommendation: personalized rank based on user profile
            // Weight: user_industry_match(40%) + reward(25%) + freshness(20%) + completion_rate(15%)
            wrapper.orderByDesc(Survey::getRewardPerResponse);
        } else if ("newest".equals(query.getSort())) {
            wrapper.orderByDesc(Survey::getCreatedAt);
        } else if ("highest_reward".equals(query.getSort())) {
            wrapper.orderByDesc(Survey::getRewardPerResponse);
        } else if ("ending_soon".equals(query.getSort())) {
            wrapper.orderByAsc(Survey::getEndTime);
        }

        Page<Survey> result = surveyMapper.selectPage(page, wrapper);
        return convertToCards(result);
    }

    public SurveyDetailForRespondent getSurveyDetail(Long surveyId, Long userId) {
        Survey survey = surveyMapper.selectById(surveyId);
        // Check if user has already claimed/answered
        boolean hasAnswered = responseMapper.hasUserAnswered(surveyId, userId);

        SurveyDetailForRespondent detail = new SurveyDetailForRespondent();
        detail.setSurvey(BasicSurveyInfo.from(survey));
        detail.setHasAnswered(hasAnswered);
        detail.setPreviewQuestions(getFirstTwoQuestions(surveyId)); // Show 2 preview questions
        detail.setRemainingQuota(survey.getRemainingQuota());
        return detail;
    }

    @Transactional
    public ClaimResponse claimSurvey(Long surveyId, Long userId) {
        Survey survey = surveyMapper.selectById(surveyId);

        // Validate: quota remaining, not expired, user not already claimed, user not frozen
        if (survey.getRemainingQuota() <= 0)
            throw new BusinessException(ErrorCode.QUOTA_FULL);
        if (survey.getEndTime() != null && survey.getEndTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "问卷已截止");

        // Check user's active claims (max 5 in-progress)
        Long activeCount = responseMapper.selectCount(new LambdaQueryWrapper<Response>()
            .eq(Response::getUserId, userId)
            .eq(Response::getStatus, "in_progress"));
        if (activeCount >= 5)
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "您有5个待完成的任务，请先完成再抢新单");

        // Decrement quota atomically
        int updated = surveyMapper.decrementQuota(surveyId);
        if (updated == 0) throw new BusinessException(ErrorCode.QUOTA_FULL);

        // Create response record (in_progress)
        Response response = new Response();
        response.setSurveyId(surveyId);
        response.setUserId(userId);
        response.setStatus("in_progress");
        response.setStartTime(LocalDateTime.now());
        response.setChannel("marketplace");
        response.setCreatedAt(LocalDateTime.now());
        responseMapper.insert(response);

        ClaimResponse resp = new ClaimResponse();
        resp.setResponseId(response.getId());
        resp.setExpireAt(LocalDateTime.now().plusMinutes(15));
        return resp;
    }
}
```

- [ ] **Step 2: Implement recommendation algorithm for "recommended" sort**

```java
@Service
public class RecommendationService {

    public double calculateMatchScore(Survey survey, User user) {
        double score = 0.0;

        // Industry match (30%)
        if (user.getTags() != null && survey.getTargetAudience() != null) {
            List<String> userTags = JSONUtil.toList(user.getTags(), String.class);
            JSONObject audience = JSONUtil.parseObj(survey.getTargetAudience());
            List<String> surveyIndustries = audience.getJSONArray("industries").toList(String.class);
            long overlap = userTags.stream().filter(surveyIndustries::contains).count();
            if (overlap > 0) score += 30.0 * overlap / Math.max(userTags.size(), surveyIndustries.size());
        }

        // Reward attractiveness (25%)
        if (survey.getRewardPerResponse() != null) {
            double maxReward = 10.0; // normalize
            score += 25.0 * Math.min(survey.getRewardPerResponse().doubleValue() / maxReward, 1.0);
        }

        // Freshness (20%)
        long hoursSinceCreation = Duration.between(survey.getCreatedAt(), LocalDateTime.now()).toHours();
        score += 20.0 * Math.max(0, 1.0 - hoursSinceCreation / 168.0); // decay over 7 days

        // Completion rate quality signal (15%)
        if (survey.getTotalResponses() > 0) {
            double completionRate = (double) survey.getTotalResponses()
                / (survey.getTotalResponses() + survey.getRemainingQuota());
            score += 15.0 * Math.min(completionRate, 1.0);
        }

        // Diversity bonus (10%) - avoid showing same industry repeatedly
        // Calculated at listing time based on recent views

        return score;
    }
}
```

- [ ] **Step 3: Create MarketplaceController**

```java
@RestController
@RequestMapping("/api/v1/marketplace")
@RequiredArgsConstructor
public class MarketplaceController {
    private final MarketplaceService marketplaceService;

    @GetMapping("/surveys")
    public ApiResponse<PageResult<SurveyCardResponse>> listSurveys(
            @CurrentUser Long userId, MarketplaceQuery query) {
        return ApiResponse.ok(marketplaceService.listSurveys(query));
    }

    @GetMapping("/surveys/{id}")
    public ApiResponse<SurveyDetailForRespondent> getDetail(
            @CurrentUser Long userId, @PathVariable Long id) {
        return ApiResponse.ok(marketplaceService.getSurveyDetail(id, userId));
    }

    @PostMapping("/surveys/{id}/claim")
    public ApiResponse<ClaimResponse> claim(
            @CurrentUser Long userId, @PathVariable Long id) {
        return ApiResponse.ok(marketplaceService.claimSurvey(id, userId));
    }

    @GetMapping("/recommended")
    public ApiResponse<List<SurveyCardResponse>> getDailyRecommendations(
            @CurrentUser Long userId) {
        return ApiResponse.ok(marketplaceService.getDailyRecommendations(userId));
    }
}
```

---

### Task 4.2: Gamification — Leaderboard, Check-in, Badges (S-203~S-205)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/marketplace/service/GamificationService.java`
- Create: `src/main/java/com/smartsurvey/module/marketplace/controller/GamificationController.java`

- [ ] **Step 1: Implement leaderboard (Redis sorted set)**

```java
@Service
@RequiredArgsConstructor
public class GamificationService {
    private final RedisUtils redisUtils;
    private final ResponseMapper responseMapper;

    private static final String LEADERBOARD_DAILY = "leaderboard:daily:";
    private static final String LEADERBOARD_WEEKLY = "leaderboard:weekly:";
    private static final String LEADERBOARD_MONTHLY = "leaderboard:monthly:";

    // Called on each approved response
    public void updateLeaderboard(Long userId, BigDecimal reward) {
        String dateKey = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        redisUtils.zincrby(LEADERBOARD_DAILY + dateKey, reward.doubleValue(), userId.toString());
        redisUtils.expire(LEADERBOARD_DAILY + dateKey, 48, TimeUnit.HOURS);

        String weekKey = YearWeek.now().toString();
        redisUtils.zincrby(LEADERBOARD_WEEKLY + weekKey, reward.doubleValue(), userId.toString());

        String monthKey = YearMonth.now().toString();
        redisUtils.zincrby(LEADERBOARD_MONTHLY + monthKey, reward.doubleValue(), userId.toString());
    }

    public LeaderboardResponse getTopUsers(String period, int topN) {
        String key = getPeriodKey(period);
        Set<ZSetOperations.TypedTuple<String>> top = redisUtils.zrevrangeWithScores(key, 0, topN - 1);
        // Map to LeaderboardEntry list with user info
        return buildResponse(top, period);
    }
}
```

- [ ] **Step 2: Implement daily check-in with Redis bitmap**

```java
public CheckInResponse checkIn(Long userId) {
    String key = "checkin:" + YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
    long dayOfMonth = LocalDate.now().getDayOfMonth();

    Boolean alreadySet = redisUtils.setbit(key, dayOfMonth - 1, true);
    if (Boolean.TRUE.equals(alreadySet)) {
        throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "今日已签到");
    }

    // Calculate streak
    int streak = calculateStreak(key, dayOfMonth);
    int points = streak >= 7 ? 20 : 5;

    // Grant bonus for streaks
    if (streak == 7) {
        // Extra reward for 7-day streak
    }

    return new CheckInResponse(streak, points);
}
```

- [ ] **Step 3: Implement badge system**

```java
public void checkAndGrantBadges(Long userId) {
    // Check each badge condition:
    // - "百答达人": response count >= 100
    // - "质量之星": consecutive approved >= 50
    // - "闪电侠": avg completion speed in top 10%
    // - etc.

    long totalResponses = responseMapper.countApprovedByUser(userId);
    if (totalResponses >= 100) {
        grantBadge(userId, "百答达人", "完成100份问卷回答");
    }

    int consecutiveApproved = responseMapper.countConsecutiveApproved(userId);
    if (consecutiveApproved >= 50) {
        grantBadge(userId, "质量之星", "连续50份回答审核通过");
    }
}
```

---

### Task 4.3: Survey Audit System (S-301~S-303)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/marketplace/service/AuditService.java`

- [ ] **Step 1: Implement auto-audit pipeline on survey publish**

```java
@Service
public class AuditService {
    private final SensitiveWordChecker sensitiveWordChecker;

    public AuditResult autoAudit(Survey survey, List<Question> questions) {
        AuditResult result = new AuditResult();
        List<String> issues = new ArrayList<>();

        // 1. Sensitive word scan on title, description, all questions, all options
        if (sensitiveWordChecker.containsSensitive(survey.getTitle())) {
            issues.add("标题包含敏感词");
        }
        for (Question q : questions) {
            if (sensitiveWordChecker.containsSensitive(q.getContent())) {
                issues.add("题目「" + truncate(q.getContent(), 30) + "」包含敏感词");
            }
            if (q.getOptions() != null) {
                JSONArray options = JSONUtil.parseArray(q.getOptions());
                for (int i = 0; i < options.size(); i++) {
                    if (sensitiveWordChecker.containsSensitive(options.getJSONObject(i).getStr("text"))) {
                        issues.add("题目" + q.getOrderIndex() + "的选项包含敏感词");
                    }
                }
            }
        }

        // 2. Content completeness
        if (survey.getTitle() == null || survey.getTitle().trim().isEmpty()) {
            issues.add("缺少问卷标题");
        }
        if (questions.size() < 3) {
            issues.add("题目数量不足（至少3题）");
        }
        for (Question q : questions) {
            if (q.getContent() == null || q.getContent().trim().isEmpty()) {
                issues.add("第" + q.getOrderIndex() + "题缺少题目内容");
            }
            if (("single".equals(q.getType()) || "multiple".equals(q.getType()))
                    && (q.getOptions() == null || JSONUtil.parseArray(q.getOptions()).size() < 2)) {
                issues.add("第" + q.getOrderIndex() + "题选项数量不足");
            }
        }

        // 3. Reward reasonableness
        if (survey.getRewardPerResponse() != null
                && survey.getRewardPerResponse().compareTo(new BigDecimal("0.5")) < 0) {
            issues.add("每份奖励金额不能低于0.5元");
        }

        // 4. Determine if manual review needed
        boolean needsManual = survey.getRewardPerResponse() != null
                && survey.getRewardPerResponse().compareTo(new BigDecimal("10")) >= 0;
        // Also: sensitive topics, new user's first survey (<7 days)

        result.setPassed(issues.isEmpty());
        result.setIssues(issues);
        result.setNeedsManualReview(needsManual);
        return result;
    }
}
```

---

## Phase 5: Analytics & Insights System (Day 25-30)

Covers requirements: M4 all (A-101~A-403)

### Task 5.1: Real-time Statistics (A-101~A-103)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/analytics/service/StatisticsService.java`
- Create: `src/main/java/com/smartsurvey/module/analytics/controller/AnalyticsController.java`

- [ ] **Step 1: Implement StatisticsService with caching**

```java
@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final QuestionMapper questionMapper;
    private final RedisUtils redisUtils;

    public SurveyStatistics getStatistics(Long surveyId) {
        // Try cache first (5 min TTL)
        String cacheKey = "stats:survey:" + surveyId;
        String cached = redisUtils.get(cacheKey);
        if (cached != null) {
            return JSONUtil.toBean(cached, SurveyStatistics.class);
        }

        SurveyStatistics stats = new SurveyStatistics();

        // Overview
        List<Response> responses = responseMapper.selectList(
            new LambdaQueryWrapper<Response>()
                .eq(Response::getSurveyId, surveyId)
                .eq(Response::getStatus, "approved"));
        stats.setTotalResponses(responses.size());
        stats.setTotalViews(responseMapper.countViews(surveyId));
        stats.setCompletionRate(stats.getTotalViews() > 0
            ? (double) stats.getTotalResponses() / stats.getTotalViews() * 100 : 0);
        stats.setAvgDurationSeconds(responses.stream()
            .mapToInt(Response::getDurationSeconds)
            .average().orElse(0));

        // Per-question analysis
        List<Question> questions = questionMapper.selectBySurveyId(surveyId);
        List<QuestionAnalysis> questionStats = new ArrayList<>();
        for (Question q : questions) {
            QuestionAnalysis qa = analyzeQuestion(q, responses);
            questionStats.add(qa);
        }
        stats.setQuestionAnalysis(questionStats);

        // Daily trend (last 14 days)
        stats.setDailyTrend(getDailyResponseTrend(surveyId, 14));

        // Channel breakdown
        stats.setChannelBreakdown(getChannelBreakdown(surveyId, responses));

        // Cache for 5 minutes
        redisUtils.setex(cacheKey, 300, JSONUtil.toJsonStr(stats));
        return stats;
    }

    private QuestionAnalysis analyzeQuestion(Question q, List<Response> responses) {
        QuestionAnalysis qa = new QuestionAnalysis();
        qa.setQuestionId(q.getId());
        qa.setQuestionContent(q.getContent());
        qa.setType(q.getType());

        List<Answer> answers = answerMapper.selectByQuestionIdAndResponses(
            q.getId(), responses.stream().map(Response::getId).collect(Collectors.toList()));

        switch (q.getType()) {
            case "single":
            case "multiple":
            case "judge":
                Map<String, Long> distribution = answers.stream()
                    .flatMap(a -> parseOptionIds(a.getAnswerOptions()).stream())
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                qa.setDistribution(distribution);
                break;
            case "rating":
                qa.setAvgRating(answers.stream()
                    .mapToInt(Answer::getAnswerRating)
                    .average().orElse(0));
                qa.setRatingDistribution(answers.stream()
                    .collect(Collectors.groupingBy(Answer::getAnswerRating, Collectors.counting())));
                break;
            case "fill":
            case "essay":
                qa.setTextCount(answers.size());
                qa.setWordCloud(generateWordCloud(answers));
                break;
            case "matrix":
                qa.setMatrixHeatmap(generateMatrixHeatmap(q, answers));
                break;
            case "ranking":
                qa.setRankingScores(calculateRankingScores(q, answers));
                break;
        }
        return qa;
    }
}
```

- [ ] **Step 2: Implement AnalyticsController**

```java
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/surveys/{surveyId}")
    public ApiResponse<SurveyStatistics> getStatistics(@PathVariable Long surveyId) {
        return ApiResponse.ok(statisticsService.getStatistics(surveyId));
    }

    @GetMapping("/surveys/{surveyId}/questions/{questionId}")
    public ApiResponse<QuestionAnalysis> getQuestionAnalysis(
            @PathVariable Long surveyId, @PathVariable Long questionId) {
        return ApiResponse.ok(statisticsService.getQuestionAnalysis(surveyId, questionId));
    }
}
```

---

### Task 5.2: AI Analysis (A-201~A-206)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/analytics/service/AiAnalysisService.java`

- [ ] **Step 1: Implement key findings extraction, sentiment analysis, topic clustering, cross-tabulation**

Phase 5 V1 implements keyword-based sentiment analysis and rule-based findings extraction. LLM integration planned for V2.

```java
@Service
public class AiAnalysisService {

    public KeyFindingsResponse getKeyFindings(Long surveyId) {
        SurveyStatistics stats = statisticsService.getStatistics(surveyId);
        List<KeyFinding> findings = new ArrayList<>();

        // Rule 1: Find questions with highly skewed distributions (>70% choosing one option)
        for (QuestionAnalysis qa : stats.getQuestionAnalysis()) {
            if (qa.getDistribution() != null) {
                long total = qa.getDistribution().values().stream().mapToLong(Long::longValue).sum();
                for (Map.Entry<String, Long> entry : qa.getDistribution().entrySet()) {
                    double pct = (double) entry.getValue() / total * 100;
                    if (pct > 70) {
                        findings.add(new KeyFinding(
                            String.format("**%.0f%%** 的受访者在"%s"中选择了"%s"",
                                pct, qa.getQuestionContent(), entry.getKey()),
                            "high", qa.getQuestionId()));
                    }
                }
            }
        }

        // Rule 2: Cross-tabulation significant differences
        // Split by demographic question, find questions where sub-groups differ significantly
        if (stats.getQuestionAnalysis().size() >= 2) {
            List<KeyFinding> crossFindings = generateCrossFindings(stats);
            findings.addAll(crossFindings);
        }

        // Sort by confidence and limit to top 5
        findings.sort(Comparator.comparing(KeyFinding::getConfidence).reversed());
        return new KeyFindingsResponse(findings.subList(0, Math.min(5, findings.size())));
    }

    public SentimentResponse analyzeSentiment(Long surveyId, Long questionId) {
        List<Answer> textAnswers = answerMapper.selectTextAnswers(surveyId, questionId);
        int positive = 0, negative = 0, neutral = 0;
        List<SentimentQuote> topQuotes = new ArrayList<>();

        for (Answer a : textAnswers) {
            String sentiment = analyzeChineseSentiment(a.getAnswerText());
            switch (sentiment) {
                case "positive": positive++; break;
                case "negative": negative++; break;
                default: neutral++;
            }
            // Collect representative quotes
            if (topQuotes.size() < 6) {
                topQuotes.add(new SentimentQuote(a.getAnswerText(), sentiment));
            }
        }

        SentimentResponse resp = new SentimentResponse();
        resp.setPositive(positive);
        resp.setNegative(negative);
        resp.setNeutral(neutral);
        resp.setTopQuotes(topQuotes);
        return resp;
    }

    private String analyzeChineseSentiment(String text) {
        // V1: Keyword-based Chinese sentiment analysis
        // Positive keywords: 好/满意/喜欢/方便/优秀/推荐/不错...
        // Negative keywords: 差/不满/讨厌/麻烦/糟糕/失望/不...
        // V2: Upgrade to NLP model (HanLP/SnowNLP or LLM)
        int posScore = countKeywordMatches(text, POSITIVE_KEYWORDS);
        int negScore = countKeywordMatches(text, NEGATIVE_KEYWORDS);
        if (posScore > negScore * 2) return "positive";
        if (negScore > posScore * 2) return "negative";
        return "neutral";
    }

    public CrossTabResponse crossTabulation(Long surveyId, Long questionXId, Long questionYId) {
        // Generate cross-tabulation table and chi-square test
        // Returns: data table, chart data, statistical significance
    }
}
```

---

### Task 5.3: Report Generation (A-301) & Data Export (A-401)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/analytics/service/ReportService.java`
- Create: `src/main/java/com/smartsurvey/module/analytics/controller/ExportController.java`

- [ ] **Step 1: Implement HTML/PDF report generation using Thymeleaf templates + Flying Saucer**

- [ ] **Step 2: Implement CSV export**

```java
public void exportCsv(Long surveyId, HttpServletResponse response) throws IOException {
    List<Question> questions = questionMapper.selectBySurveyId(surveyId);
    List<Response> responses = responseMapper.selectApprovedBySurveyId(surveyId);

    response.setContentType("text/csv; charset=UTF-8");
    response.setHeader("Content-Disposition", "attachment; filename=survey_" + surveyId + ".csv");

    try (PrintWriter writer = response.getWriter()) {
        // Header row
        writer.println("回答ID,提交时间,耗时(秒)," +
            questions.stream().map(Question::getContent).collect(Collectors.joining(",")));

        // Data rows
        for (Response r : responses) {
            List<Answer> answers = answerMapper.selectByResponseId(r.getId());
            StringBuilder row = new StringBuilder();
            row.append(r.getId()).append(",").append(r.getEndTime()).append(",")
               .append(r.getDurationSeconds()).append(",");
            row.append(questions.stream()
                .map(q -> formatAnswerForCsv(findAnswer(answers, q.getId()), q))
                .collect(Collectors.joining(",")));
            writer.println(row);
        }
    }
}
```

---

## Phase 6: Admin System (Day 31-36)

Covers requirements: M6 all (M-101~M-404)

### Task 6.1: Admin User Management (M-101~M-103)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/admin/service/AdminUserService.java`
- Create: `src/main/java/com/smartsurvey/module/admin/controller/AdminUserController.java`

- [ ] **Step 1: User list with multi-filter, export, ban/unban**

### Task 6.2: Admin Survey Management (M-201~M-203)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/admin/service/AdminSurveyService.java`
- Create: `src/main/java/com/smartsurvey/module/admin/controller/AdminSurveyController.java`

- [ ] **Step 1: Survey audit queue, violation handling, template management**

### Task 6.3: Admin Finance (M-301~M-303)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/admin/service/AdminFinanceService.java`
- Create: `src/main/java/com/smartsurvey/module/admin/controller/AdminFinanceController.java`

- [ ] **Step 1: Transaction query, daily/monthly reconciliation reports, fee rate configuration**

### Task 6.4: Admin Dashboard & Configuration (M-401~M-404)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/admin/service/AdminSystemService.java`
- Create: `src/main/java/com/smartsurvey/module/admin/controller/AdminSystemController.java`

- [ ] **Step 1: RBAC role management, sensitive word library, system announcements, operations dashboard**

---

## Phase 7: Integration, Testing, Deployment (Day 37-42)

### Task 7.1: Notification System (U-203)

**Files:**
- Create: `src/main/java/com/smartsurvey/module/user/service/NotificationService.java`
- Modify: Various services to emit notifications

### Task 7.2: WebSocket Integration for Real-time Stats

**Files:**
- Create: `src/main/java/com/smartsurvey/common/config/WebSocketConfig.java`
- Create: `src/main/java/com/smartsurvey/common/websocket/StatsWebSocketHandler.java`

### Task 7.3: API Documentation (Swagger/Knife4j)

- [ ] Verify all controllers have proper Swagger annotations
- [ ] Generate OpenAPI 3.0 spec at `/api/v1/openapi.json`

### Task 7.4: Comprehensive Test Suite

- [ ] Unit tests for all services (>80% coverage)
- [ ] Integration tests for core flows (register→create survey→publish→claim→answer→verify stats)
- [ ] Performance test: 1000 concurrent answer submissions

### Task 7.5: CI/CD Pipeline

- Create: `Dockerfile`
- Create: `docker-compose.yml` (dev environment with MySQL + Redis + App)
- Create: `.github/workflows/ci.yml` or equivalent

---

## Task Dependency Graph

```
Phase 0: Foundation ─────────────────────────────────────────────────────────────┐
  ├── 0.1 Maven deps                                                             │
  ├── 0.2 App config                                                             │
  ├── 0.3 DB init ───────────────────────┐                                        │
  └── 0.4 Common framework ─────────────┼───────────────────────────────────────┐│
                                          │                                        ││
Phase 1: User System ──────────────────┐│                                        ││
  ├── 1.1 User entity/mapper ◄────────┼┤                                        ││
  ├── 1.2 Auth (register/login) ◄─────┼┤                                        ││
  ├── 1.3 Profile management ◄────────┼┤                                        ││
  ├── 1.4 Social (follow) ────────────┼┼───────────────────────────────────────┐││
  └── 1.5 Wallet/transaction ─────────┼┼──────────────────────────────────────┐│││
                                        ││                                        ││││
Phase 2: Survey Engine ───────────────┐││                                        ││││
  ├── 2.1 Survey CRUD ◄──────────────┼┼┤                                        ││││
  ├── 2.2 Logic engine ◄─────────────┼┼┤                                        ││││
  ├── 2.3 AI generation ◄────────────┼┼┤                                        ││││
  └── 2.4 Templates ─────────────────┼┼┼───────────────────────────────────────┐││││
                                        │││                                        │││││
Phase 3: Response Collection ────────┐│││                                        │││││
  ├── 3.1 Answer flow ◄─────────────┼┼┼┤                                        │││││
  ├── 3.2 Anti-cheat ───────────────┼┼┼┼───────────────────────────────────────┐│││││
  └── 3.3 Rewards ◄─────────────────┼┼┼┼┐                                       ││││││
                                        ││││││                                       ││││││
Phase 4: Marketplace ───────────────┐│││││                                       ││││││
  ├── 4.1 Marketplace listing ◄────┼┼┼┼┼┤                                       ││││││
  ├── 4.2 Gamification ────────────┼┼┼┼┼┼───────────────────────────────────────┐││││││
  └── 4.3 Audit system ────────────┼┼┼┼┼┼┐                                      │││││││
                                        ││││││││                                      │││││││
Phase 5: Analytics ─────────────────┐││││││                                      │││││││
  ├── 5.1 Statistics ◄─────────────┼┼┼┼┼┼┤                                      │││││││
  ├── 5.2 AI analysis ◄────────────┼┼┼┼┼┼┤                                      │││││││
  └── 5.3 Reports/Export ──────────┼┼┼┼┼┼┼── All prior phases                   │││││││
                                        │││││││                                      │││││││
Phase 6: Admin System ─────────────┐││││││                                      │││││││
  └── All admin controllers ◄─────┼┼┼┼┼┼┼── All prior phases                    │││││││
                                        │││││││                                      │││││││
Phase 7: Integration/Deploy ◄──────── All above ─────────────────────────────────┘││││││
```

---

## Quick Reference: Key SQL Statements

### MySQL Init Script Excerpt (V1__init.sql)

```sql
CREATE DATABASE IF NOT EXISTS smart_survey DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_survey;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(20),
    email VARCHAR(100),
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    bio VARCHAR(200),
    tags JSON,
    real_name VARCHAR(50),
    id_card VARCHAR(255),
    is_verified TINYINT DEFAULT 0,
    level TINYINT DEFAULT 1,
    experience INT DEFAULT 0,
    reputation INT DEFAULT 100,
    balance DECIMAL(12,2) DEFAULT 0.00,
    frozen_balance DECIMAL(12,2) DEFAULT 0.00,
    role VARCHAR(20) DEFAULT 'user',
    status VARCHAR(20) DEFAULT 'normal',
    login_ip VARCHAR(45),
    login_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_status_created (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE surveys (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    cover_image VARCHAR(500),
    status VARCHAR(20) DEFAULT 'draft',
    total_questions INT DEFAULT 0,
    total_responses INT DEFAULT 0,
    target_quota INT,
    remaining_quota INT,
    reward_type VARCHAR(20) DEFAULT 'fixed',
    reward_per_response DECIMAL(10,2),
    reward_total_budget DECIMAL(12,2),
    dispatch_type VARCHAR(20) DEFAULT 'public',
    target_audience JSON,
    is_anonymous TINYINT DEFAULT 0,
    allow_resume TINYINT DEFAULT 1,
    time_limit_minutes INT DEFAULT 0,
    max_attempts INT DEFAULT 1,
    start_time DATETIME,
    end_time DATETIME,
    closing_message VARCHAR(500) DEFAULT '感谢您的参与！',
    view_count INT DEFAULT 0,
    share_count INT DEFAULT 0,
    ai_generated TINYINT DEFAULT 0,
    audit_status VARCHAR(20) DEFAULT 'pending',
    audit_note VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_status_created (status, created_at),
    INDEX idx_marketplace (status, dispatch_type, remaining_quota)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    content TEXT NOT NULL,
    required TINYINT DEFAULT 1,
    order_index INT NOT NULL,
    options JSON,
    settings JSON,
    logic_jump JSON,
    logic_show JSON,
    is_random_options TINYINT DEFAULT 0,
    quota_limit JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_survey_order (survey_id, order_index),
    FOREIGN KEY (survey_id) REFERENCES surveys(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE responses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    survey_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    device_fingerprint VARCHAR(64),
    user_agent VARCHAR(500),
    channel VARCHAR(50),
    start_time DATETIME,
    end_time DATETIME,
    duration_seconds INT,
    reward_amount DECIMAL(10,2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'in_progress',
    review_type VARCHAR(20) DEFAULT 'auto',
    review_note TEXT,
    quality_score DECIMAL(3,2),
    behavior_data JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_survey_user (survey_id, user_id),
    INDEX idx_survey_status (survey_id, status),
    INDEX idx_user_created (user_id, created_at),
    FOREIGN KEY (survey_id) REFERENCES surveys(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    response_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    answer_options JSON,
    answer_rating INT,
    answer_order JSON,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_response (response_id),
    INDEX idx_question (question_id),
    FOREIGN KEY (response_id) REFERENCES responses(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE follows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, followee_id),
    INDEX idx_follower (follower_id),
    INDEX idx_followee (followee_id),
    FOREIGN KEY (follower_id) REFERENCES users(id),
    FOREIGN KEY (followee_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation (sender_id, receiver_id, created_at),
    INDEX idx_receiver_unread (receiver_id, is_read),
    FOREIGN KEY (sender_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    balance_before DECIMAL(12,2),
    balance_after DECIMAL(12,2),
    related_id BIGINT,
    related_type VARCHAR(50),
    status VARCHAR(20) DEFAULT 'pending',
    remark VARCHAR(500),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, created_at),
    INDEX idx_type_time (type, created_at),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    related_id BIGINT,
    related_type VARCHAR(50),
    is_read TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_unread (user_id, is_read, created_at),
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    description VARCHAR(500),
    updated_by BIGINT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## Summary

| Phase | Days | Deliverables | Files Created |
|-------|------|-------------|---------------|
| **P0: Foundation** | 1-2 | Maven, config, DB, common framework | ~15 |
| **P1: User System** | 3-6 | Auth, profile, social, wallet | ~20 |
| **P2: Survey Engine** | 7-12 | CRUD, logic, AI gen, templates | ~15 |
| **P3: Response Collection** | 13-17 | Answer flow, anti-cheat, rewards | ~12 |
| **P4: Marketplace** | 18-24 | Listing, recommendation, gamification, audit | ~12 |
| **P5: Analytics** | 25-30 | Statistics, AI analysis, reports, export | ~10 |
| **P6: Admin System** | 31-36 | User/survey/finance admin, dashboard | ~10 |
| **P7: Integration** | 37-42 | Notifications, WebSocket, testing, CI/CD | ~5 |
| **Total** | **42 days** | **Full v1.0 backend** | **~100 files** |

---

**Plan complete.**
