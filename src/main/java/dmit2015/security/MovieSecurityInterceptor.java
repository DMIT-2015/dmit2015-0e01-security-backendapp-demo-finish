package dmit2015.security;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.security.enterprise.SecurityContext;

public class MovieSecurityInterceptor {

    @Inject
    private SecurityContext _securityContext;

    @AroundInvoke
    public Object verifyAccess(InvocationContext ic) throws Exception {
       // Restrict access to the add and update method to the roles USER and Sales
        String methodName = ic.getMethod().getName();
        if (methodName.startsWith("add") || methodName.startsWith("create") || methodName.startsWith("update")) {
            boolean havePermission = _securityContext.isCallerInRole("USER") || _securityContext.isCallerInRole("SALES");
            if (!havePermission) {
                throw new RuntimeException("Access denied. You do not have permission to execute this method.");
            }
        }
        // Restrict access to the delete method to the roles ADMIN, IT, Administration
        if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            boolean havePermission = _securityContext.isCallerInRole("ADMIN")
                    || _securityContext.isCallerInRole("IT")
                    || _securityContext.isCallerInRole("Administration");
            if (!havePermission) {
                throw new RuntimeException("Access denied. You do not have permission to execute this method.");
            }
        }

        return ic.proceed();
    }
}
