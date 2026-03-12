package com.original.security.test.annotation;

import com.original.security.test.context.WithMockUserSecurityContextFactory;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class WithMockUserTest {

    @Test
    void testWithMockUserCreatesCustomUserDetails() throws NoSuchMethodException {
        WithMockUserSecurityContextFactory factory = new WithMockUserSecurityContextFactory();
        
        WithMockUser mockUserAnnotation = CustomUserTest.class.getMethod("testMethod").getAnnotation(WithMockUser.class);
        
        SecurityContext context = factory.createSecurityContext(mockUserAnnotation);
        
        assertThat(context.getAuthentication()).isNotNull();
        Object principal = context.getAuthentication().getPrincipal();
        
        assertThat(principal).isInstanceOf(MyCustomUser.class);
        MyCustomUser user = (MyCustomUser) principal;
        assertThat(user.getUsername()).isEqualTo("custom_user");
        assertThat(user.getPassword()).isEqualTo("custom_pass");
        assertThat(user.getAuthorities()).hasSize(1);
    }
    
    public static class MyCustomUser extends User {
        public MyCustomUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
            super(username, password, authorities);
        }
    }
    
    static class CustomUserTest {
        @WithMockUser(username = "custom_user", password = "custom_pass", roles = "ADMIN", userDetailsClass = "com.original.security.test.annotation.WithMockUserTest$MyCustomUser")
        public void testMethod() {}
    }
}