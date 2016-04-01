package org.devgateway.geoph.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by sebas on 6/17/14.
 */
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private String jsonUsername;
    private String jsonPassword;
    private final ObjectMapper mapper;

    public JsonUsernamePasswordAuthenticationFilter() {
        mapper = new ObjectMapper();
    }

    @Override
    protected String obtainPassword(HttpServletRequest request) {
        String password = null;

        if (request.getHeader("Content-Type").indexOf("application/json") > -1) {
            password = this.jsonPassword;
        } else {
            password = super.obtainPassword(request);
        }

        return password;
    }

    @Override
    protected String obtainUsername(HttpServletRequest request) {
        String username = null;

        if (request.getHeader("Content-Type").indexOf("application/json") > -1) {
            username = this.jsonUsername;
        } else {
            username = super.obtainUsername(request);
        }

        return username;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (request.getHeader("Content-Type").indexOf("application/json") > -1) {
            try {
                /*
                 * HttpServletRequest can be read only once
                 */
                StringBuilder sb = new StringBuilder();
                String line = null;

                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                //json transformation
                LoginRequest loginRequest = mapper.readValue(sb.toString(), LoginRequest.class);
                this.jsonUsername = loginRequest.getUsername();
                this.jsonPassword = loginRequest.getPassword();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return super.attemptAuthentication(request, response);


    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        attemptAuthentication(request, response);
        chain.doFilter(req, res);
    }


}
