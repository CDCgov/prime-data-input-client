package gov.cdc.usds.simplereport.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SimpleSavedRequest;
import org.springframework.http.HttpMethod;

import com.okta.spring.boot.oauth.Okta;

import gov.cdc.usds.simplereport.service.model.IdentityAttributes;
import gov.cdc.usds.simplereport.service.model.IdentitySupplier;

/**
 * Created by nickrobison on 11/13/20
 */
@Configuration
@Profile("!" + DevSecurityConfiguration.PROFILE) // Activate this profile to disable security
@EnableGlobalMethodSecurity(prePostEnabled = true)
@ConditionalOnWebApplication
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String SAVED_REQUEST_HEADER = "SPRING_SECURITY_SAVED_REQUEST";
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);

    public interface OktaAttributes {
        public static String EMAIL = "email";
		public static String FIRST_NAME = "given_name";
		public static String LAST_NAME = "family_name";
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/").permitAll()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				.requestMatchers(EndpointRequest.to(HealthEndpoint.class)).permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .and()
                .oauth2ResourceServer().jwt();
        Okta.configureResourceServer401ResponseBody(http);
    }

    @Bean
    public RequestCache refererRequestCache() {
        return new HttpSessionRequestCache() {
            @Override
            public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
                final String referrer = request.getHeader("referer");
                if (referrer != null) {
                    request.getSession().setAttribute(SAVED_REQUEST_HEADER, new SimpleSavedRequest(referrer));
                }
            }
        };
    }


	@Bean
	public IdentitySupplier getRealIdentity() {
		return () -> {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof OidcUser) {
				OidcUser me = (OidcUser) principal;
				LOG.debug("OIDC user found with attributes {}", me.getAttributes());
				String firstName = me.getAttribute(OktaAttributes.FIRST_NAME);
				String lastName = me.getAttribute(OktaAttributes.LAST_NAME);
				String email = me.getAttribute(OktaAttributes.EMAIL);
				if (lastName == null) {
					lastName = email;
				}
				LOG.debug("Hello OIDC user {} {} ({})", firstName, lastName, email);
				return new IdentityAttributes(email, firstName, null, lastName, null);
			} else if (principal instanceof Jwt) {
				Jwt token = (Jwt) principal;
				LOG.debug("JWT user found with claims {}", token.getClaims());
				String email = token.getSubject();
				String firstName = token.getClaim(OktaAttributes.FIRST_NAME);
				String lastName = token.getClaim(OktaAttributes.LAST_NAME);
				if (lastName == null) {
					lastName = email;
				}
				LOG.debug("Hello JWT user {} {} ({})", firstName, lastName, email);
				return new IdentityAttributes(email, firstName, null, lastName, null);
			}
			throw new RuntimeException("Unexpected authentication principal of type " + principal.getClass());
		};
	}
}
