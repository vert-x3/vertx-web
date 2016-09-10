package io.vertx.ext.web;

public interface MIMEHeader extends ParsedHeaderValue{
  
  String component();
  
  String subComponent();
  
}
