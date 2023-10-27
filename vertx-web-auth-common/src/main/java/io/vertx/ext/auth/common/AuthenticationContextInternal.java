package io.vertx.ext.auth.common;

import io.vertx.ext.auth.audit.SecurityAudit;

public interface AuthenticationContextInternal {

  /**
   * Get or Default the security audit object.
   */
  SecurityAudit securityAudit();

  /**
   * Get or Default the security audit object.
   */
  void setSecurityAudit(SecurityAudit securityAudit);

}
